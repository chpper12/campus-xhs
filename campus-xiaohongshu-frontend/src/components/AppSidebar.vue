<template>
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

  <!-- 发布弹窗 -->
  <PublishModal v-model:visible="showPublish" @success="handlePublishSuccess" />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { House, Edit, Bell, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import PublishModal from '@/views/PublishModal.vue'

const emit = defineEmits<{
  (e: 'published'): void
}>()

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

// 高亮菜单：根据当前路由自动计算
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/') return 'discover'
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
