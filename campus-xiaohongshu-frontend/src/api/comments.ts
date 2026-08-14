import request from '@/utils/request'

export interface CommentAuthor {
  userId: number
  nickname: string
  avatar: string
}

export interface Comment {
  id: number
  postId: number
  content: string
  author: CommentAuthor
  createTime: string
}

export interface CommentListResponse {
  list: Comment[]
  total: number
}

export function getComments(postId: number, params: { current: number; size: number }): Promise<CommentListResponse> {
  return request({
    url: `/v1/posts/${postId}/comments`,
    method: 'get',
    params
  })
}

export function addComment(postId: number, content: string): Promise<number> {
  return request({
    url: `/v1/posts/${postId}/comments`,
    method: 'post',
    data: { content }
  })
}
