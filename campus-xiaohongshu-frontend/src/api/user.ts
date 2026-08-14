import request from '@/utils/request'

// ======================== Interface 定义（严格按后端 VO 字段名） ========================

/** 用户资料 VO — 对应后端 UserProfileVO */
export interface UserProfile {
  userId: number
  nickname: string
  avatar: string
  bio: string
  postCount: number
  followingCount: number
  followerCount: number
  isFollowed: boolean | null
}

/** 笔记作者 — 对应后端 UserSimpleVO */
export interface PostAuthor {
  userId: number
  nickname: string
  avatar: string
}

/** 笔记卡片 VO — 对应后端 PostCardVO */
export interface UserPost {
  id: number
  title: string
  category: string
  coverUrl: string
  likeCount: number
  liked: boolean
  author: PostAuthor
  createTime: string
}

/** 笔记列表分页响应 */
export interface UserPostListResponse {
  list: UserPost[]
  total: number
}

/** 编辑个人资料请求参数 — 对应后端 UpdateUserDTO */
export interface UpdateProfileParams {
  nickname?: string
  bio?: string
  avatar?: string
}

// ======================== API 函数 ========================

/**
 * 获取用户资料
 * GET /api/v1/users/{userId}
 */
export function getUserProfile(userId: number): Promise<UserProfile> {
  return request({
    url: `/v1/users/${userId}`,
    method: 'get'
  })
}

/**
 * 分页获取用户的笔记列表
 * GET /api/v1/users/{userId}/posts?current=1&size=10
 */
export function getUserPosts(
  userId: number,
  params: { current: number; size: number }
): Promise<UserPostListResponse> {
  return request({
    url: `/v1/users/${userId}/posts`,
    method: 'get',
    params
  })
}

/**
 * 关注用户
 * POST /api/v1/users/{userId}/follow
 */
export function followUser(userId: number): Promise<void> {
  return request({
    url: `/v1/users/${userId}/follow`,
    method: 'post'
  })
}

/**
 * 取消关注
 * DELETE /api/v1/users/{userId}/follow
 */
export function unfollowUser(userId: number): Promise<void> {
  return request({
    url: `/v1/users/${userId}/follow`,
    method: 'delete'
  })
}

/**
 * 查询是否已关注
 * GET /api/v1/users/{userId}/followed
 */
export function isFollowed(userId: number): Promise<boolean> {
  return request({
    url: `/v1/users/${userId}/followed`,
    method: 'get'
  })
}

/**
 * 获取关注数
 * GET /api/v1/users/{userId}/following-count
 */
export function getFollowingCount(userId: number): Promise<number> {
  return request({
    url: `/v1/users/${userId}/following-count`,
    method: 'get'
  })
}

/**
 * 获取粉丝数
 * GET /api/v1/users/{userId}/follower-count
 */
export function getFollowerCount(userId: number): Promise<number> {
  return request({
    url: `/v1/users/${userId}/follower-count`,
    method: 'get'
  })
}

/**
 * 编辑个人资料
 * PUT /api/v1/users/me
 */
export function updateProfile(params: UpdateProfileParams): Promise<void> {
  return request({
    url: '/v1/users/me',
    method: 'put',
    data: params
  })
}
