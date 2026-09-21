package com.chenpperr.xhs.market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.market.domain.dto.MarketOrderCreateDTO;
import com.chenpperr.xhs.market.domain.entity.MarketOrder;
import com.chenpperr.xhs.market.domain.vo.MarketOrderVO;
import com.chenpperr.xhs.market.mq.message.PaySuccessMessage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 订单业务接口
 */
public interface MarketOrderService  extends IService<MarketOrder> {

    //创建订单（买家点击购买，并发乐观锁，MQ 延迟消息）
    String createOrder(MarketOrderCreateDTO marketOrderCreateDTO);

    //模拟支付
    Boolean payOrder(String orderSn);

    //取消订单
    Boolean cancelOrder(String orderSn);

    //买卖记录（返回带商品标题/封面、交易对方信息的 VO 分页）
    Page<MarketOrderVO> getMyOrders(Page<MarketOrder> pageParam, String type);

    //取消超时订单
    Boolean cancelOrderOnTimeOut(String orderSn);

    //处理支付成功的订单
    void handlePaySuccess(PaySuccessMessage msg);
}

