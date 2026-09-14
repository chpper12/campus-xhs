package com.chenpperr.xhs.market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.domain.entity.Post;
import com.chenpperr.xhs.market.domain.dto.MarketItemPublishDTO;
import com.chenpperr.xhs.market.domain.entity.MarketItem;
import com.chenpperr.xhs.market.service.MarketItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market/items")
@RequiredArgsConstructor
public class MartetItemController {

    private final MarketItemService marketItemService;

    //获取商品列表
    @GetMapping
    public Result<Page<MarketItem>> getItems(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        Page<MarketItem> pageParam = new Page<>(pageNum, pageSize);
        Page<MarketItem> page = marketItemService.getItems(pageParam, keyword);

        return Result.success(page);
    }

    //获取商品详情
    @GetMapping("/{id}")
    public Result<MarketItem> getItemById(@PathVariable Long id) {
        MarketItem item = marketItemService.getItemById(id);
        return Result.success(item);
    }

    //发布商品
    @PostMapping
    public Result<Boolean> publishItem(@Valid @RequestBody MarketItemPublishDTO marketItemPublishDTO) {
        boolean success = marketItemService.publishItem(marketItemPublishDTO);
        return  Result.success(success);
    }

    //下架商品
    @PutMapping("/{id}/off-shelf")
    public Result<Boolean> offShelfItem(@PathVariable Long id) {
        boolean success = marketItemService.offShelfItem(id);
        return Result.success(success);
    }

}
