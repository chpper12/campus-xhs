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

        <!-- 返回按钮 -->
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
        <!-- 加载中 -->
        <div v-if="loading" class="flex justify-center py-20">
          <el-icon class="is-loading text-primary" :size="32"><Loading /></el-icon>
        </div>

        <!-- 商品详情 -->
        <div v-else-if="item" class="max-w-4xl mx-auto bg-white rounded-xl shadow-sm overflow-hidden">
          <div class="flex gap-6 p-6">
            <!-- 左侧图片区 -->
            <div class="w-96 shrink-0">
              <img
                :src="item.coverUrl || 'https://picsum.photos/400/300'"
                :alt="item.title"
                class="w-full h-72 object-cover rounded-xl"
              />
              <!-- 详情图（imageUrls 为 JSON 数组字符串，需 parse） -->
              <div v-if="detailImages.length > 0" class="grid grid-cols-3 gap-2 mt-2">
                <img
                  v-for="(url, index) in detailImages"
                  :key="index"
                  :src="url"
                  :alt="`详情图${index + 1}`"
                  class="w-full h-24 object-cover rounded-lg cursor-pointer hover:opacity-80 transition-opacity"
                  @click="previewImage(url)"
                />
              </div>
            </div>

            <!-- 右侧信息区 -->
            <div class="flex-1 flex flex-col">
              <div class="flex items-start justify-between gap-4">
                <h1 class="text-xl font-bold text-gray-900">{{ item.title }}</h1>
                <span
                  :class="[
                    'shrink-0 px-3 py-1 rounded-full text-xs font-medium',
                    statusClass(item.status)
                  ]"
                >
                  {{ statusText(item.status) }}
                </span>
              </div>

              <div class="mt-4">
                <span class="text-3xl font-bold text-primary">¥{{ formatPrice(item.price) }}</span>
              </div>

              <div class="mt-4 text-sm text-gray-500 space-y-2">
                <p>发布时间：{{ formatTime(item.createTime) }}</p>
                <div class="flex items-center gap-2">
                  <span>卖家：</span>
                  <div
                    class="flex items-center gap-2 cursor-pointer"
                    @click="item.seller && router.push(`/profile/${item.seller.userId}`)"
                  >
                    <img
                      :src="item.seller?.avatar || 'https://picsum.photos/100/100'"
                      :alt="item.seller?.nickname"
                      class="w-6 h-6 rounded-full object-cover hover:ring-2 hover:ring-primary/30 transition-all"
                    />
                    <span class="text-sm text-gray-700 hover:text-primary transition-colors">
                      {{ item.seller?.nickname || '-' }}
                    </span>
                  </div>
                </div>
              </div>

              <div v-if="item.description" class="mt-4 pt-4 border-t border-gray-100">
                <h2 class="text-sm font-medium text-gray-900 mb-2">商品描述</h2>
                <p class="text-sm text-gray-600 whitespace-pre-wrap">{{ item.description }}</p>
              </div>

              <div class="mt-auto pt-6">
                <!-- 卖家本人 + status=0 待售：显示下架按钮 -->
                <div v-if="isSeller && item.status === 0" class="flex gap-3">
                  <el-button
                    size="large"
                    class="flex-1"
                    :loading="offShelfing"
                    :disabled="buying"
                    @click="handleOffShelf"
                  >
                    下架商品
                  </el-button>
                  <el-button
                    type="info"
                    size="large"
                    class="flex-1"
                    disabled
                  >
                    自己发布的商品，不可购买
                  </el-button>
                </div>
                <!-- 非卖家：status=0 待售可购买；其他状态禁用 -->
                <el-button
                  v-else
                  type="primary"
                  size="large"
                  class="w-full"
                  :disabled="item.status !== 0"
                  :loading="buying"
                  @click="handleBuy"
                >
                  {{ buyButtonText(item.status) }}
                </el-button>
              </div>
            </div>
          </div>

          <!-- 联系方式（购买后可见更符合业务，这里直接展示） -->
          <div class="px-6 py-4 bg-gray-50 border-t border-gray-100">
            <span class="text-sm text-gray-500">联系方式：</span>
            <span class="text-sm font-medium text-gray-900">{{ item.contactInfo }}</span>
          </div>
        </div>

        <!-- 商品不存在 -->
        <div v-else class="flex flex-col items-center justify-center py-20">
          <el-icon :size="64" class="text-gray-300 mb-4"><ShoppingBag /></el-icon>
          <p class="text-gray-400">商品不存在或已被删除</p>
        </div>
      </main>
    </div>

    <!-- 图片预览 -->
    <el-dialog v-model="showPreview" width="60%" align-center>
      <img :src="previewUrl" alt="预览" class="w-full rounded-xl" />
    </el-dialog>

    <!-- 下单成功弹窗：orderSn + 15 分钟倒计时 + 支付/取消 -->
    <el-dialog
      v-model="showOrderDialog"
      title="下单成功"
      width="420px"
      align-center
      :close-on-click-modal="false"
      @closed="handleOrderDialogClosed"
    >
      <div class="flex flex-col items-center py-2">
        <el-icon :size="48" class="text-primary mb-3"><CircleCheckFilled /></el-icon>
        <p class="text-sm text-gray-500 mb-1">订单号</p>
        <p class="text-base font-bold text-gray-900 break-all text-center">{{ currentOrderSn }}</p>

        <div class="mt-4 px-4 py-2 bg-red-50 rounded-full">
          <span class="text-sm text-gray-600">支付剩余时间 </span>
          <span :class="['text-base font-bold', countdownSeconds <= 60 ? 'text-red-500' : 'text-primary']">
            {{ countdownText }}
          </span>
        </div>
        <p class="mt-2 text-xs text-gray-400">超时未支付订单将自动取消，商品重新上架</p>
      </div>

      <template #footer>
        <div class="flex gap-3">
          <el-button
            class="flex-1"
            :disabled="paying || canceling"
            :loading="canceling"
            @click="handleCancelOrder"
          >
            取消订单
          </el-button>
          <el-button
            type="primary"
            class="flex-1"
            :disabled="canceling"
            :loading="paying"
            @click="handlePayOrder"
          >
            模拟支付
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Loading,
  ShoppingBag,
  ArrowLeft,
  CircleCheckFilled
} from '@element-plus/icons-vue'
import {
  getMarketItemById,
  createMarketOrder,
  payMarketOrder,
  cancelMarketOrder,
  offShelfMarketItem
} from '@/api/market'
import type { MarketItemVO, MarketItemStatus } from '@/types/market'
import { useUserStore } from '@/stores/user'
import AppSidebar from '@/components/AppSidebar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 商品详情
const item = ref<MarketItemVO | null>(null)
const loading = ref(false)
const buying = ref(false)
const offShelfing = ref(false)

// 当前登录用户是否为该商品卖家
const isSeller = computed(() =>
  !!item.value?.seller && item.value.seller.userId === userStore.userId
)

// 图片预览
const showPreview = ref(false)
const previewUrl = ref('')

// 订单弹窗
const showOrderDialog = ref(false)
const currentOrderSn = ref('')
const paying = ref(false)
const canceling = ref(false)

// 15 分钟支付倒计时（秒）
const PAY_TIMEOUT_SECONDS = 15 * 60
const countdownSeconds = ref(PAY_TIMEOUT_SECONDS)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 详情图：imageUrls 是 JSON 数组格式的字符串，需 parse
const detailImages = computed<string[]>(() => {
  if (!item.value?.imageUrls) return []
  try {
    const parsed = JSON.parse(item.value.imageUrls)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

// 倒计时展示 mm:ss
const countdownText = computed(() => {
  const m = Math.floor(countdownSeconds.value / 60)
  const s = countdownSeconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

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

const statusClass = (status: MarketItemStatus) => {
  switch (status) {
    case 0: return 'bg-green-50 text-green-600'
    case 1: return 'bg-orange-50 text-orange-500'
    case 2: return 'bg-gray-100 text-gray-500'
    case 3: return 'bg-gray-100 text-gray-400'
    default: return 'bg-gray-100 text-gray-500'
  }
}

const buyButtonText = (status: MarketItemStatus) => {
  switch (status) {
    case 0: return '立即购买'
    case 1: return '锁定中（有人已下单）'
    case 2: return '已售出'
    case 3: return '已下架'
    default: return '立即购买'
  }
}

// 价格格式化（两位小数）
const formatPrice = (price: number) => price.toFixed(2)

// 时间格式化：ISO 字符串 → yyyy-MM-dd HH:mm
const formatTime = (time: string) => time ? time.replace('T', ' ').slice(0, 16) : ''

// 获取商品详情
const fetchItem = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    item.value = await getMarketItemById(id)
  } catch (error) {
    console.error('获取商品详情失败:', error)
    item.value = null
  } finally {
    loading.value = false
  }
}

// 图片预览
const previewImage = (url: string) => {
  previewUrl.value = url
  showPreview.value = true
}

// 倒计时控制
const startCountdown = () => {
  stopCountdown()
  countdownSeconds.value = PAY_TIMEOUT_SECONDS
  countdownTimer = setInterval(() => {
    countdownSeconds.value--
    if (countdownSeconds.value <= 0) {
      stopCountdown()
      // 倒计时结束：后端 MQ 会自动取消订单，前端关闭弹窗并刷新商品状态
      ElMessage.warning('支付超时，订单已自动取消')
      showOrderDialog.value = false
      fetchItem()
    }
  }, 1000)
}

const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

// 购买 → 创建订单 → 弹窗显示 orderSn + 倒计时
const handleBuy = async () => {
  if (!item.value || item.value.status !== 0) return

  buying.value = true
  try {
    const orderSn = await createMarketOrder({ itemId: item.value.id })
    currentOrderSn.value = orderSn
    showOrderDialog.value = true
    startCountdown()
    // 商品进入锁定中
    item.value.status = 1
  } catch (error) {
    // 并发失败（商品被抢/已下架）等：错误提示由拦截器统一弹出，这里刷新最新状态
    console.error('创建订单失败:', error)
    fetchItem()
  } finally {
    buying.value = false
  }
}

// 下架商品（仅卖家本人，status=0 时可见）
const handleOffShelf = async () => {
  if (!item.value) return

  try {
    await ElMessageBox.confirm(
      '确定下架该商品吗？下架后买家将无法购买。',
      '下架商品',
      {
        confirmButtonText: '确定下架',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )
  } catch {
    // 用户点了"再想想"，不做任何操作
    return
  }

  offShelfing.value = true
  try {
    await offShelfMarketItem(item.value.id)
    ElMessage.success('商品已下架')
    fetchItem()
  } catch (error) {
    console.error('下架商品失败:', error)
  } finally {
    offShelfing.value = false
  }
}

// 模拟支付
const handlePayOrder = async () => {
  if (!currentOrderSn.value) return

  paying.value = true
  try {
    await payMarketOrder(currentOrderSn.value)
    ElMessage.success('支付成功')
    showOrderDialog.value = false
    fetchItem()
  } catch (error) {
    console.error('支付失败:', error)
    fetchItem()
  } finally {
    paying.value = false
  }
}

// 取消订单
const handleCancelOrder = async () => {
  if (!currentOrderSn.value) return

  canceling.value = true
  try {
    await cancelMarketOrder(currentOrderSn.value)
    ElMessage.success('订单已取消')
    showOrderDialog.value = false
    fetchItem()
  } catch (error) {
    console.error('取消订单失败:', error)
    fetchItem()
  } finally {
    canceling.value = false
  }
}

// 弹窗关闭（含右上角 X）：停止倒计时，订单仍可在"我的订单"中支付/取消
const handleOrderDialogClosed = () => {
  stopCountdown()
  currentOrderSn.value = ''
}

onMounted(() => {
  fetchItem()
})

onUnmounted(() => {
  stopCountdown()
})
</script>

<style scoped>
/* 保持与 MarketList.vue 一致的极简风格，无额外样式 */
</style>
