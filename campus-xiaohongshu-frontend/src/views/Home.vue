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
              v-model="searchQuery"
              type="text"
              placeholder="搜索笔记、用户..."
              class="w-full h-10 pl-4 pr-12 rounded-full border border-gray-200 focus:border-primary focus:outline-none transition-colors"
              @keyup.enter="handleSearch"
            />
            <button
              class="absolute right-1 top-1 h-8 w-8 bg-primary rounded-full flex items-center justify-center hover:bg-primary-dark transition-colors"
              @click="handleSearch"
            >
              <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </button>
          </div>
        </div>

        <!-- 右侧用户头像 -->
        <div class="flex items-center gap-4">
          <el-button type="primary" @click="showPublish = true">
            <el-icon class="mr-1"><Plus /></el-icon>
            发布
          </el-button>
          <div class="relative" ref="avatarDropdownRef">
            <div
              class="w-10 h-10 rounded-full bg-gray-200 overflow-hidden cursor-pointer hover:ring-2 hover:ring-primary/30 transition-all"
              @click="toggleDropdown"
            >
              <img
                :src="currentUser?.avatar || 'https://picsum.photos/100/100'"
                alt="用户头像"
                class="w-full h-full object-cover"
              />
            </div>
            <!-- 下拉菜单 -->
            <transition name="dropdown">
              <div
                v-if="showDropdown"
                class="absolute right-0 top-12 w-44 bg-white rounded-xl shadow-lg border border-gray-100 py-1.5 z-50"
              >
                <div
                  class="flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 cursor-pointer transition-colors"
                  @click="goToProfile"
                >
                  <el-icon :size="16"><User /></el-icon>
                  个人主页
                </div>
                <div class="mx-3 my-1 border-t border-gray-100" />
                <div
                  class="flex items-center gap-3 px-4 py-2.5 text-sm text-red-500 hover:bg-red-50 cursor-pointer transition-colors"
                  @click="handleLogout"
                >
                  <el-icon :size="16"><SwitchButton /></el-icon>
                  退出登录
                </div>
              </div>
            </transition>
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
        <!-- 分类胶囊按钮 -->
        <div class="mb-6 overflow-x-auto">
          <div class="flex gap-2 pb-2">
            <button
              v-for="category in categories"
              :key="category"
              :class="[
                'px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors',
                activeCategory === category
                  ? 'bg-primary text-white'
                  : 'bg-white text-gray-600 hover:bg-gray-100'
              ]"
              @click="selectCategory(category)"
            >
              {{ category }}
            </button>
          </div>
        </div>

        <!-- 瀑布流 -->
        <div
          v-if="posts.length > 0"
          class="columns-4 gap-4 space-y-4"
        >
          <div
            v-for="post in posts"
            :key="post.id"
            class="break-inside-avoid bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow cursor-pointer"
            @click="handlePostClick(post)"
          >
            <!-- 封面图 -->
            <div class="relative overflow-hidden">
              <img
                :src="post.coverUrl"
                :alt="post.title"
                class="w-full object-cover"
                :style="{ height: getPostHeight(post.id) }"
              />
            </div>

            <!-- 底部信息 -->
            <div class="p-3">
              <h3 class="text-sm font-medium text-gray-900 line-clamp-2 mb-2">
                {{ post.title }}
              </h3>
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <img
                    :src="post.author.avatar"
                    :alt="post.author.nickname"
                    class="w-6 h-6 rounded-full object-cover"
                  />
                  <span class="text-xs text-gray-500">{{ post.author.nickname }}</span>
                </div>
                <button
                  class="flex items-center gap-1 text-gray-400 hover:text-primary transition-colors"
                  @click.stop="handleLike(post)"
                >
                  <svg
                    :class="['w-4 h-4', post.liked ? 'text-primary fill-primary' : '']"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                  </svg>
                  <span class="text-xs">{{ post.likeCount }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div
          v-if="loading"
          class="flex justify-center py-8"
        >
          <el-icon class="is-loading text-primary" :size="24"><Loading /></el-icon>
        </div>

        <!-- 空状态 -->
        <div
          v-if="!loading && posts.length === 0"
          class="flex flex-col items-center justify-center py-20"
        >
          <el-icon :size="64" class="text-gray-300 mb-4"><Document /></el-icon>
          <p class="text-gray-400">暂无笔记</p>
        </div>
      </main>
    </div>

    <!-- 发布弹窗 -->
    <PublishModal v-model:visible="showPublish" @success="handlePublishSuccess" />

    <!-- 详情弹窗 -->
    <PostDetailModal
      v-model:visible="showDetail"
      :post="currentPost"
      @like="handleDetailLike"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Loading, Document, House, Bell, User, Edit, SwitchButton } from '@element-plus/icons-vue'
import { getPosts, likePost, type Post } from '@/api/posts'
import { getUserProfile, type UserProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'
import PublishModal from './PublishModal.vue'
import PostDetailModal from './PostDetailModal.vue'

const router = useRouter()
const userStore = useUserStore()

// 菜单项
const menuItems = [
  { key: 'discover', label: '发现', icon: House },
  { key: 'publish', label: '发布', icon: Edit },
  { key: 'notification', label: '通知', icon: Bell },
  { key: 'profile', label: '我', icon: User }
]

// 侧边栏点击
const handleMenuClick = (item: { key: string }) => {
  activeMenu.value = item.key
  if (item.key === 'publish') {
    showPublish.value = true
  } else if (item.key === 'profile') {
    if (userStore.userId) {
      router.push(`/profile/${userStore.userId}`)
    }
  } else if (item.key === 'notification') {
    router.push('/notifications')
  }
}

// 分类
const categories = [
  '全部', '教育', '穿搭', '美食', '彩妆',
  '影视', '游戏', '职场', '情感', '萌宠'
]

// 状态
const activeMenu = ref('discover')
const activeCategory = ref('全部')
const searchQuery = ref('')
const currentUser = ref<UserProfile | null>(null)
const showDropdown = ref(false)
const avatarDropdownRef = ref<HTMLElement>()
const posts = ref<Post[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = 20
const showPublish = ref(false)
const showDetail = ref(false)
const currentPost = ref<Post>({
  id: 0,
  title: '',
  category: '',
  coverUrl: '',
  likeCount: 0,
  liked: false,
  author: { userId: 0, nickname: '', avatar: '' },
  createTime: ''
})

// 随机高度（瀑布流效果，每张卡片只分配一次）
const heightPool = ['180px', '200px', '220px', '240px', '260px', '280px']
const postHeightMap = new Map<number, string>()
const getPostHeight = (id: number) => {
  if (!postHeightMap.has(id)) {
    postHeightMap.set(id, heightPool[Math.floor(Math.random() * heightPool.length)])
  }
  return postHeightMap.get(id)!
}

// 下拉菜单：切换显示
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

// 下拉菜单：点击外部关闭
const handleClickOutside = (e: MouseEvent) => {
  if (avatarDropdownRef.value && !avatarDropdownRef.value.contains(e.target as Node)) {
    showDropdown.value = false
  }
}

// 下拉菜单：跳转个人主页
const goToProfile = () => {
  if (userStore.userId) {
    router.push(`/profile/${userStore.userId}`)
  }
  showDropdown.value = false
}

// 下拉菜单：退出登录
const handleLogout = () => {
  userStore.clearUser()
  router.push('/login')
}

// 获取当前登录用户信息（头像等）
const fetchCurrentUser = async () => {
  if (!userStore.userId) return
  try {
    currentUser.value = await getUserProfile(userStore.userId)
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 选择分类
const selectCategory = (category: string) => {
  activeCategory.value = category
  fetchPosts(true)
}

// 搜索
const handleSearch = () => {
  const keyword = searchQuery.value.trim()
  if (!keyword) return
  router.push({ path: '/search', query: { keyword } })
}

// 获取笔记列表
const fetchPosts = async (isRefresh = false) => {
  if (loading.value) return

  if (isRefresh) {
    page.value = 1
    posts.value = []
  }

  loading.value = true

  try {
    const params: { current: number; size: number; category?: string } = {
      current: page.value,
      size: pageSize
    }
    // 选「全部」时不传 category，后端返回全部
    if (activeCategory.value !== '全部') {
      params.category = activeCategory.value
    }
    const data = await getPosts(params)
    if (isRefresh) {
      posts.value = data.list
    } else {
      posts.value.push(...data.list)
    }
    page.value++
  } catch (error) {
    console.error('获取笔记失败:', error)
  } finally {
    loading.value = false
  }
}

// 点赞
const handleLike = async (post: Post) => {
  try {
    const result = await likePost(post.id)
    post.liked = result.isLiked
    post.likeCount = result.likeCount
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 点击笔记
const handlePostClick = (post: Post) => {
  currentPost.value = post
  showDetail.value = true
}

// 详情页点赞同步
const handleDetailLike = (post: Post) => {
  const index = posts.value.findIndex(p => p.id === post.id)
  if (index !== -1) {
    posts.value[index].liked = post.liked
    posts.value[index].likeCount = post.likeCount
  }
}

// 发布成功
const handlePublishSuccess = () => {
  fetchPosts(true)
}

// 滚动加载
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchPosts()
  }
}

onMounted(() => {
  fetchCurrentUser()
  fetchPosts()
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside, true)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleClickOutside, true)
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

/* 下拉菜单动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
