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
          <span class="text-xl font-bold text-gray-900">校园市集</span>
        </div>

        <!-- 中间搜索框 -->
        <div class="flex-1 max-w-xl mx-8">
          <div class="relative">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索二手好物..."
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

        <!-- 右侧发布商品按钮 -->
        <div class="flex items-center">
          <el-button type="primary" @click="showPublish = true">
            <el-icon class="mr-1"><Plus /></el-icon>
            发布商品
          </el-button>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="pt-16 flex">
      <!-- 左侧固定侧边栏 -->
      <AppSidebar />

      <!-- 右侧主内容区 -->
      <main class="ml-60 flex-1 p-6">
        <!-- 商品网格 -->
        <div
          v-if="items.length > 0"
          class="grid grid-cols-4 gap-4"
        >
          <div
            v-for="item in items"
            :key="item.id"
            class="bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow cursor-pointer"
            @click="handleItemClick(item)"
          >
            <!-- 封面图 -->
            <div class="relative overflow-hidden">
              <img
                :src="item.coverUrl || 'https://picsum.photos/400/300'"
                :alt="item.title"
                class="w-full h-48 object-cover"
              />
              <!-- 状态角标：1-锁定中 2-已售出 3-已下架 -->
              <div
                v-if="item.status !== 0"
                class="absolute inset-0 bg-black/40 flex items-center justify-center"
              >
                <span class="px-3 py-1 bg-white/90 rounded-full text-sm font-medium text-gray-700">
                  {{ statusText(item.status) }}
                </span>
              </div>
            </div>

            <!-- 底部信息 -->
            <div class="p-3">
              <h3 class="text-sm font-medium text-gray-900 line-clamp-2 mb-2">
                {{ item.title }}
              </h3>
              <div class="flex items-center justify-between">
                <span class="text-primary font-bold">¥{{ formatPrice(item.price) }}</span>
                <span v-if="item.status === 0" class="text-xs text-gray-400">在售</span>
              </div>
              <!-- 卖家信息 -->
              <div class="flex items-center gap-2 mt-2">
                <img
                  :src="item.seller?.avatar || 'https://picsum.photos/100/100'"
                  :alt="item.seller?.nickname"
                  class="w-5 h-5 rounded-full object-cover"
                />
                <span class="text-xs text-gray-500 truncate">{{ item.seller?.nickname || '-' }}</span>
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
          v-if="!loading && items.length === 0"
          class="flex flex-col items-center justify-center py-20"
        >
          <el-icon :size="64" class="text-gray-300 mb-4"><ShoppingBag /></el-icon>
          <p class="text-gray-400">暂无商品</p>
        </div>
      </main>
    </div>

    <!-- 发布商品弹窗 -->
    <PublishDialog v-model:visible="showPublish" @success="handlePublishSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading, ShoppingBag, Plus } from '@element-plus/icons-vue'
import { getMarketItems } from '@/api/market'
import type { MarketItemVO, MarketItemStatus } from '@/types/market'
import AppSidebar from '@/components/AppSidebar.vue'
import PublishDialog from './components/PublishDialog.vue'

const route = useRoute()
const router = useRouter()

// 状态
const showPublish = ref(false)
const searchQuery = ref('')
const keyword = ref('')
const items = ref<MarketItemVO[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = 20
const total = ref(0)

// 商品状态文案: 0-待售, 1-锁定中, 2-已售出, 3-已下架
const statusText = (status: MarketItemStatus) => {
  switch (status) {
    case 0: return '待售'
    case 1: return '锁定中'
    case 2: return '已售出'
    case 3: return '已下架'
    default: return ''
  }
}

// 价格格式化（两位小数）
const formatPrice = (price: number) => price.toFixed(2)

// 搜索
const handleSearch = () => {
  keyword.value = searchQuery.value.trim()
  fetchItems(true)
}

// 获取商品列表
const fetchItems = async (isRefresh = false) => {
  if (loading.value) return
  if (!isRefresh && items.value.length >= total.value && total.value > 0) return

  if (isRefresh) {
    pageNum.value = 1
    items.value = []
    total.value = 0
  }

  loading.value = true

  try {
    const data = await getMarketItems({
      pageNum: pageNum.value,
      pageSize,
      keyword: keyword.value || undefined
    })
    if (isRefresh) {
      items.value = data.records
    } else {
      items.value.push(...data.records)
    }
    total.value = data.total
    pageNum.value++
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 点击商品，进入详情
const handleItemClick = (item: MarketItemVO) => {
  router.push(`/market/item/${item.id}`)
}

// 发布成功后刷新列表
const handlePublishSuccess = () => {
  fetchItems(true)
}

// 响应侧边栏"发布商品"子菜单：/market?publish=1 自动打开发布弹窗
const checkPublishQuery = () => {
  if (route.query.publish === '1') {
    showPublish.value = true
    // 清掉 query，保证下次点击"发布商品"（重复路由）仍能触发弹窗
    router.replace({ path: '/market' })
  }
}

watch(() => route.query.publish, () => {
  checkPublishQuery()
})

// 滚动加载
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchItems()
  }
}

onMounted(() => {
  fetchItems(true)
  checkPublishQuery()
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
</style>
