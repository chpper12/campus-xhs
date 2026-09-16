package com.chenpperr.xhs.market.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.market.domain.dto.MarketOrderCreateDTO;
import com.chenpperr.xhs.market.domain.entity.MarketOrder;
import com.chenpperr.xhs.market.domain.vo.MarketOrderVO;
import com.chenpperr.xhs.market.service.MarketOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/market/orders")
@RequiredArgsConstructor
public class MarketOrderController {

    private final MarketOrderService marketOrderService;

    //创建订单（买家点击购买，并发乐观锁，MQ 延迟消息）
    @PostMapping
    public Result<String> createOrder(@Valid @RequestBody MarketOrderCreateDTO marketOrderCreateDTO){
        String orderSn = marketOrderService.createOrder(marketOrderCreateDTO);
        return Result.success(orderSn);
    }

    //模拟支付
    @PostMapping("/{orderSn}/pay")
    public Result<Boolean> payOrder(@PathVariable String orderSn){
        Boolean success =  marketOrderService.payOrder(orderSn);
        return Result.success(success);
    }

    //取消订单
    @PostMapping("/{orderSn}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable String orderSn){
        Boolean success = marketOrderService.cancelOrder(orderSn);
        return Result.success(success);
    }

    //买卖记录（VO 含商品标题/封面、交易对方昵称头像，前端一次请求即可渲染）
    @GetMapping("/my")
    public Result<Page<MarketOrderVO>> getMyOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type){

        Page<MarketOrder> pageParam = new Page<>(pageNum, pageSize);
        Page<MarketOrderVO> page = marketOrderService.getMyOrders(pageParam, type);
        return Result.success(page);
    }

}
