import request from '@/utils/request'

/** 用户简要信息 VO */
export interface UserSimpleVO {
  userId: number
  nickname: string
  avatar: string
}

/** 通知类型 */
export type NoticeType = 'like' | 'comment' | 'follow'

/** 通知 VO */
export interface NoticeVO {
  id: number
  type: NoticeType
  content: string
  fromUser: UserSimpleVO
  postId: number
  isRead: boolean
  createTime: string
}

/** 分页结果（后端 PageResult 结构） */
export interface NoticePageResult {
  list: NoticeVO[]
  total: number
}

/**
 * 获取通知列表
 * GET /api/v1/notifications?current=1&size=10
 */
export function getNotifications(params: { current: number; size: number }): Promise<NoticePageResult> {
  return request({
    url: '/v1/notifications',
    method: 'get',
    params
  })
}

/**
 * 标记通知为已读
 * PUT /api/v1/notifications/{id}/read
 */
export function markAsRead(id: number): Promise<void> {
  return request({
    url: `/v1/notifications/${id}/read`,
    method: 'put'
  })
}
