<template>
  <!-- 左侧固定侧边栏 -->
  <aside class="fixed left-0 top-16 bottom-0 w-60 bg-white border-r border-gray-100 overflow-y-auto">
    <nav class="py-4">
      <div v-for="item in menuItems" :key="item.key">
        <!-- 一级菜单 -->
        <div
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
          <span class="font-medium flex-1">{{ item.label }}</span>
          <!-- 展开箭头（点击仅切换展开/收起，不跳转） -->
          <el-icon
            v-if="item.children"
            :size="14"
            class="transition-transform hover:text-primary"
            :class="{ 'rotate-90': expandedMenus.includes(item.key) }"
            @click.stop="toggleExpand(item.key)"
          >
            <ArrowRight />
          </el-icon>
        </div>

        <!-- 二级子菜单（展开时显示） -->
        <div v-if="item.children && expandedMenus.includes(item.key)">
          <div
            v-for="child in item.children"
            :key="child.key"
            :class="[
              'pl-14 pr-6 py-2.5 cursor-pointer text-sm transition-colors',
              activeMenu === child.key
                ? 'text-primary font-medium bg-red-50/60'
                : 'text-gray-500 hover:bg-gray-50 hover:text-gray-700'
            ]"
            @click="handleChildClick(child)"
          >
            {{ child.label }}
          </div>
        </div>
      </div>
    </nav>
  </aside>

  <!-- 发布弹窗 -->
  <PublishModal v-model:visible="showPublish" @success="handlePublishSuccess" />
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { House, Edit, Bell, User, ShoppingBag, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import PublishModal from '@/views/PublishModal.vue'

const emit = defineEmits<{
  (e: 'published'): void
}>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 子菜单项
interface MenuChild {
  key: string
  label: string
  path: string
  query?: Record<string, string>
}

// 菜单项（带可选 children，支持展开子菜单）
interface MenuItem {
  key: string
  label: string
  icon: typeof House
  children?: MenuChild[]
}

const menuItems: MenuItem[] = [
  { key: 'discover', label: '发现', icon: House },
  {
    key: 'market',
    label: '校园市集',
    icon: ShoppingBag,
    children: [
      { key: 'market-orders', label: '我的订单', path: '/market/orders' },
      { key: 'market-publish', label: '发布商品', path: '/market', query: { publish: '1' } }
    ]
  },
  { key: 'publish', label: '发布', icon: Edit },
  { key: 'notification', label: '通知', icon: Bell },
  { key: 'profile', label: '我', icon: User }
]

// 展开的子菜单分组（进入 /market* 时默认展开）
const expandedMenus = ref<string[]>(route.path.startsWith('/market') ? ['market'] : [])

// 切换展开/收起（点击 chevron 箭头）
const toggleExpand = (key: string) => {
  const index = expandedMenus.value.indexOf(key)
  if (index === -1) {
    expandedMenus.value.push(key)
  } else {
    expandedMenus.value.splice(index, 1)
  }
}

// 展开分组
const expandMenu = (key: string) => {
  if (!expandedMenus.value.includes(key)) {
    expandedMenus.value.push(key)
  }
}

// 路由进入市集相关页面时自动展开
watch(() => route.path, (path) => {
  if (path.startsWith('/market')) {
    expandMenu('market')
  }
})

// 子菜单点击
const handleChildClick = (child: MenuChild) => {
  router.push({ path: child.path, query: child.query })
}

// 高亮菜单：根据当前路由自动计算
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/') return 'discover'
  if (path === '/market/orders') return 'market-orders'
  if (path.startsWith('/market')) return 'market'
  if (path === '/notifications') return 'notification'
  if (path.startsWith('/profile/')) {
    const routeUserId = Number(route.params.userId)
    if (routeUserId && routeUserId === userStore.userId) return 'profile'
  }
  return ''
})

// 发布弹窗
const showPublish = ref(false)

const openPublish = () => {
  showPublish.value = true
}

// 供父页面调用（如顶部 header 的"发布"按钮）
defineExpose({ openPublish })

// 菜单点击
const handleMenuClick = (item: { key: string }) => {
  switch (item.key) {
    case 'discover':
      router.push('/')
      break
    case 'market':
      router.push('/market')
      expandMenu('market')
      break
    case 'publish':
      showPublish.value = true
      break
    case 'notification':
      router.push('/notifications')
      break
    case 'profile':
      if (userStore.userId) {
        router.push(`/profile/${userStore.userId}`)
      }
      break
  }
}

// 发布成功后通知父页面刷新
const handlePublishSuccess = () => {
  emit('published')
}
</script>

<style scoped>
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
