package com.chenpperr.xhs.market.service.impl;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.domain.entity.User;
import com.chenpperr.xhs.domain.vo.UserSimpleVO;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.mapper.UserMapper;
import com.chenpperr.xhs.market.config.RabbitMQConfig;
import com.chenpperr.xhs.market.domain.dto.MarketOrderCreateDTO;
import com.chenpperr.xhs.market.domain.entity.MarketItem;
import com.chenpperr.xhs.market.domain.entity.MarketOrder;
import com.chenpperr.xhs.market.domain.vo.MarketOrderVO;
import com.chenpperr.xhs.market.mapper.MarketItemMapper;
import com.chenpperr.xhs.market.mapper.MarketOrderMapper;
import com.chenpperr.xhs.market.mq.message.PaySuccessMessage;
import com.chenpperr.xhs.market.service.MarketItemService;
import com.chenpperr.xhs.market.service.MarketOrderService;
import com.chenpperr.xhs.security.JwtUtil;
import com.chenpperr.xhs.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketOrderServiceImpl extends ServiceImpl<MarketOrderMapper, MarketOrder> implements MarketOrderService {

    private final MarketItemMapper marketItemMapper;

    private final UserMapper userMapper;

    private final RabbitTemplate rabbitTemplate;

    private final MarketItemService marketItemService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(MarketOrderCreateDTO orderCreateDTO) {
        //参数校验与数据获取
        Long itemId = orderCreateDTO.getItemId();
        MarketItem item = marketItemMapper.selectById(itemId);

        if (item == null || item.getStatus() != 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品已下架或不存在");
        }

        Long buyerId = SecurityUtil.getCurrentUserId();
        Long sellerId = item.getSellerId();
        BigDecimal amount = item.getPrice();

        if (item.getSellerId().equals(buyerId)) {
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
        if (locked == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品已被其他人下单");
        }

        //生成唯一订单号
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        String orderSn = "xhs" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + randomNum;

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

        //发送延迟消息给“取消订单”函数
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE,
                RabbitMQConfig.DELAY_ROUTING_KEY,
                order.getOrderSn(),     //这就是要发送的消息内容
                message -> {
                    message.getMessageProperties().setDelayLong(30 * 60 * 1000L);
                    return message;
                }
        );

        return orderSn;
    }


    //TODO: 兜底方案（定时任务）：处理消息丢失或消费失败的异常订单，保证最终一致性。
    //作为延迟消息的生产者
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payOrder(String orderSn) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        MarketOrder order = this.getOne(new LambdaQueryWrapper<MarketOrder>().eq(MarketOrder::getOrderSn, orderSn));
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "未找到订单");
        }
        //身份校验
        if (!order.getBuyerId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能支付自己的订单");
        }
        //检查 status 是否为0（待支付）
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单状态异常，请返回重试");
        }

        //订单CAS，先将状态改为3-支付中，并且先返回，等到mq后续链路支付
        boolean seized = this.update(new LambdaUpdateWrapper<MarketOrder>()
                .set(MarketOrder::getStatus, 3)
                .set(MarketOrder::getUpdateTime, LocalDateTime.now())
                .eq(MarketOrder::getOrderSn, orderSn)
                .eq(MarketOrder::getStatus, 0)
        );
        if (!seized) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单已被取消或已支付");
        }

        //1.扣买家余额：条件更新 balance >= amount，原子操作防止余额扣成负数
        //余额CAS
        int rows = userMapper.update(null, new LambdaUpdateWrapper<User>()
                .setSql("balance = balance - {0}", order.getAmount())
                .eq(User::getId, order.getBuyerId())
                .ge(User::getBalance, order.getAmount()));
        if (rows == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "余额不足");
        }

        //2.发送MQ消息通知交易/订单服务（异步）
        PaySuccessMessage message = new PaySuccessMessage(
                order.getOrderSn(), order.getBuyerId(), order.getSellerId(), order.getItemId()
        );

        //事务内部直接发消息，等于同步调用
//        rabbitTemplate.convertAndSend(
//                RabbitMQConfig.PAY_EXCHANGE,
//                RabbitMQConfig.PAY_ROUTING_KEY,
//                message
//        );

        //在事务提交后再消息，才是异步
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.PAY_EXCHANGE,
                            RabbitMQConfig.PAY_ROUTING_KEY,
                            message
                    );
                } catch (Exception e) {
                    //走到这里 = 钱已扣、订单卡在3、消息没发出去 → 只能靠定时任务兜底，必须留下 error 日志
                    log.error("支付消息发送失败，订单号：{}", order.getOrderSn(), e);
                }
            }
        });

        //返回支付结果
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(PaySuccessMessage msg) {
        //1.查询订单及其状态
        MarketOrder order = this.getOne(
                new LambdaUpdateWrapper<MarketOrder>()
                        .eq(MarketOrder::getOrderSn, msg.getOrderSn()));


        //幂等/防重复

        if (order == null || order.getStatus() == 0) {
            log.warn("订单异常");
            return;
        }

        //订单状态: 0-待支付, 1-已完成(已支付), 2-已取消(超时/手动), 3-支付中

        if (order.getStatus() == 1) { //支付成功，直接return
            return;
        }

        if (order.getStatus() == 2) { //已取消（超时/手动）-> 重新给买家添加余额
            boolean firstRefund = this.update(new LambdaUpdateWrapper<MarketOrder>()
                    .set(MarketOrder::getRefunded, 1)
                    .set(MarketOrder::getUpdateTime, LocalDateTime.now())
                    .eq(MarketOrder::getOrderSn, order.getOrderSn())
                    .eq(MarketOrder::getRefunded, 0));    // ← 令牌：只有"未退款"才抢得到
            if (!firstRefund) {
                log.warn("已退款，跳过重复消息，订单号：{}", msg.getOrderSn());
                return;
            }
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("balance = balance + {0}", order.getAmount())
                    .eq(User::getId, order.getBuyerId()));
            log.warn("订单已取消，买家退款完成，订单号：{}", msg.getOrderSn());
            return;
        }

        if(order.getStatus() == 3){

            //2.更新订单状态，时间
            boolean orderUpdate = this.update(
                    new LambdaUpdateWrapper<MarketOrder>()
                            .set(MarketOrder::getStatus, 1)
                            .set(MarketOrder::getUpdateTime, LocalDateTime.now())
                            .set(MarketOrder::getPayTime, LocalDateTime.now())
                            .eq(MarketOrder::getOrderSn, msg.getOrderSn())
                            .eq(MarketOrder::getStatus, 3)
            );

            if (!orderUpdate) {
                throw new RuntimeException("订单状态异常");
            }

        }



        //3.更新商品状态,时间
        boolean itemUpdated = marketItemService.update(
                new LambdaUpdateWrapper<MarketItem>()
                        .set(MarketItem::getStatus, 2)
                        .set(MarketItem::getUpdateTime, LocalDateTime.now())
                        .eq(MarketItem::getStatus, 1)
                        .eq(MarketItem::getId, msg.getItemId())
        );

        //代表更新失败，抛异常，rabbitmq重发
        if (!itemUpdated) {
            throw new RuntimeException("商品状态异常，订单号：" + msg.getOrderSn());
        }

        //4.卖家加钱
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .setSql("balance = balance + {0}", order.getAmount())
                .eq(User::getId, msg.getSellerId())
        );
    }

    //作为延迟消息的消费者
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(String orderSn) {
        //查询订单是否存在
        MarketOrder order = this.getOne(new LambdaQueryWrapper<MarketOrder>().eq(MarketOrder::getOrderSn, orderSn));
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }

        //查看订单状态，只能取消待支付的订单
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能取消待支付的订单");
        }

        //判断是否为买家，不能取消他人订单
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!order.getBuyerId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能取消自己的订单");
        }

        //修改商品状态为：0-待售
        boolean cancelled = this.update(new LambdaUpdateWrapper<MarketOrder>()
                .set(MarketOrder::getStatus, 2)
                .set(MarketOrder::getUpdateTime, LocalDateTime.now())
                .set(MarketOrder::getPayTime, null)
                .eq(MarketOrder::getStatus, 0)
                .eq(MarketOrder::getOrderSn, orderSn)
        );
        if(!cancelled) {
            throw new BusinessException(ResultCode.FORBIDDEN, "订单已支付或支付中，无法取消");
        }

        marketItemMapper.update(null, new LambdaUpdateWrapper<MarketItem>()
                .set(MarketItem::getStatus, 0)
                .set(MarketItem::getUpdateTime, LocalDateTime.now())
                .eq(MarketItem::getId, order.getItemId())
                .eq(MarketItem::getStatus, 1)
        );

        return true;
    }

    @Override
    public Page<MarketOrderVO> getMyOrders(Page<MarketOrder> pageParam, String type) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        LambdaQueryWrapper<MarketOrder> wrapper = new LambdaQueryWrapper<>();

        //type：卖出/买入/空
        if ("seller".equals(type)) {
            wrapper.eq(MarketOrder::getSellerId, currentUserId);
        } else if ("buyer".equals(type)) {
            wrapper.eq(MarketOrder::getBuyerId, currentUserId);
        } else {
            //不传type时，买卖都能查出来
            wrapper.and(w -> w.eq(MarketOrder::getSellerId, currentUserId)
                    .or()
                    .eq(MarketOrder::getBuyerId, currentUserId));
        }

        wrapper.orderByDesc(MarketOrder::getCreateTime);

        Page<MarketOrder> orderPage = this.page(pageParam, wrapper);
        List<MarketOrder> orders = orderPage.getRecords();

        //批量查询商品信息：itemId -> MarketItem，避免循环查库
        Map<Long, MarketItem> itemMap = orders.isEmpty() ? Collections.emptyMap()
                : marketItemMapper.selectBatchIds(orders.stream()
                        .map(MarketOrder::getItemId)
                        .distinct()
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(MarketItem::getId, Function.identity()));

        //批量查询交易对方用户信息：对方userId -> UserSimpleVO
        Map<Long, UserSimpleVO> userMap = orders.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(orders.stream()
                        .map(o -> resolveCounterpartyId(o, currentUserId))
                        .distinct()
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(User::getId, this::toUserSimpleVO));

        //组装 VO 列表
        List<MarketOrderVO> voList = orders.stream().map(order -> {
            MarketItem item = itemMap.get(order.getItemId());
            Long counterpartyId = resolveCounterpartyId(order, currentUserId);
            //防御性兜底：对方用户被删除时显示「已注销用户」
            UserSimpleVO counterparty = userMap.getOrDefault(counterpartyId,
                    UserSimpleVO.builder()
                            .userId(counterpartyId)
                            .nickname("已注销用户")
                            .avatar(null)
                            .build());

            return MarketOrderVO.builder()
                    .id(order.getId())
                    .orderSn(order.getOrderSn())
                    .itemId(order.getItemId())
                    //防御性兜底：商品被物理删除时给默认文案
                    .itemTitle(item != null ? item.getTitle() : "商品已删除")
                    .itemCoverUrl(item != null ? item.getCoverUrl() : null)
                    .amount(order.getAmount())
                    .status(order.getStatus())
                    .counterparty(counterparty)
                    .payTime(order.getPayTime())
                    .createTime(order.getCreateTime())
                    .build();
        }).collect(Collectors.toList());

        //构造 VO 分页，保留原分页信息
        Page<MarketOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrderOnTimeOut(String orderSn) {
        //1.查询订单
        MarketOrder order = this.getOne(new LambdaQueryWrapper<MarketOrder>().eq(MarketOrder::getOrderSn, orderSn));

        //2.状态校验
        if (order == null || order.getStatus() != 0) {
            log.info("订单不存在或已被处理，无需取消，订单号：{}", orderSn);
            return false;
        }

        //订单状态: 0-待支付, 1-已完成(已支付), 2-已取消(超时/手动), 3-支付中

        //3.CAS乐观锁
        boolean orderUpdated = this.update(
                new LambdaUpdateWrapper<MarketOrder>()
                        .set(MarketOrder::getStatus, 2)
                        .set(MarketOrder::getUpdateTime, LocalDateTime.now())
                        .eq(MarketOrder::getOrderSn, orderSn)
                        .eq(MarketOrder::getStatus, 0)
        );

        if (!orderUpdated) {
            return false;
        }

        //商品状态: 0-待售, 1-锁定中(已下单未支付), 2-已售出, 3-已下架
        marketItemMapper.update(
                null,
                new LambdaUpdateWrapper<MarketItem>()
                        .set(MarketItem::getStatus, 0)
                        .set(MarketItem::getUpdateTime, LocalDateTime.now())
                        .eq(MarketItem::getId, order.getItemId())
                        .eq(MarketItem::getStatus, 1)
        );

        return true;
    }

    /**
     * 计算交易对方ID：买家视角返回卖家ID，卖家视角返回买家ID
     */
    private Long resolveCounterpartyId(MarketOrder order, Long currentUserId) {
        return order.getBuyerId().equals(currentUserId) ? order.getSellerId() : order.getBuyerId();
    }

    private UserSimpleVO toUserSimpleVO(User user) {
        return UserSimpleVO.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }
}







