<template>
  <div class="min-h-screen bg-bg">
    <!-- 顶部Header -->
    <header class="fixed top-0 left-0 right-0 h-16 bg-white shadow-sm z-50">
      <div class="flex items-center justify-between h-full px-6">
        <!-- 左侧Logo -->
        <div class="flex items-center gap-3 cursor-pointer" @click="router.push('/')">
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
              placeholder="搜索笔记..."
              class="w-full h-10 pl-4 pr-12 rounded-full border border-gray-200 focus:border-primary focus:outline-none transition-colors"
              @keyup.enter="doSearch"
            />
            <button
              class="absolute right-1 top-1 h-8 w-8 bg-primary rounded-full flex items-center justify-center hover:bg-primary-dark transition-colors"
              @click="doSearch"
            >
              <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </button>
          </div>
        </div>

        <!-- 右侧返回首页 -->
        <div class="flex items-center gap-4">
          <el-button type="primary" @click="router.push('/')">
            <el-icon class="mr-1"><House /></el-icon>
            首页
          </el-button>
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
        <!-- 搜索结果信息 -->
        <div class="mb-6">
          <h2 class="text-lg font-bold text-gray-900">
            搜索：<span class="text-primary">"{{ keyword }}"</span>
          </h2>
          <p v-if="total > 0" class="text-sm text-gray-500 mt-1">共找到 {{ total }} 条结果</p>
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
          v-if="!loading && searched && posts.length === 0"
          class="flex flex-col items-center justify-center py-20"
        >
          <el-icon :size="64" class="text-gray-300 mb-4"><Search /></el-icon>
          <p class="text-gray-400">暂无搜索结果</p>
          <p class="text-gray-300 text-sm mt-2">换个关键词试试吧</p>
        </div>
      </main>
    </div>

    <!-- 详情弹窗 -->
    <PostDetailModal
      v-model:visible="showDetail"
      :post="currentPost"
      @like="handleDetailLike"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, House, Bell, User, Edit, Search } from '@element-plus/icons-vue'
import { searchPosts, likePost, type Post } from '@/api/posts'
import { useUserStore } from '@/stores/user'
import PostDetailModal from './PostDetailModal.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 菜单项
const menuItems = [
  { key: 'discover', label: '发现', icon: House },
  { key: 'publish', label: '发布', icon: Edit },
  { key: 'notification', label: '通知', icon: Bell },
  { key: 'profile', label: '我', icon: User }
]

const activeMenu = ref('')

const handleMenuClick = (item: { key: string }) => {
  if (item.key === 'discover') {
    router.push('/')
  } else if (item.key === 'profile') {
    if (userStore.userId) {
      router.push(`/profile/${userStore.userId}`)
    }
  } else if (item.key === 'notification') {
    router.push('/notifications')
  }
}

// 搜索状态
const keyword = ref('')
const searchQuery = ref('')
const posts = ref<Post[]>([])
const loading = ref(false)
const searched = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)

// 随机高度（瀑布流效果）
const heightPool = ['180px', '200px', '220px', '240px', '260px', '280px']
const postHeightMap = new Map<number, string>()
const getPostHeight = (id: number) => {
  if (!postHeightMap.has(id)) {
    postHeightMap.set(id, heightPool[Math.floor(Math.random() * heightPool.length)])
  }
  return postHeightMap.get(id)!
}

// 详情弹窗
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

const handlePostClick = (post: Post) => {
  currentPost.value = post
  showDetail.value = true
}

const handleDetailLike = (post: Post) => {
  const index = posts.value.findIndex(p => p.id === post.id)
  if (index !== -1) {
    posts.value[index].liked = post.liked
    posts.value[index].likeCount = post.likeCount
  }
}

// 执行搜索
const doSearch = () => {
  const kw = searchQuery.value.trim()
  if (!kw) return
  // 更新 URL 参数，触发 watch
  router.push({ path: '/search', query: { keyword: kw } })
}

// 获取搜索结果
const fetchResults = async (isRefresh = false) => {
  if (loading.value) return

  if (isRefresh) {
    page.value = 1
    posts.value = []
    searched.value = false
  }

  loading.value = true

  try {
    const data = await searchPosts({
      keyword: keyword.value,
      current: page.value,
      size: pageSize
    })
    total.value = data.total
    if (isRefresh) {
      posts.value = data.list
    } else {
      posts.value.push(...data.list)
    }
    page.value++
    searched.value = true
  } catch (error) {
    console.error('搜索失败:', error)
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

// 滚动加载
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchResults()
  }
}

// URL keyword 变化时重新搜索
watch(
  () => route.query.keyword,
  (newKeyword) => {
    if (newKeyword && typeof newKeyword === 'string') {
      keyword.value = newKeyword
      searchQuery.value = newKeyword
      fetchResults(true)
    }
  },
  { immediate: true }
)

onMounted(() => {
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
