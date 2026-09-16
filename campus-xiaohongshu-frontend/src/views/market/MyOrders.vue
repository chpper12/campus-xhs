<template>
  <div class="min-h-screen bg-bg">
    <!-- 顶部Header -->
    <header class="fixed top-0 left-0 right-0 h-16 bg-white shadow-sm z-50">
      <div class="flex items-center justify-between h-full px-6">
        <!-- 左侧Logo -->
        <div class="flex items-center gap-3 cursor-pointer" @click="router.push('/market')">
          <div class="px-4 py-1.5 bg-primary rounded-full flex items-center justify-center">
            <span class="text-white font-bold text-sm tracking-wider">小红薯</span>
          </div>
          <span class="text-xl font-bold text-gray-900">校园市集</span>
        </div>

        <!-- 右侧返回 -->
        <button
          class="flex items-center gap-1 text-sm text-gray-600 hover:text-primary transition-colors"
          @click="router.push('/market')"
        >
          <el-icon :size="16"><ArrowLeft /></el-icon>
          返回市集
        </button>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="pt-16 flex">
      <!-- 左侧固定侧边栏 -->
      <AppSidebar />

      <!-- 右侧主内容区 -->
      <main class="ml-60 flex-1 p-6">
        <div class="max-w-2xl mx-auto">
          <!-- 页面标题 -->
          <div class="mb-4">
            <h1 class="text-2xl font-bold text-gray-900">我的订单</h1>
          </div>

          <!-- Tabs：我买到的 / 我卖出的 -->
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="我买到的" name="buyer" />
            <el-tab-pane label="我卖出的" name="seller" />
          </el-tabs>

          <!-- 首次加载骨架屏 -->
          <div v-if="currentState.loading && currentState.list.length === 0" class="space-y-3">
            <el-skeleton
              v-for="i in 3"
              :key="i"
              class="p-4 bg-white rounded-xl"
              animated
            >
              <template #template>
                <div class="flex gap-4">
                  <el-skeleton-item variant="image" style="width: 80px; height: 80px; border-radius: 8px" />
                  <div class="flex-1">
                    <el-skeleton-item variant="h3" style="width: 60%" />
                    <el-skeleton-item variant="text" style="width: 40%; margin-top: 12px" />
                    <el-skeleton-item variant="text" style="width: 30%; margin-top: 12px" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </div>

          <!-- 订单列表 -->
          <div v-else-if="currentState.list.length > 0" class="space-y-3">
            <div
              v-for="order in currentState.list"
              :key="order.id"
              class="bg-white p-4 rounded-xl hover:shadow-md transition-shadow"
            >
              <div class="flex gap-4">
                <!-- 商品封面 -->
                <img
                  :src="order.itemCoverUrl || 'https://picsum.photos/160/160'"
                  :alt="order.itemTitle"
                  class="w-20 h-20 rounded-lg object-cover flex-shrink-0 cursor-pointer"
                  @click="router.push(`/market/item/${order.itemId}`)"
                />

                <!-- 商品信息 -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-start justify-between gap-2">
                    <h3
                      class="text-sm font-medium text-gray-900 line-clamp-1 cursor-pointer hover:text-primary transition-colors"
                      @click="router.push(`/market/item/${order.itemId}`)"
                    >
                      {{ order.itemTitle }}
                    </h3>
                    <!-- 状态 Tag: 0-待支付(warning) 1-已完成(success) 2-已取消(info) -->
                    <el-tag
                      :type="statusTagType(order.status)"
                      size="small"
                      class="flex-shrink-0"
                    >
                      {{ statusText(order.status) }}
                    </el-tag>
                  </div>

                  <p class="text-xs text-gray-400 mt-1">订单号：{{ order.orderSn }}</p>
                  <p class="text-xs text-gray-400 mt-1">
                    {{ activeTab === 'buyer' ? '卖家' : '买家' }}：{{ order.counterparty?.nickname || '-' }}
                  </p>
                  <p class="text-xs text-gray-400 mt-1">{{ formatTime(order.createTime) }}</p>
                </div>

                <!-- 金额 + 操作 -->
                <div class="flex flex-col items-end justify-between flex-shrink-0">
                  <span class="text-base font-bold text-primary">¥{{ formatPrice(order.amount) }}</span>

                  <!-- 买家视角 + 待支付：去支付 / 取消订单 -->
                  <div v-if="activeTab === 'buyer' && order.status === 0" class="flex gap-2">
                    <el-button
                      size="small"
                      :loading="cancelingOrderSn === order.orderSn"
                      :disabled="payingOrderSn === order.orderSn"
                      @click="handleCancel(order)"
                    >
                      取消订单
                    </el-button>
                    <el-button
                      type="primary"
                      size="small"
                      :loading="payingOrderSn === order.orderSn"
                      :disabled="cancelingOrderSn === order.orderSn"
                      @click="handlePay(order)"
                    >
                      去支付
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <el-empty
            v-else
            :description="activeTab === 'buyer' ? '还没有买过东西' : '还没有卖出过东西'"
          />

          <!-- 加载更多 -->
          <div v-if="currentState.loading && currentState.list.length > 0" class="flex justify-center py-6">
            <el-icon class="is-loading text-primary" :size="24"><Loading /></el-icon>
          </div>

          <!-- 加载完成 -->
          <div
            v-if="!currentState.loading && currentState.list.length > 0 && currentState.list.length >= currentState.total"
            class="text-center py-6 text-gray-400 text-sm"
          >
            没有更多订单了
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, ArrowLeft } from '@element-plus/icons-vue'
import { getMyMarketOrders, payMarketOrder, cancelMarketOrder } from '@/api/market'
import type { MarketOrderVO, MarketOrderStatus, OrderQueryType } from '@/types/market'
import AppSidebar from '@/components/AppSidebar.vue'

const router = useRouter()

// Tab 类型：后端真实取值为 "buyer" / "seller"
type TabName = OrderQueryType

// 每个 Tab 各自维护列表与分页状态
interface TabState {
  list: MarketOrderVO[]
  pageNum: number
  total: number
  loading: boolean
}

const createTabState = (): TabState => ({
  list: [],
  pageNum: 1,
  total: 0,
  loading: false
})

const tabStates = reactive<Record<TabName, TabState>>({
  buyer: createTabState(),
  seller: createTabState()
})

const activeTab = ref<TabName>('buyer')
const pageSize = 10

// 操作中的订单号（防止重复点击）
const payingOrderSn = ref('')
const cancelingOrderSn = ref('')

// 当前 Tab 的状态
const currentState = computed(() => tabStates[activeTab.value])

// 订单状态文案: 0-待支付, 1-已完成, 2-已取消
const statusText = (status: MarketOrderStatus) => {
  switch (status) {
    case 0: return '待支付'
    case 1: return '已完成'
    case 2: return '已取消'
    default: return ''
  }
}

// 状态 Tag 类型: 0-warning, 1-success, 2-info
const statusTagType = (status: MarketOrderStatus): 'warning' | 'success' | 'info' => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'info'
    default: return 'info'
  }
}

// 价格格式化（两位小数）
const formatPrice = (price: number) => price.toFixed(2)

// 时间格式化：ISO 字符串 → yyyy-MM-dd HH:mm
const formatTime = (time: string) => time ? time.replace('T', ' ').slice(0, 16) : ''

// 获取当前 Tab 的订单列表
const fetchOrders = async (type: TabName, isRefresh = false) => {
  const state = tabStates[type]
  if (state.loading) return
  if (!isRefresh && state.list.length >= state.total && state.total > 0) return

  if (isRefresh) {
    state.pageNum = 1
    state.list = []
    state.total = 0
  }

  state.loading = true

  try {
    const data = await getMyMarketOrders({
      pageNum: state.pageNum,
      pageSize,
      type
    })
    if (isRefresh) {
      state.list = data.records
    } else {
      state.list.push(...data.records)
    }
    state.total = data.total
    state.pageNum++
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    state.loading = false
  }
}

// 切换 Tab：重新请求该 Tab 数据
const handleTabChange = (name: string | number) => {
  fetchOrders(name as TabName, true)
}

// 去支付（模拟支付）
const handlePay = async (order: MarketOrderVO) => {
  payingOrderSn.value = order.orderSn
  try {
    await payMarketOrder(order.orderSn)
    ElMessage.success('支付成功')
    fetchOrders(activeTab.value, true)
  } catch (error) {
    console.error('支付失败:', error)
    fetchOrders(activeTab.value, true)
  } finally {
    payingOrderSn.value = ''
  }
}

// 取消订单（二次确认）
const handleCancel = async (order: MarketOrderVO) => {
  try {
    await ElMessageBox.confirm(
      `确定取消订单 ${order.orderSn} 吗？取消后商品将重新上架。`,
      '取消订单',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )
  } catch {
    // 用户点了"再想想"，不做任何操作
    return
  }

  cancelingOrderSn.value = order.orderSn
  try {
    await cancelMarketOrder(order.orderSn)
    ElMessage.success('订单已取消')
    fetchOrders(activeTab.value, true)
  } catch (error) {
    console.error('取消订单失败:', error)
    fetchOrders(activeTab.value, true)
  } finally {
    cancelingOrderSn.value = ''
  }
}

// 滚动加载
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchOrders(activeTab.value)
  }
}

onMounted(() => {
  fetchOrders(activeTab.value, true)
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
