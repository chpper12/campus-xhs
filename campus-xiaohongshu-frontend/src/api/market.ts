import request from '@/utils/request'
import type {
  Page,
  MarketItemVO,
  MarketOrderVO,
  MarketItemPublishDTO,
  MarketOrderCreateDTO,
  MarketItemListParams,
  MyOrderListParams
} from '@/types/market'

/**
 * 获取商品列表
 * GET /api/v1/market/items?pageNum=1&pageSize=10&keyword=xxx
 */
export function getMarketItems(params?: MarketItemListParams): Promise<Page<MarketItemVO>> {
  return request({
    url: '/v1/market/items',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 * GET /api/v1/market/items/{id}
 */
export function getMarketItemById(id: number): Promise<MarketItemVO> {
  return request({
    url: `/v1/market/items/${id}`,
    method: 'get'
  })
}

/**
 * 发布商品
 * POST /api/v1/market/items
 */
export function publishMarketItem(data: MarketItemPublishDTO): Promise<boolean> {
  return request({
    url: '/v1/market/items',
    method: 'post',
    data
  })
}

/**
 * 下架商品（仅卖家本人）
 * PUT /api/v1/market/items/{id}/off-shelf
 */
export function offShelfMarketItem(id: number): Promise<boolean> {
  return request({
    url: `/v1/market/items/${id}/off-shelf`,
    method: 'put'
  })
}

/**
 * 创建订单（购买），返回订单号 orderSn
 * POST /api/v1/market/orders
 */
export function createMarketOrder(data: MarketOrderCreateDTO): Promise<string> {
  return request({
    url: '/v1/market/orders',
    method: 'post',
    data
  })
}

/**
 * 模拟支付（仅买家本人）
 * POST /api/v1/market/orders/{orderSn}/pay
 */
export function payMarketOrder(orderSn: string): Promise<boolean> {
  return request({
    url: `/v1/market/orders/${orderSn}/pay`,
    method: 'post'
  })
}

/**
 * 取消订单（仅买家本人）
 * POST /api/v1/market/orders/{orderSn}/cancel
 */
export function cancelMarketOrder(orderSn: string): Promise<boolean> {
  return request({
    url: `/v1/market/orders/${orderSn}/cancel`,
    method: 'post'
  })
}

/**
 * 我的买卖记录
 * GET /api/v1/market/orders/my?pageNum=1&pageSize=10&type=buyer|seller
 */
export function getMyMarketOrders(params?: MyOrderListParams): Promise<Page<MarketOrderVO>> {
  return request({
    url: '/v1/market/orders/my',
    method: 'get',
    params
  })
}
