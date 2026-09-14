package com.chenpperr.xhs.market.service.impl;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.domain.entity.User;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.mapper.UserMapper;
import com.chenpperr.xhs.market.domain.dto.MarketOrderCreateDTO;
import com.chenpperr.xhs.market.domain.entity.MarketItem;
import com.chenpperr.xhs.market.domain.entity.MarketOrder;
import com.chenpperr.xhs.market.mapper.MarketItemMapper;
import com.chenpperr.xhs.market.mapper.MarketOrderMapper;
import com.chenpperr.xhs.market.service.MarketOrderService;
import com.chenpperr.xhs.security.JwtUtil;
import com.chenpperr.xhs.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MarketOrderServiceImpl extends ServiceImpl<MarketOrderMapper, MarketOrder> implements MarketOrderService {

    private final MarketItemMapper marketItemMapper;

    private final UserMapper userMapper;

    @Override
    public String createOrder(MarketOrderCreateDTO orderCreateDTO) {
        //参数校验与数据获取
        Long itemId = orderCreateDTO.getItemId();
        MarketItem item = marketItemMapper.selectById(itemId);

        if(item == null || item.getStatus()!=0){
            throw new BusinessException(ResultCode.NOT_FOUND, "商品已下架或不存在");
        }

        Long buyerId = SecurityUtil.getCurrentUserId();
        Long sellerId = item.getSellerId();
        BigDecimal amount = item.getPrice();
        Long currentUserId = SecurityUtil.getCurrentUserId();

        if(item.getSellerId().equals(buyerId)){
            throw new BusinessException(ResultCode.FORBIDDEN, "不能购买自己的商品");
        }

        //锁定商品：只有status = 0才能被锁定，乐观锁
        int locked = marketItemMapper.update(null, new LambdaUpdateWrapper<MarketItem>()
                .set(MarketItem::getStatus, 1)
                .set(MarketItem::getUpdateTime, LocalDateTime.now())
                .eq(MarketItem::getId, itemId)
                .eq(MarketItem::getStatus, 0)
        );
        //也就是没有修改成功
        if(locked == 0){
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品已被其他人下单");
        }

        //生成唯一订单号
        int randomNum = ThreadLocalRandom.current().nextInt(1000,10000);
        String orderSn = "xhs"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + randomNum;

        //封装订单实体并落库
        MarketOrder order = new MarketOrder();
        order.setOrderSn(orderSn);
        order.setItemId(itemId);
        //订单状态预先修改为：待支付；订单状态: 0-待支付, 1-已完成(已支付), 2-已取消(超时/手动)
        order.setStatus(0);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setAmount(amount);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        this.save(order);

        return orderSn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payOrder(String orderSn) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        MarketOrder order = this.getOne(new LambdaQueryWrapper<MarketOrder>().eq(MarketOrder::getOrderSn, orderSn));
        if(order==null){
            throw new BusinessException(ResultCode.NOT_FOUND, "未找到订单");
        }

        //身份校验
        if(!order.getBuyerId().equals(currentUserId)){
            throw new BusinessException(ResultCode.FORBIDDEN, "只能支付自己的订单");
        }


        //检查 status 是否为0（待支付）
        if(order.getStatus() != 0){
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单状态异常，请返回重试");
        }

        //扣买家余额：条件更新 balance >= amount，原子操作防止余额扣成负数
        //rows 代表的是数据库中实际受到影响的行数（受影响的记录条数）
        int rows = userMapper.update(null, new LambdaUpdateWrapper<User>()
                .setSql("balance = balance - {0}", order.getAmount())
                .eq(User::getId, order.getBuyerId())
                .ge(User::getBalance, order.getAmount()));
        if (rows == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "余额不足");
        }

        //开启事务修改：用户表、订单表、商品表

        //卖家收钱
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .setSql("balance = balance + {0}", order.getAmount())
                .eq(User::getId, order.getSellerId())
        );

        //更新商品状态：已售出
        MarketItem item = marketItemMapper.selectById(order.getItemId());
        item.setStatus(2);
        item.setUpdateTime(LocalDateTime.now());
        marketItemMapper.updateById(item);

        //更新订单状态/时间：已完成
        order.setStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        //返回支付结果
        return true;
    }

    @Override
    public Boolean cancelOrder(String orderSn) {
        //查询订单是否存在
        MarketOrder order = this.getOne(new LambdaQueryWrapper<MarketOrder>().eq(MarketOrder::getOrderSn, orderSn));
        if(order==null){
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        
        //查看订单状态，只能取消待支付的订单
        if(order.getStatus() != 0){
            throw new BusinessException(ResultCode.FORBIDDEN, "只能取消待支付的订单");
        }

        //判断是否为买家，不能取消他人订单
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if(!order.getBuyerId().equals(currentUserId)){
            throw new BusinessException(ResultCode.FORBIDDEN, "只能取消自己的订单");
        }

        //修改商品状态为：0-待售
        MarketItem item = marketItemMapper.selectById(order.getItemId());
        item.setStatus(0);
        order.setStatus(2);

        marketItemMapper.updateById(item);
        this.updateById(order);

        return true;
    }

    @Override
    public Page<MarketOrder> getMyOrders(Page<MarketOrder> pageParam, String type) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        LambdaQueryWrapper<MarketOrder> wrapper = new LambdaQueryWrapper<>();

        //type：卖出/买入/空
        if("seller".equals(type)){
            wrapper.eq(MarketOrder::getSellerId, currentUserId);
        }else if("buyer".equals(type)){
            wrapper.eq(MarketOrder::getBuyerId, currentUserId);
        }else{
            //不传type时，买卖都能查出来
            wrapper.and(w -> w.eq(MarketOrder::getSellerId, currentUserId)
                    .or()
                    .eq(MarketOrder::getBuyerId, currentUserId));
        }

        wrapper.orderByDesc(MarketOrder::getCreateTime);

        return this.page(pageParam, wrapper);

    }
}







