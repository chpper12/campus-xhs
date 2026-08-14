import request from '@/utils/request'

export interface PostAuthor {
  userId: number
  nickname: string
  avatar: string
}

export interface Post {
  id: number
  title: string
  category: string
  coverUrl: string
  likeCount: number
  liked: boolean
  author: PostAuthor
  createTime: string
}

export interface PostListResponse {
  list: Post[]
  total: number
}

export function getPosts(params: { current: number; size: number; category?: string }): Promise<PostListResponse> {
  return request({
    url: '/v1/posts',
    method: 'get',
    params
  })
}

export interface PostDetail {
  id: number
  title: string
  category: string
  content: string
  imageUrls: string[]
  likeCount: number
  liked: boolean
  commentCount: number
  tags: string[]
  author: PostAuthor
  createTime: string
}

export function getPostDetail(id: number): Promise<PostDetail> {
  return request({
    url: `/v1/posts/${id}`,
    method: 'get'
  })
}

/**
 * 按关键词搜索笔记
 * GET /api/v1/posts/search?keyword=xxx&current=1&size=10
 */
export function searchPosts(params: { keyword: string; current: number; size: number }): Promise<PostListResponse> {
  return request({
    url: '/v1/posts/search',
    method: 'get',
    params
  })
}

/**
 * 获取当前用户赞过的笔记
 * GET /api/v1/posts/my/liked?current=1&size=10
 */
export function getMyLikedPosts(params: { current: number; size: number }): Promise<PostListResponse> {
  return request({
    url: '/v1/posts/my/liked',
    method: 'get',
    params
  })
}

export async function likePost(id: number): Promise<{ isLiked: boolean; likeCount: number }> {
  const isLiked = await request({
    url: `/v1/posts/${id}/like`,
    method: 'post'
  }) as boolean

  const likeCount = await request({
    url: `/v1/posts/${id}/like-count`,
    method: 'get'
  }) as number

  return { isLiked, likeCount }
}

/**
 * 删除笔记（只有作者自己可以删除）
 * DELETE /api/v1/posts/{id}
 */
export function deletePost(id: number): Promise<void> {
  return request({
    url: `/v1/posts/${id}`,
    method: 'delete'
  })
}
