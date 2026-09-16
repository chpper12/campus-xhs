package com.chenpperr.xhs.market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpperr.xhs.market.domain.dto.MarketItemPublishDTO;
import com.chenpperr.xhs.market.domain.entity.MarketItem;
import com.chenpperr.xhs.market.domain.vo.MarketItemVO;
/**
 * 商品业务接口
 */
public interface MarketItemService extends IService<MarketItem> {

    //获取商品列表（返回带卖家信息的 VO 分页）
    Page<MarketItemVO> getItems(Page<MarketItem> pageParam, String keyword);

    //获取商品详情（返回带卖家信息的 VO）
    MarketItemVO getItemById(Long id);

    //发布商品
    Boolean publishItem(MarketItemPublishDTO marketItemPublishDTO);

    //下架商品
    Boolean offShelfItem(Long id);
}
