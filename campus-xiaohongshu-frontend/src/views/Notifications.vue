<template>
  <div class="min-h-screen bg-bg">
    <!-- 顶部Header -->
    <header class="fixed top-0 left-0 right-0 h-16 bg-white shadow-sm z-50">
      <div class="flex items-center justify-between h-full px-6">
        <!-- 左侧Logo -->
        <div class="flex items-center gap-3">
          <div class="px-4 py-1.5 bg-primary rounded-full flex items-center justify-center">
            <span class="text-white font-bold text-sm tracking-wider">小红薯</span>
          </div>
          <span class="text-xl font-bold text-gray-900">校园小红书</span>
        </div>

        <!-- 中间搜索框 -->
        <div class="flex-1 max-w-xl mx-8">
          <div class="relative">
            <input
              type="text"
              placeholder="搜索笔记、用户..."
              class="w-full h-10 pl-4 pr-12 rounded-full border border-gray-200 focus:border-primary focus:outline-none transition-colors"
            />
            <button class="absolute right-1 top-1 h-8 w-8 bg-primary rounded-full flex items-center justify-center hover:bg-primary-dark transition-colors">
              <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </button>
          </div>
        </div>

        <!-- 右侧用户头像 -->
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 rounded-full bg-gray-200 overflow-hidden cursor-pointer">
            <img
              :src="currentUser?.avatar || 'https://picsum.photos/100/100'"
              alt="用户头像"
              class="w-full h-full object-cover"
            />
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="pt-16 flex">
      <!-- 左侧固定侧边栏 -->
      <aside class="fixed left-0 top-16 bottom-0 w-60 bg-white border-r border-gray-100 overflow-y-auto">
        <nav class="py-4">
          <div
            v-for="item in menuItems"
            :key="item.key"
            :class="[
              'flex items-center gap-3 px-6 py-3 cursor-pointer transition-colors',
              activeMenu === item.key
                ? 'bg-red-50 text-primary border-r-2 border-primary'
                : 'text-gray-600 hover:bg-gray-50'
            ]"
            @click="handleMenuClick(item)"
          >
            <el-icon :size="20">
              <component :is="item.icon" />
            </el-icon>
            <span class="font-medium">{{ item.label }}</span>
          </div>
        </nav>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="ml-60 flex-1 p-6">
        <div class="max-w-2xl mx-auto">
          <!-- 页面标题 -->
          <div class="flex items-center justify-between mb-6">
            <h1 class="text-2xl font-bold text-gray-900">通知</h1>
            <el-button
              v-if="notifications.some(n => !n.isRead)"
              type="primary"
              link
              @click="markAllAsRead"
            >
              全部已读
            </el-button>
          </div>

          <!-- 通知列表 -->
          <div v-if="notifications.length > 0" class="space-y-2">
            <div
              v-for="notice in notifications"
              :key="notice.id"
              :class="[
                'flex items-start gap-4 p-4 rounded-xl cursor-pointer transition-all',
                notice.isRead
                  ? 'bg-white hover:bg-gray-50'
                  : 'bg-red-50/50 hover:bg-red-50 border-l-4 border-primary'
              ]"
              @click="handleNoticeClick(notice)"
            >
              <!-- 未读红点 -->
              <div v-if="!notice.isRead" class="absolute left-2 top-1/2 -translate-y-1/2 w-2 h-2 bg-primary rounded-full" />

              <!-- 通知类型图标 -->
              <div
                :class="[
                  'flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center',
                  getNoticeIconBg(notice.type)
                ]"
              >
                <el-icon :size="18" :class="getNoticeIconColor(notice.type)">
                  <component :is="getNoticeIcon(notice.type)" />
                </el-icon>
              </div>

              <!-- 通知内容 -->
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-2">
                  <div class="flex-1">
                    <!-- 发送者信息 -->
                    <div class="flex items-center gap-2 mb-1">
                      <img
                        :src="notice.fromUser.avatar"
                        :alt="notice.fromUser.nickname"
                        class="w-6 h-6 rounded-full object-cover"
                      />
                      <span class="font-medium text-gray-900 text-sm">
                        {{ notice.fromUser.nickname }}
                      </span>
                    </div>
                    <!-- 通知内容 -->
                    <p class="text-sm text-gray-600 line-clamp-2">
                      {{ notice.content }}
                    </p>
                  </div>
                  <!-- 时间 -->
                  <span class="text-xs text-gray-400 flex-shrink-0">
                    {{ formatTime(notice.createTime) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 加载更多 -->
          <div v-if="loading" class="flex justify-center py-8">
            <el-icon class="is-loading text-primary" :size="24"><Loading /></el-icon>
          </div>

          <!-- 加载完成 -->
          <div v-if="!loading && finished && notifications.length > 0" class="text-center py-6 text-gray-400 text-sm">
            没有更多通知了
          </div>

          <!-- 空状态 -->
          <div v-if="!loading && notifications.length === 0" class="flex flex-col items-center justify-center py-20">
            <el-icon :size="64" class="text-gray-300 mb-4"><Bell /></el-icon>
            <p class="text-gray-400">暂无通知</p>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell,
  House,
  Edit,
  User,
  Loading,
  Star,
  ChatDotRound,
  Connection
} from '@element-plus/icons-vue'
import { getNotifications, markAsRead, type NoticeVO, type NoticeType } from '@/api/notifications'
import { getUserProfile, type UserProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 菜单项
const menuItems = [
  { key: 'discover', label: '发现', icon: House },
  { key: 'publish', label: '发布', icon: Edit },
  { key: 'notification', label: '通知', icon: Bell },
  { key: 'profile', label: '我', icon: User }
]

const activeMenu = ref('notification')
const currentUser = ref<UserProfile | null>(null)

// 通知列表状态
const notifications = ref<NoticeVO[]>([])
const loading = ref(false)
const finished = false
const currentPage = ref(1)
const pageSize = 10

// 获取通知图标
const getNoticeIcon = (type: NoticeType) => {
  const iconMap: Record<NoticeType, typeof Star> = {
    like: Star,
    comment: ChatDotRound,
    follow: Connection
  }
  return iconMap[type] || Bell
}

// 获取通知图标背景色
const getNoticeIconBg = (type: NoticeType) => {
  const bgMap: Record<NoticeType, string> = {
    like: 'bg-red-100',
    comment: 'bg-blue-100',
    follow: 'bg-green-100'
  }
  return bgMap[type] || 'bg-gray-100'
}

// 获取通知图标颜色
const getNoticeIconColor = (type: NoticeType) => {
  const colorMap: Record<NoticeType, string> = {
    like: 'text-primary',
    comment: 'text-blue-500',
    follow: 'text-green-500'
  }
  return colorMap[type] || 'text-gray-500'
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 获取通知列表
const fetchNotifications = async (isRefresh = false) => {
  if (loading.value) return

  if (isRefresh) {
    currentPage.value = 1
    notifications.value = []
  }

  loading.value = true

  try {
    const data = await getNotifications({
      current: currentPage.value,
      size: pageSize
    })

    if (isRefresh) {
      notifications.value = data.list
    } else {
      notifications.value.push(...data.list)
    }

    // 判断是否加载完成
    if (notifications.value.length >= data.total) {
      // 已加载全部
    } else {
      currentPage.value++
    }
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
  }
}

// 点击通知
const handleNoticeClick = async (notice: NoticeVO) => {
  // 标记为已读
  if (!notice.isRead) {
    try {
      await markAsRead(notice.id)
      notice.isRead = true
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 根据通知类型跳转
  if (notice.type === 'like' || notice.type === 'comment') {
    // 点赞/评论通知跳转到笔记详情（这里返回首页，实际可弹窗或跳详情页）
    router.push('/')
  }
  // 关注通知不跳转
}

// 全部已读
const markAllAsRead = async () => {
  const unreadNotices = notifications.value.filter(n => !n.isRead)
  try {
    await Promise.all(unreadNotices.map(n => markAsRead(n.id)))
    unreadNotices.forEach(n => (n.isRead = true))
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('标记全部已读失败:', error)
  }
}

// 侧边栏点击
const handleMenuClick = (item: { key: string }) => {
  activeMenu.value = item.key
  if (item.key === 'discover') {
    router.push('/')
  } else if (item.key === 'profile') {
    if (userStore.userId) {
      router.push(`/profile/${userStore.userId}`)
    }
  }
}

// 获取当前用户信息
const fetchCurrentUser = async () => {
  if (!userStore.userId) return
  try {
    currentUser.value = await getUserProfile(userStore.userId)
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 滚动加载
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchNotifications()
  }
}

onMounted(() => {
  fetchCurrentUser()
  fetchNotifications(true)
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 自定义滚动条 */
aside::-webkit-scrollbar {
  width: 4px;
}

aside::-webkit-scrollbar-track {
  background: transparent;
}

aside::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 2px;
}

aside::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}
</style>
