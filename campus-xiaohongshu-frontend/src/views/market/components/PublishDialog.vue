<template>
  <el-dialog
    v-model="visible"
    title="发布商品"
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
      <!-- 封面图上传 -->
      <el-form-item label="封面图" prop="coverUrl">
        <div class="w-full">
          <div class="flex gap-3 mb-3">
            <!-- 已上传封面预览 -->
            <div
              v-if="form.coverUrl"
              class="relative w-40 aspect-square rounded-lg overflow-hidden border border-gray-200 group"
            >
              <img
                :src="form.coverUrl"
                alt="封面图"
                class="w-full h-full object-cover"
              />
              <!-- 删除按钮 -->
              <button
                class="absolute top-1 right-1 w-6 h-6 rounded-full bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-black/70"
                @click="removeCover"
              >
                <el-icon :size="14" class="text-white"><Close /></el-icon>
              </button>
            </div>

            <!-- 上传按钮 -->
            <div
              v-else
              class="w-40 aspect-square rounded-lg border-2 border-dashed border-gray-300 flex flex-col items-center justify-center cursor-pointer hover:border-primary transition-colors"
              @click="triggerUpload"
            >
              <el-icon v-if="!uploading" :size="24" class="text-gray-400 mb-1"><Plus /></el-icon>
              <el-icon v-else class="is-loading text-primary mb-1" :size="24"><Loading /></el-icon>
              <span class="text-xs text-gray-400">上传封面</span>
            </div>
          </div>

          <!-- URL 输入添加 -->
          <div class="flex gap-2">
            <el-input
              v-model="coverUrlInput"
              placeholder="或输入封面图URL，点击设置"
              clearable
              @keyup.enter="setCoverByUrl"
            >
              <template #prefix>
                <el-icon><Link /></el-icon>
              </template>
            </el-input>
            <el-button @click="setCoverByUrl" :disabled="!coverUrlInput.trim()">
              设置
            </el-button>
          </div>
          <p class="text-xs text-gray-400 mt-1">支持jpg/png格式，大小不超过5MB</p>
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
          placeholder="填写商品标题，吸引更多人关注"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="描述一下商品的成色、入手渠道、转手原因..."
        />
      </el-form-item>

      <!-- 详情图上传（最多9张，提交时 JSON.stringify 为 imageUrls 字符串） -->
      <el-form-item label="详情图" prop="imageUrls">
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
                alt="详情图"
                class="w-full h-full object-cover"
              />
              <!-- 删除按钮 -->
              <button
                class="absolute top-1 right-1 w-6 h-6 rounded-full bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-black/70"
                @click="removeDetailImage(index)"
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
              @click="triggerDetailUpload"
            >
              <el-icon v-if="!detailUploading" :size="24" class="text-gray-400 mb-1"><Plus /></el-icon>
              <el-icon v-else class="is-loading text-primary mb-1" :size="24"><Loading /></el-icon>
              <span class="text-xs text-gray-400">{{ form.imageUrls.length }}/9</span>
            </div>
          </div>

          <!-- URL 输入添加 -->
          <div class="flex gap-2">
            <el-input
              v-model="detailImageUrlInput"
              placeholder="输入图片URL，按回车添加"
              clearable
              @keyup.enter="addDetailImageByUrl"
            >
              <template #prefix>
                <el-icon><Link /></el-icon>
              </template>
            </el-input>
            <el-button @click="addDetailImageByUrl" :disabled="!detailImageUrlInput.trim()">
              添加
            </el-button>
          </div>
          <p class="text-xs text-gray-400 mt-1">选填，支持jpg/png格式，最多上传9张图片</p>
        </div>
        <input
          ref="detailUploadInput"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleDetailUpload"
        />
      </el-form-item>

      <el-form-item label="价格（元）" prop="price">
        <el-input-number
          v-model="form.price"
          :min="0"
          :precision="2"
          :step="1"
          controls-position="right"
          class="!w-48"
        />
      </el-form-item>

      <el-form-item label="联系方式" prop="contactInfo">
        <el-input
          v-model="form.contactInfo"
          placeholder="微信号 / 手机号"
          maxlength="100"
          show-word-limit
        />
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
import { Plus, Close, Link, Loading } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { publishMarketItem } from '@/api/market'

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
const detailUploadInput = ref<HTMLInputElement>()
const submitting = ref(false)
const uploading = ref(false)
const detailUploading = ref(false)
const coverUrlInput = ref('')
const detailImageUrlInput = ref('')

const form = reactive({
  title: '',
  description: '',
  price: 0,
  coverUrl: '',
  imageUrls: [] as string[],
  contactInfo: ''
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { max: 100, message: '商品标题长度不能超过100字', trigger: 'blur' }
  ],
  price: [
    {
      required: true,
      validator: (_rule: any, value: any, callback: (error?: Error) => void) => {
        if (value === null || value === undefined || value <= 0) {
          callback(new Error('价格必须大于0'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { max: 100, message: '联系方式长度不能超过100字', trigger: 'blur' }
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

// 移除封面
const removeCover = () => {
  form.coverUrl = ''
}

// 通过URL设置封面
const setCoverByUrl = () => {
  const url = coverUrlInput.value.trim()
  if (!url) return

  // 简单校验URL格式
  if (!url.match(/^https?:\/\/.+\.(jpg|jpeg|png|gif|webp)(\?.*)?$/i)) {
    ElMessage.warning('请输入有效的图片URL')
    return
  }

  form.coverUrl = url
  coverUrlInput.value = ''
  ElMessage.success('封面已设置')
}

// 处理封面文件上传（OSS 上传接口，与 PublishModal.vue 一致）
const handleUpload = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return

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

  uploading.value = true
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

    form.coverUrl = data.url
    ElMessage.success('封面上传成功')
  } catch (error) {
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败，请重试')
  } finally {
    uploading.value = false
    // 清空 input，允许重复上传同一文件
    if (uploadInput.value) {
      uploadInput.value.value = ''
    }
  }
}

// ===== 详情图（多图，最多9张，模式与 PublishModal.vue 一致） =====

// 触发详情图文件选择
const triggerDetailUpload = () => {
  detailUploadInput.value?.click()
}

// 移除详情图
const removeDetailImage = (index: number) => {
  form.imageUrls.splice(index, 1)
}

// 通过URL添加详情图
const addDetailImageByUrl = () => {
  const url = detailImageUrlInput.value.trim()
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
  detailImageUrlInput.value = ''
  ElMessage.success('图片已添加')
}

// 处理详情图文件上传（OSS 上传接口）
const handleDetailUpload = async (e: Event) => {
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

  detailUploading.value = true
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
    detailUploading.value = false
    // 清空 input，允许重复上传同一文件
    if (detailUploadInput.value) {
      detailUploadInput.value.value = ''
    }
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
    await publishMarketItem({
      title: form.title,
      description: form.description || undefined,
      price: form.price,
      coverUrl: form.coverUrl || undefined,
      // 契约要求：imageUrls 为 JSON 数组格式的字符串
      imageUrls: form.imageUrls.length > 0 ? JSON.stringify(form.imageUrls) : undefined,
      contactInfo: form.contactInfo
    })

    ElMessage.success('发布成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('发布商品失败:', error)
    // 展示后端返回的 msg（request 拦截器 reject 的 Error.message 即 msg）
    ElMessage.error((error as Error).message || '发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  form.title = ''
  form.description = ''
  form.price = 0
  form.coverUrl = ''
  form.imageUrls = []
  form.contactInfo = ''
  coverUrlInput.value = ''
  detailImageUrlInput.value = ''
  formRef.value?.clearValidate()
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
</style>
