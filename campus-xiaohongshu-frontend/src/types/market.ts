/**
 * 校园市集类型定义
 * 依据 docs/market-api-contract.md
 */

/** 商品状态: 0-待售, 1-锁定中(已下单未支付), 2-已售出, 3-已下架 */
export type MarketItemStatus = 0 | 1 | 2 | 3

/** 订单状态: 0-待支付, 1-已完成(已支付), 2-已取消(超时/手动) */
export type MarketOrderStatus = 0 | 1 | 2

/** 买卖记录角色过滤: buyer-我买到的, seller-我卖出的, 不传-全部 */
export type OrderQueryType = 'buyer' | 'seller'

/** MyBatis-Plus 分页结构 */
export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/** 商品视图对象（对应后端 MarketItemVO，用于商品列表/详情展示） */
export interface MarketItemVO {
  id: number
  title: string
  description: string | null
  price: number
  coverUrl: string
  /** JSON 数组格式的字符串，前端需自行 JSON.parse */
  imageUrls: string | null
  contactInfo: string
  status: MarketItemStatus
  /** 卖家信息 */
  seller: UserSimpleVO
  createTime: string
}

/** 用户简要信息（对应后端 UserSimpleVO） */
export interface UserSimpleVO {
  userId: number
  nickname: string
  avatar: string
}

/** 订单视图对象（对应后端 MarketOrderVO，用于「我的订单」列表） */
export interface MarketOrderVO {
  id: number
  /** 唯一订单号，支付/取消均以此为准 */
  orderSn: string
  itemId: number
  /** 商品标题（商品被删除时返回「商品已删除」） */
  itemTitle: string
  /** 商品封面图 URL */
  itemCoverUrl: string
  amount: number
  status: MarketOrderStatus
  /** 交易对方（买家视角=卖家，卖家视角=买家） */
  counterparty: UserSimpleVO
  payTime: string | null
  createTime: string
}

/** 发布商品请求参数 */
export interface MarketItemPublishDTO {
  /** 非空；长度 ≤ 100 */
  title: string
  description?: string
  /** 必须 > 0；最多 8 位整数 + 2 位小数 */
  price: number
  coverUrl?: string
  /** JSON 数组格式的字符串 */
  imageUrls?: string
  /** 非空；长度 ≤ 100（微信/电话） */
  contactInfo: string
}

/** 创建订单请求参数 */
export interface MarketOrderCreateDTO {
  /** 非空；必须 > 0 */
  itemId: number
}

/** 商品列表查询参数 */
export interface MarketItemListParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
}

/** 买卖记录查询参数 */
export interface MyOrderListParams {
  pageNum?: number
  pageSize?: number
  type?: OrderQueryType
}
