package com.chenpperr.xhs.market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpperr.xhs.market.domain.dto.MarketItemPublishDTO;
import com.chenpperr.xhs.market.domain.entity.MarketItem;
/**
 * 商品业务接口
 */
public interface MarketItemService extends IService<MarketItem> {

    //获取商品列表
    Page<MarketItem> getItems(Page<MarketItem> pageParam, String keyword);

    //获取商品详情
    MarketItem getItemById(Long id);

    //发布商品
    Boolean publishItem(MarketItemPublishDTO marketItemPublishDTO);

    //下架商品
    Boolean offShelfItem(Long id);
}
