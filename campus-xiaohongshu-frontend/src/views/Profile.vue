<template>
  <div class="min-h-screen bg-bg">
    <!-- 顶部 Header -->
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
        <!-- 用户资料卡片 -->
        <div class="bg-white rounded-2xl shadow-sm p-8 mb-6">
          <div class="flex items-start gap-8">
            <!-- 头像 -->
            <div class="w-24 h-24 rounded-full overflow-hidden flex-shrink-0 border-4 border-gray-100">
              <img
                :src="profile.avatar || 'https://picsum.photos/100/100'"
                :alt="profile.nickname"
                class="w-full h-full object-cover"
              />
            </div>

            <!-- 用户信息 -->
            <div class="flex-1">
              <div class="flex items-center gap-4 mb-3">
                <h1 class="text-2xl font-bold text-gray-900">{{ profile.nickname }}</h1>
                <!-- 编辑资料按钮（仅自己主页显示） -->
                <el-button
                  v-if="isMyProfile"
                  type="primary"
                  plain
                  class="rounded-full px-5"
                  @click="showEditProfile = true"
                >
                  <el-icon class="mr-1"><Edit /></el-icon>
                  编辑资料
                </el-button>
                <!-- 关注按钮（仅他人主页显示） -->
                <el-button
                  v-if="!isMyProfile"
                  :type="profile.isFollowed ? 'default' : 'primary'"
                  :class="[
                    'rounded-full px-6',
                    profile.isFollowed ? 'border-gray-300' : ''
                  ]"
                  :loading="followLoading"
                  @click="handleFollowToggle"
                >
                  {{ profile.isFollowed ? '已关注' : '关注' }}
                </el-button>
              </div>

              <!-- bio -->
              <p class="text-gray-500 mb-6 leading-relaxed">
                {{ profile.bio || '这个人很懒，什么都没写~' }}
              </p>

              <!-- 统计数据 -->
              <div class="flex gap-10">
                <div class="text-center cursor-pointer hover:opacity-80 transition-opacity">
                  <div class="text-2xl font-bold text-gray-900">{{ profile.postCount }}</div>
                  <div class="text-sm text-gray-500 mt-1">笔记</div>
                </div>
                <div class="text-center cursor-pointer hover:opacity-80 transition-opacity">
                  <div class="text-2xl font-bold text-gray-900">{{ profile.followingCount }}</div>
                  <div class="text-sm text-gray-500 mt-1">关注</div>
                </div>
                <div class="text-center cursor-pointer hover:opacity-80 transition-opacity">
                  <div class="text-2xl font-bold text-gray-900">{{ profile.followerCount }}</div>
                  <div class="text-sm text-gray-500 mt-1">粉丝</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tab 切换 -->
        <div class="flex gap-1 mb-6 bg-white rounded-xl p-1.5 shadow-sm w-fit">
          <button
            v-for="tab in availableTabs"
            :key="tab.key"
            :class="[
              'px-6 py-2.5 rounded-lg text-sm font-medium transition-all whitespace-nowrap',
              activeTab === tab.key
                ? 'bg-primary text-white shadow-sm'
                : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
            ]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- 笔记瀑布流 -->
        <div v-if="displayPosts.length > 0" class="columns-4 gap-4 space-y-4">
          <div
            v-for="post in displayPosts"
            :key="post.id"
            class="group relative break-inside-avoid bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow cursor-pointer"
            @click="handlePostClick(post)"
          >
            <!-- 删除按钮（仅自己的主页 + 笔记 Tab 显示） -->
            <button
              v-if="isMyProfile && activeTab === 'posts'"
              class="absolute top-2 right-2 z-10 w-7 h-7 rounded-full bg-white/80 backdrop-blur-sm flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity text-gray-400 hover:text-red-500 hover:bg-white"
              @click.stop="handleDeletePost(post)"
            >
              <el-icon :size="14"><Delete /></el-icon>
            </button>
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
        <div v-if="loading" class="flex justify-center py-8">
          <el-icon class="is-loading text-primary" :size="24"><Loading /></el-icon>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && displayPosts.length === 0" class="flex flex-col items-center justify-center py-20">
          <el-icon :size="64" class="text-gray-300 mb-4"><Document /></el-icon>
          <p class="text-gray-400">{{ activeTab === 'posts' ? '暂无笔记' : '暂无点赞' }}</p>
        </div>
      </main>
    </div>

    <!-- 详情弹窗 -->
    <PostDetailModal
      v-model:visible="showDetail"
      :post="currentPost"
      @like="handleDetailLike"
    />

    <!-- 编辑资料弹窗 -->
    <EditProfileModal
      v-model:visible="showEditProfile"
      :user-profile="profile"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House, Loading, Document, Bell, User, Edit, Delete } from '@element-plus/icons-vue'
import {
  getUserProfile,
  getUserPosts,
  followUser,
  unfollowUser,
  type UserProfile,
  type UserPost
} from '@/api/user'
import { likePost, getMyLikedPosts, deletePost } from '@/api/posts'
import { useUserStore } from '@/stores/user'
import PostDetailModal from './PostDetailModal.vue'
import EditProfileModal from './EditProfileModal.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// ======================== 侧边栏 ========================
const menuItems = [
  { key: 'discover', label: '发现', icon: House },
  { key: 'publish', label: '发布', icon: Edit },
  { key: 'notification', label: '通知', icon: Bell },
  { key: 'profile', label: '我', icon: User }
]

const activeMenu = ref('profile')

const handleMenuClick = (item: { key: string }) => {
  if (item.key === 'discover') {
    router.push('/')
  } else if (item.key === 'profile') {
    if (userStore.userId) {
      router.push(`/profile/${userStore.userId}`)
    }
  }
}

// ======================== 用户资料 ========================
const profile = ref<UserProfile>({
  userId: 0,
  nickname: '',
  avatar: '',
  bio: '',
  postCount: 0,
  followingCount: 0,
  followerCount: 0,
  isFollowed: null
})

const followLoading = ref(false)

/** 是否是自己的主页 */
const isMyProfile = computed(() => profile.value.isFollowed === null)

const fetchProfile = async (userId: number) => {
  try {
    const data = await getUserProfile(userId)
    profile.value = data
  } catch (error) {
    console.error('获取用户资料失败:', error)
    ElMessage.error('获取用户资料失败')
  }
}

// 关注/取关
const handleFollowToggle = async () => {
  if (!profile.value.userId) return
  followLoading.value = true
  try {
    if (profile.value.isFollowed) {
      await unfollowUser(profile.value.userId)
      profile.value.isFollowed = false
      profile.value.followerCount = Math.max(0, profile.value.followerCount - 1)
      ElMessage.success('已取消关注')
    } else {
      await followUser(profile.value.userId)
      profile.value.isFollowed = true
      profile.value.followerCount += 1
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('关注操作失败:', error)
  } finally {
    followLoading.value = false
  }
}

// ======================== Tab 切换 ========================
type TabKey = 'posts' | 'liked'

interface TabItem {
  key: TabKey
  label: string
}

const tabs: TabItem[] = [
  { key: 'posts', label: '笔记' },
  { key: 'liked', label: '赞过' }
]

/** 可用的 tab：自己的主页显示两个，他人主页只显示「笔记」 */
const availableTabs = computed<TabItem[]>(() => {
  if (isMyProfile.value) {
    return tabs
  }
  return tabs.filter(t => t.key === 'posts')
})

const activeTab = ref<TabKey>('posts')

// ======================== 笔记列表 ========================
const posts = ref<UserPost[]>([])
const likedPosts = ref<UserPost[]>([])
const loading = ref(false)
const page = ref(1)
const likedPage = ref(1)
const pageSize = 20

/** 当前 Tab 对应的列表（模板统一用 displayPosts 渲染） */
const displayPosts = computed(() => activeTab.value === 'liked' ? likedPosts.value : posts.value)

// 随机高度（瀑布流效果）
const heightPool = ['180px', '200px', '220px', '240px', '260px', '280px']
const postHeightMap = new Map<number, string>()
const getPostHeight = (id: number) => {
  if (!postHeightMap.has(id)) {
    postHeightMap.set(id, heightPool[Math.floor(Math.random() * heightPool.length)])
  }
  return postHeightMap.get(id)!
}

const fetchPosts = async (isRefresh = false) => {
  if (loading.value) return
  const userId = Number(route.params.userId)
  if (!userId) return

  if (isRefresh) {
    page.value = 1
    posts.value = []
  }

  loading.value = true
  try {
    const data = await getUserPosts(userId, { current: page.value, size: pageSize })
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

const fetchLikedPosts = async (isRefresh = false) => {
  if (loading.value) return

  if (isRefresh) {
    likedPage.value = 1
    likedPosts.value = []
  }

  loading.value = true
  try {
    const data = await getMyLikedPosts({ current: likedPage.value, size: pageSize })
    if (isRefresh) {
      likedPosts.value = data.list
    } else {
      likedPosts.value.push(...data.list)
    }
    likedPage.value++
  } catch (error) {
    console.error('获取赞过笔记失败:', error)
  } finally {
    loading.value = false
  }
}

// Tab 切换时重新加载
watch(activeTab, (newTab) => {
  if (newTab === 'posts') {
    fetchPosts(true)
  } else if (newTab === 'liked') {
    fetchLikedPosts(true)
  }
})

// ======================== 点赞 ========================
const handleLike = async (post: UserPost) => {
  try {
    const result = await likePost(post.id)
    post.liked = result.isLiked
    post.likeCount = result.likeCount
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// ======================== 删除笔记 ========================
const handleDeletePost = async (post: UserPost) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇笔记吗？删除后无法恢复。', '删除笔记', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
      type: 'warning'
    })
  } catch {
    // 用户点了「取消」，不做任何操作
    return
  }

  try {
    await deletePost(post.id)
    // 从列表中移除
    const idx = posts.value.findIndex(p => p.id === post.id)
    if (idx !== -1) posts.value.splice(idx, 1)
    // 更新笔记数
    profile.value.postCount = Math.max(0, profile.value.postCount - 1)
    ElMessage.success('笔记已删除')
  } catch (error) {
    console.error('删除笔记失败:', error)
    ElMessage.error('删除失败，请重试')
  }
}

// ======================== 编辑资料弹窗 ========================
const showEditProfile = ref(false)

// ======================== 笔记详情弹窗 ========================
const showDetail = ref(false)
const currentPost = ref<UserPost>({
  id: 0,
  title: '',
  category: '',
  coverUrl: '',
  likeCount: 0,
  liked: false,
  author: { userId: 0, nickname: '', avatar: '' },
  createTime: ''
})

const handlePostClick = (post: UserPost) => {
  currentPost.value = post
  showDetail.value = true
}

const handleDetailLike = (post: UserPost) => {
  // 同步到 posts 列表
  const idx1 = posts.value.findIndex(p => p.id === post.id)
  if (idx1 !== -1) {
    posts.value[idx1].liked = post.liked
    posts.value[idx1].likeCount = post.likeCount
  }
  // 同步到 likedPosts 列表
  const idx2 = likedPosts.value.findIndex(p => p.id === post.id)
  if (idx2 !== -1) {
    likedPosts.value[idx2].liked = post.liked
    likedPosts.value[idx2].likeCount = post.likeCount
  }
}

/** 编辑资料成功后刷新用户资料 */
const handleEditSuccess = () => {
  const userId = Number(route.params.userId)
  if (userId) {
    fetchProfile(userId)
  }
}

// ======================== 滚动加载 ========================
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 100) {
    if (activeTab.value === 'liked') {
      fetchLikedPosts()
    } else {
      fetchPosts()
    }
  }
}

// ======================== 路由变化时重新加载 ========================
watch(
  () => route.params.userId,
  (newUserId) => {
    if (newUserId) {
      const uid = Number(newUserId)
      fetchProfile(uid)
      fetchPosts(true)
    }
  }
)

onMounted(() => {
  const userId = Number(route.params.userId)
  if (userId) {
    fetchProfile(userId)
    fetchPosts(true)
  }
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
