package com.chenpperr.xhs.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.market.domain.dto.MarketItemPublishDTO;
import com.chenpperr.xhs.market.domain.entity.MarketItem;
import com.chenpperr.xhs.market.mapper.MarketItemMapper;
import com.chenpperr.xhs.market.service.MarketItemService;
import com.chenpperr.xhs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class MarketItemServiceImpl extends ServiceImpl<MarketItemMapper, MarketItem> implements MarketItemService {

    @Override
    public Page<MarketItem> getItems(Page<MarketItem> pageParam, String keyword) {
        LambdaQueryWrapper<MarketItem> queryWrapper = new LambdaQueryWrapper<>();

        //只显示未售出的商品
        queryWrapper.eq(MarketItem::getStatus, 0);

        if(StringUtils.isNotBlank(keyword)){
            queryWrapper.like(MarketItem::getTitle, keyword);
        }

        queryWrapper.orderByDesc(MarketItem::getCreateTime);

        Page<MarketItem> itemPage = this.page(pageParam, queryWrapper);

        return itemPage;

    }

    @Override
    public MarketItem getItemById(Long id) {

        MarketItem item = this.getById(id);
        return item;
    }

    @Override
    public Boolean publishItem(MarketItemPublishDTO marketItemPublishDTO) {
        //从 JWT 上下文获取当前用户ID
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if(currentUserId == null){
            throw new RuntimeException("用户登录信息错误");
        }

        //创建实体并拷贝 DTO 属性
        MarketItem item = new MarketItem();
        BeanUtils.copyProperties(marketItemPublishDTO,item);

        //填充后端业务控制字段
        item.setSellerId(currentUserId);
        item.setStatus(0); // 状态: 0-待售, 1-锁定中(已下单未支付), 2-已售出, 3-已下架
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        //插入数据库
        this.save(item);

        return true;
    }

    @Override
    public Boolean offShelfItem(Long id) {
        //获取当前用户
        Long currentUserId = SecurityUtil.getCurrentUserId();

        //根据 id 查询商品
        MarketItem item = this.getById(id);

        if(item == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        //越权校验：只能下架自己的商品
        if(!item.getSellerId().equals(currentUserId)){
            throw new BusinessException(ResultCode.FORBIDDEN, "只能下架自己的商品");
        }
        //只有待售商品允许下架
        if(item.getStatus()!=0){
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前商品状态不允许下架");
        }

        //修改状态
        item.setStatus(3);
        return this.updateById(item);
    }
}
