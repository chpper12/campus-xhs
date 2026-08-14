<template>
  <el-dialog
    v-model="visible"
    :show-close="false"
    width="900px"
    class="post-detail-dialog"
    @close="handleClose"
  >
    <div class="flex h-[600px]">
      <!-- 左侧图片轮播 -->
      <div
        class="w-[480px] bg-black flex-shrink-0 relative group"
        @mouseenter="showArrows = true"
        @mouseleave="showArrows = false"
      >
        <!-- 图片容器 -->
        <div class="w-full h-full overflow-hidden relative">
          <div
            class="flex h-full transition-transform duration-300 ease-in-out"
            :style="{ transform: `translateX(-${currentIndex * 100}%)` }"
          >
            <div
              v-for="(url, index) in imageList"
              :key="index"
              class="w-full h-full flex-shrink-0 flex items-center justify-center"
            >
              <img
                :src="url"
                :alt="`${post.title} - ${index + 1}`"
                class="max-w-full max-h-full object-contain"
              />
            </div>
          </div>
        </div>

        <!-- 上一张箭头 -->
        <button
          v-if="imageList.length > 1 && showArrows"
          class="absolute left-2 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center transition-all opacity-0 group-hover:opacity-100"
          @click="prevImage"
        >
          <el-icon :size="20" class="text-white"><ArrowLeft /></el-icon>
        </button>

        <!-- 下一张箭头 -->
        <button
          v-if="imageList.length > 1 && showArrows"
          class="absolute right-2 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center transition-all opacity-0 group-hover:opacity-100"
          @click="nextImage"
        >
          <el-icon :size="20" class="text-white"><ArrowRight /></el-icon>
        </button>

        <!-- 底部圆点指示器 -->
        <div
          v-if="imageList.length > 1"
          class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2"
        >
          <button
            v-for="(_, index) in imageList"
            :key="index"
            :class="[
              'w-2 h-2 rounded-full transition-all',
              currentIndex === index
                ? 'bg-white w-4'
                : 'bg-white/50 hover:bg-white/75'
            ]"
            @click="goToImage(index)"
          />
        </div>

        <!-- 图片计数器 -->
        <div
          v-if="imageList.length > 1"
          class="absolute top-4 right-4 px-2 py-1 rounded bg-black/50 text-white text-xs"
        >
          {{ currentIndex + 1 }} / {{ imageList.length }}
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="flex-1 flex flex-col">
        <!-- 顶部关闭按钮 -->
        <div class="flex justify-end p-3">
          <button
            class="w-8 h-8 rounded-full hover:bg-gray-100 flex items-center justify-center transition-colors"
            @click="visible = false"
          >
            <el-icon :size="20"><Close /></el-icon>
          </button>
        </div>

        <!-- 作者信息 -->
        <div class="flex items-center gap-3 px-6 pb-4 border-b border-gray-100">
          <img
            :src="post.author.avatar"
            :alt="post.author.nickname"
            class="w-10 h-10 rounded-full object-cover"
          />
          <div>
            <div class="font-medium text-gray-900">{{ post.author.nickname }}</div>
            <div class="text-xs text-gray-400">发布于 {{ post.createTime }}</div>
          </div>
          <el-button
            class="ml-auto"
            type="primary"
            plain
            size="small"
          >
            关注
          </el-button>
        </div>

        <!-- 正文内容 -->
        <div class="flex-1 overflow-y-auto px-6 py-4">
          <h2 class="text-lg font-bold text-gray-900 mb-4">{{ post.title }}</h2>
          <div class="text-gray-700 leading-relaxed whitespace-pre-wrap">{{ postDetail?.content || '' }}</div>

          <!-- 标签 -->
          <div v-if="postDetail?.tags && postDetail.tags.length > 0" class="flex flex-wrap gap-2 mt-4">
            <span
              v-for="tag in postDetail.tags"
              :key="tag"
              class="px-3 py-1 bg-red-50 text-primary text-xs rounded-full"
            >
              {{ tag.startsWith('#') ? tag : '#' + tag }}
            </span>
          </div>

          <!-- 评论区 -->
          <div class="mt-6 pt-4 border-t border-gray-100">
            <div class="flex items-center justify-between mb-4">
              <span class="font-medium text-gray-900">评论 ({{ postDetail?.commentCount ?? comments.length }})</span>
            </div>

            <!-- 评论列表 -->
            <div v-if="comments.length > 0" class="space-y-4">
              <div
                v-for="comment in comments"
                :key="comment.id"
                class="flex gap-3"
              >
                <img
                  :src="comment.author.avatar"
                  :alt="comment.author.nickname"
                  class="w-8 h-8 rounded-full object-cover flex-shrink-0"
                />
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <span class="text-sm font-medium text-gray-900">{{ comment.author.nickname }}</span>
                    <span class="text-xs text-gray-400">{{ comment.createTime }}</span>
                  </div>
                  <p class="text-sm text-gray-700">{{ comment.content }}</p>
                </div>
              </div>
            </div>

            <!-- 空评论 -->
            <div v-else class="text-center py-8 text-gray-400">
              <p>暂无评论，快来抢沙发吧~</p>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="border-t border-gray-100 px-6 py-3">
          <div class="flex items-center gap-4">
            <!-- 点赞按钮 -->
            <button
              class="flex items-center gap-1 px-4 py-2 rounded-full hover:bg-gray-50 transition-colors"
              @click="handleLike"
            >
              <svg
                :class="['w-5 h-5', post.liked ? 'text-primary fill-primary' : 'text-gray-500']"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
              </svg>
              <span :class="['text-sm', post.liked ? 'text-primary' : 'text-gray-500']">
                {{ post.likeCount }}
              </span>
            </button>

            <!-- 收藏按钮 -->
            <button class="flex items-center gap-1 px-4 py-2 rounded-full hover:bg-gray-50 transition-colors">
              <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
              </svg>
              <span class="text-sm text-gray-500">收藏</span>
            </button>

            <!-- 分享按钮 -->
            <button class="flex items-center gap-1 px-4 py-2 rounded-full hover:bg-gray-50 transition-colors">
              <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
              </svg>
              <span class="text-sm text-gray-500">分享</span>
            </button>

            <!-- 评论输入框 -->
            <div class="flex-1 flex gap-2">
              <el-input
                v-model="commentContent"
                placeholder="说点什么..."
                class="flex-1"
                @keyup.enter="handleComment"
              />
              <el-button
                type="primary"
                :disabled="!commentContent.trim()"
                @click="handleComment"
              >
                发送
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { likePost, getPostDetail, type Post, type PostDetail } from '@/api/posts'
import { getComments, addComment, type Comment } from '@/api/comments'

interface Props {
  visible: boolean
  post: Post
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'like', post: Post): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.visible)
const commentContent = ref('')
const comments = ref<Comment[]>([])
const loadingComments = ref(false)
const postDetail = ref<PostDetail | null>(null)
const loadingDetail = ref(false)

// 轮播相关状态
const currentIndex = ref(0)
const showArrows = ref(false)

// 计算图片列表（优先使用详情接口的 imageUrls，降级使用 post.coverUrl）
const imageList = computed<string[]>(() => {
  if (postDetail.value?.imageUrls && postDetail.value.imageUrls.length > 0) {
    return postDetail.value.imageUrls
  }
  if (props.post.coverUrl) {
    return [props.post.coverUrl]
  }
  return []
})

watch(() => props.visible, (val) => {
  visible.value = val
  if (val && props.post.id) {
    currentIndex.value = 0 // 重置轮播索引
    fetchPostDetail()
    fetchComments()
  }
})

watch(visible, (val) => {
  emit('update:visible', val)
})

// 获取笔记详情
const fetchPostDetail = async () => {
  if (!props.post.id) return
  loadingDetail.value = true
  try {
    postDetail.value = await getPostDetail(props.post.id)
  } catch (error) {
    console.error('获取笔记详情失败:', error)
  } finally {
    loadingDetail.value = false
  }
}

// 获取评论
const fetchComments = async () => {
  if (!props.post.id) return

  loadingComments.value = true
  try {
    const data = await getComments(props.post.id, { current: 1, size: 50 })
    comments.value = data.list
  } catch (error) {
    console.error('获取评论失败:', error)
  } finally {
    loadingComments.value = false
  }
}

// 轮播方法
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  } else {
    currentIndex.value = imageList.value.length - 1
  }
}

const nextImage = () => {
  if (currentIndex.value < imageList.value.length - 1) {
    currentIndex.value++
  } else {
    currentIndex.value = 0
  }
}

const goToImage = (index: number) => {
  currentIndex.value = index
}

// 点赞
const handleLike = async () => {
  try {
    const result = await likePost(props.post.id)
    props.post.liked = result.isLiked
    props.post.likeCount = result.likeCount
    // 同步更新详情数据
    if (postDetail.value) {
      postDetail.value.liked = result.isLiked
      postDetail.value.likeCount = result.likeCount
    }
    emit('like', props.post)
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 发表评论
const handleComment = async () => {
  if (!commentContent.value.trim()) return

  try {
    await addComment(props.post.id, commentContent.value)
    commentContent.value = ''
    ElMessage.success('评论成功')
    // 重新获取评论列表
    await fetchComments()
    // 更新评论数
    if (postDetail.value) {
      postDetail.value.commentCount++
    }
  } catch (error) {
    console.error('评论失败:', error)
  }
}

// 关闭弹窗
const handleClose = () => {
  commentContent.value = ''
}
</script>

<style scoped>
:deep(.post-detail-dialog) {
  border-radius: 16px;
  overflow: hidden;
  padding: 0;
}

:deep(.post-detail-dialog .el-dialog__header) {
  display: none;
}

:deep(.post-detail-dialog .el-dialog__body) {
  padding: 0;
}

:deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #FF2442 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #FF2442 inset;
}

:deep(.el-button--primary) {
  background-color: #FF2442;
  border-color: #FF2442;
  border-radius: 20px;
}

:deep(.el-button--primary:hover) {
  background-color: #E6203C;
  border-color: #E6203C;
}

:deep(.el-button--primary.is-plain) {
  background-color: transparent;
  color: #FF2442;
  border-color: #FF2442;
}

:deep(.el-button--primary.is-plain:hover) {
  background-color: #FFF0F0;
  color: #FF2442;
  border-color: #FF2442;
}
</style>
