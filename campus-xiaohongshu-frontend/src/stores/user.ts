import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  token: string
  userId: number
  nickname: string
  avatar: string
}

export const useUserStore = defineStore('user', () => {
  // 状态（自 hydrate：store 首次创建时同步从 localStorage 读初始值）
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(Number(localStorage.getItem('userId')) || 0)
  const nickname = ref(localStorage.getItem('nickname') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 登录/注册后设置用户信息
  function setUser(data: { token: string; userId: number; nickname: string; avatar?: string }) {
    token.value = data.token
    userId.value = data.userId
    nickname.value = data.nickname
    avatar.value = data.avatar || ''

    // 同步到 localStorage（持久化）
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', String(data.userId))
    localStorage.setItem('nickname', data.nickname)
    localStorage.setItem('avatar', data.avatar || '')
  }

  // 退出登录
  function clearUser() {
    token.value = ''
    userId.value = 0
    nickname.value = ''
    avatar.value = ''

    // 清除 localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('nickname')
    localStorage.removeItem('avatar')
  }

  // 更新头像（编辑资料后调用）
  function updateAvatar(newAvatar: string) {
    avatar.value = newAvatar
    localStorage.setItem('avatar', newAvatar)
  }

  // 更新昵称（编辑资料后调用）
  function updateNickname(newNickname: string) {
    nickname.value = newNickname
    localStorage.setItem('nickname', newNickname)
  }

  return {
    token,
    userId,
    nickname,
    avatar,
    isLoggedIn,
    setUser,
    clearUser,
    updateAvatar,
    updateNickname
  }
})
