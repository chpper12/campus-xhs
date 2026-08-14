<template>
  <el-dialog
    v-model="visible"
    title="发布笔记"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
    >
      <!-- 多图上传区域 -->
      <el-form-item label="图片" prop="imageUrls">
        <div class="w-full">
          <!-- 图片网格 -->
          <div class="grid grid-cols-3 gap-3 mb-3">
            <!-- 已上传的图片 -->
            <div
              v-for="(url, index) in form.imageUrls"
              :key="index"
              class="relative aspect-square rounded-lg overflow-hidden border border-gray-200 group"
            >
              <img
                :src="url"
                alt="图片"
                class="w-full h-full object-cover"
              />
              <!-- 删除按钮 -->
              <button
                class="absolute top-1 right-1 w-6 h-6 rounded-full bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-black/70"
                @click="removeImage(index)"
              >
                <el-icon :size="14" class="text-white"><Close /></el-icon>
              </button>
              <!-- 图片序号 -->
              <div class="absolute bottom-1 left-1 px-1.5 py-0.5 rounded bg-black/50 text-white text-xs">
                {{ index + 1 }}
              </div>
            </div>

            <!-- 上传按钮（最多9张） -->
            <div
              v-if="form.imageUrls.length < 9"
              class="aspect-square rounded-lg border-2 border-dashed border-gray-300 flex flex-col items-center justify-center cursor-pointer hover:border-primary transition-colors"
              @click="triggerUpload"
            >
              <el-icon :size="24" class="text-gray-400 mb-1"><Plus /></el-icon>
              <span class="text-xs text-gray-400">{{ form.imageUrls.length }}/9</span>
            </div>
          </div>

          <!-- URL 输入添加 -->
          <div class="flex gap-2">
            <el-input
              v-model="imageUrlInput"
              placeholder="输入图片URL，按回车添加"
              clearable
              @keyup.enter="addImageByUrl"
            >
              <template #prefix>
                <el-icon><Link /></el-icon>
              </template>
            </el-input>
            <el-button @click="addImageByUrl" :disabled="!imageUrlInput.trim()">
              添加
            </el-button>
          </div>
          <p class="text-xs text-gray-400 mt-1">支持jpg/png格式，最多上传9张图片</p>
        </div>
        <input
          ref="uploadInput"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleUpload"
        />
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="填写标题，吸引更多人关注"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="分区" prop="category">
        <el-select v-model="form.category" placeholder="选择分区">
          <el-option
            v-for="cat in categories"
            :key="cat"
            :label="cat"
            :value="cat"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="正文" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          placeholder="分享你的校园生活..."
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item>
        <el-button
          class="ai-polish-btn"
          :loading="aiLoading"
          @click="handleAiPolish"
        >
          <el-icon class="mr-1"><MagicStick /></el-icon>
          AI智能润色
        </el-button>
        <span class="text-xs text-gray-400 ml-2">使用AI优化你的文案</span>
      </el-form-item>

      <el-form-item v-if="form.tags.length > 0" label="标签">
        <div class="flex flex-wrap gap-2">
          <el-tag
            v-for="tag in form.tags"
            :key="tag"
            closable
            @close="removeTag(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        @click="handleSubmit"
      >
        发布
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, MagicStick, Close, Link } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Props {
  visible: boolean
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.visible)
const formRef = ref<FormInstance>()
const uploadInput = ref<HTMLInputElement>()
const submitting = ref(false)
const aiLoading = ref(false)
const imageUrlInput = ref('')

const categories = ['推荐', '教育', '穿搭', '美食', '彩妆', '影视', '游戏', '职场', '情感', '萌宠']

const form = reactive({
  title: '',
  category: '推荐',
  content: '',
  imageUrls: [] as string[],
  tags: [] as string[]
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 20, message: '标题长度在2-20个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分区', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入正文', trigger: 'blur' },
    { min: 10, max: 1000, message: '正文长度在10-1000个字符', trigger: 'blur' }
  ],
  imageUrls: [
    {
      required: true,
      validator: (_rule: any, _value: any, callback: (error?: Error) => void) => {
        if (form.imageUrls.length === 0) {
          callback(new Error('请至少上传一张图片'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

watch(() => props.visible, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:visible', val)
})

// 触发文件选择
const triggerUpload = () => {
  uploadInput.value?.click()
}

// 移除图片
const removeImage = (index: number) => {
  form.imageUrls.splice(index, 1)
}

// 通过URL添加图片
const addImageByUrl = () => {
  const url = imageUrlInput.value.trim()
  if (!url) return

  if (form.imageUrls.length >= 9) {
    ElMessage.warning('最多只能上传9张图片')
    return
  }

  // 简单校验URL格式
  if (!url.match(/^https?:\/\/.+\.(jpg|jpeg|png|gif|webp)(\?.*)?$/i)) {
    ElMessage.warning('请输入有效的图片URL')
    return
  }

  form.imageUrls.push(url)
  imageUrlInput.value = ''
  ElMessage.success('图片已添加')
}

// 处理文件上传
const handleUpload = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return

  if (form.imageUrls.length >= 9) {
    ElMessage.warning('最多只能上传9张图片')
    return
  }

  // 验证文件类型和大小
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return
  }

  // 创建 FormData 对象
  const formData = new FormData()
  formData.append('file', file)

  try {
    // 调用后端上传接口
    const data = await request({
      url: '/v1/upload/image',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }) as { url: string; filename: string }

    form.imageUrls.push(data.url)
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败，请重试')
  } finally {
    // 清空 input，允许重复上传同一文件
    if (uploadInput.value) {
      uploadInput.value.value = ''
    }
  }
}

const removeTag = (tag: string) => {
  form.tags = form.tags.filter(t => t !== tag)
}

const handleAiPolish = async () => {
  if (!form.content) {
    ElMessage.warning('请先输入正文内容')
    return
  }

  aiLoading.value = true

  try {
    const data = await request({
      url: '/v1/posts/ai-polish',
      method: 'post',
      data: {
        content: form.content
      }
    }) as { polishedContent: string; tags: string[] }

    form.content = data.polishedContent
    form.tags = [...new Set([...form.tags, ...data.tags])]
    ElMessage.success('AI润色完成')
  } catch (error) {
    console.error('AI润色失败:', error)
  } finally {
    aiLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true

  try {
    await request({
      url: '/v1/posts',
      method: 'post',
      data: {
        title: form.title,
        category: form.category,
        content: form.content,
        imageUrls: form.imageUrls
      }
    })

    ElMessage.success('发布成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('发布失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  form.title = ''
  form.category = '推荐'
  form.content = ''
  form.imageUrls = []
  form.tags = []
  imageUrlInput.value = ''
  visible.value = false
}
</script>

<style scoped>
:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

:deep(.el-dialog__title) {
  font-weight: 600;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

.ai-polish-btn {
  background-color: #FF8A9B !important;
  border-color: #FF8A9B !important;
  color: #ffffff !important;
}

.ai-polish-btn:hover {
  background-color: #FF6B81 !important;
  border-color: #FF6B81 !important;
  color: #ffffff !important;
}
</style>
