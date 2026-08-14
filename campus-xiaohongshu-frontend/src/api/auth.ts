import request from '@/utils/request'

interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  nickname?: string
}

interface LoginResponse {
  token: string
  userId: number
  username: string
  nickname: string
  avatar: string
  bio: string
}

export function login(params: LoginParams): Promise<LoginResponse> {
  return request({
    url: '/v1/auth/login',
    method: 'post',
    data: params
  })
}

export function register(params: RegisterParams): Promise<LoginResponse> {
  return request({
    url: '/v1/auth/register',
    method: 'post',
    data: params
  })
}
