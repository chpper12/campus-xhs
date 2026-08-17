<template>
  <el-dialog
    v-model="visible"
    title="编辑个人资料"
    width="520px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
    >
      <!-- 头像上传 -->
      <el-form-item label="头像" prop="avatar">
        <div class="flex items-center gap-4">
          <div
            class="w-20 h-20 rounded-full overflow-hidden border-2 border-gray-200 cursor-pointer hover:border-primary transition-colors"
            @click="triggerUpload"
          >
            <img
              v-if="form.avatar"
              :src="form.avatar"
              alt="头像"
              class="w-full h-full object-cover"
            />
            <div
              v-else
              class="w-full h-full bg-gray-100 flex flex-col items-center justify-center"
            >
              <el-icon :size="20" class="text-gray-400"><Plus /></el-icon>
              <span class="text-[10px] text-gray-400 mt-0.5">上传</span>
            </div>
          </div>
          <div class="flex-1">
            <el-input
              v-model="form.avatar"
              placeholder="或输入头像URL"
              clearable
            />
            <p class="text-xs text-gray-400 mt-1">点击头像上传，或粘贴图片链接</p>
          </div>
        </div>
        <input
          ref="uploadInput"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleUpload"
        />
      </el-form-item>

      <!-- 昵称 -->
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="form.nickname"
          placeholder="设置你的昵称"
          maxlength="30"
          show-word-limit
        />
      </el-form-item>

      <!-- 个人简介 -->
      <el-form-item label="个人简介" prop="bio">
        <el-input
          v-model="form.bio"
          type="textarea"
          :rows="4"
          placeholder="介绍一下自己吧~"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 手机号（回填的是后端脱敏后的值，如 138****6789） -->
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="form.phone"
          placeholder="输入11位手机号"
          maxlength="11"
          clearable
        />
        <p class="text-xs text-gray-400 mt-1">当前展示为脱敏后的号码；不修改则保持原值</p>
      </el-form-item>

      <!-- 邮箱（回填的是后端脱敏后的值，如 t****@qq.com） -->
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="form.email"
          placeholder="输入邮箱地址"
          clearable
        />
        <p class="text-xs text-gray-400 mt-1">当前展示为脱敏后的邮箱；不修改则保持原值</p>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        @click="handleSubmit"
      >
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { updateProfile, type UpdateProfileParams, type UserProfile } from '@/api/user'

interface Props {
  visible: boolean
  /** 当前用户资料，用于回填表单 */
  userProfile: UserProfile | null
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

const form = reactive({
  avatar: '',
  nickname: '',
  bio: '',
  phone: '',
  email: ''
})

// 打开弹窗时回填的原始值（后端脱敏后的值），用于提交时的脏检查：
// 只有用户真正修改过的手机号/邮箱才会提交，防止把脱敏值（138****6789）误存回数据库
const originalPhone = ref('')
const originalEmail = ref('')

const rules: FormRules = {
  nickname: [
    { max: 30, message: '昵称长度不能超过30个字符', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '简介长度不能超过200个字符', trigger: 'blur' }
  ],
  phone: [
    {
      validator: (_rule, value: string, callback) => {
        // 未修改（仍是脱敏原值）或已清空 → 视为不修改，跳过校验
        if (!value || value === originalPhone.value) {
          callback()
          return
        }
        if (!/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('手机号格式不正确'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (_rule, value: string, callback) => {
        // 未修改（仍是脱敏原值）或已清空 → 视为不修改，跳过校验
        if (!value || value === originalEmail.value) {
          callback()
          return
        }
        if (!/^[\w.-]+@[\w-]+(\.[\w-]+)+$/.test(value)) {
          callback(new Error('邮箱格式不正确'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

// 同步外部 visible
watch(() => props.visible, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:visible', val)
})

// 打开弹窗时用当前用户资料回填表单（phone/email 为后端脱敏后的值）
watch(() => props.visible, (val) => {
  if (val && props.userProfile) {
    form.avatar = props.userProfile.avatar || ''
    form.nickname = props.userProfile.nickname || ''
    form.bio = props.userProfile.bio || ''
    form.phone = props.userProfile.phone || ''
    form.email = props.userProfile.email || ''
    // 记录脱敏原值，用于提交时的脏检查
    originalPhone.value = form.phone
    originalEmail.value = form.email
  }
})

// ======================== 头像上传（复用 PublishModal 写法） ========================
const triggerUpload = () => {
  uploadInput.value?.click()
}

const handleUpload = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  try {
    const data = await request({
      url: '/v1/upload/image',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    }) as { url: string; filename: string }

    form.avatar = data.url
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('头像上传失败:', error)
  } finally {
    // 清空 input 以支持重复上传同一文件
    if (uploadInput.value) {
      uploadInput.value.value = ''
    }
  }
}

// ======================== 提交 ========================
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true

  try {
    const params: UpdateProfileParams = {}
    if (form.nickname) params.nickname = form.nickname
    if (form.bio) params.bio = form.bio
    if (form.avatar) params.avatar = form.avatar
    // 脏检查：只有用户真正修改过的手机号/邮箱才提交，
    // 避免把脱敏值（138****6789）误存回数据库覆盖真实数据
    if (form.phone && form.phone !== originalPhone.value) params.phone = form.phone
    if (form.email && form.email !== originalEmail.value) params.email = form.email

    await updateProfile(params)

    ElMessage.success('资料更新成功')
    emit('success')
    visible.value = false
  } catch (error) {
    console.error('更新资料失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  form.avatar = ''
  form.nickname = ''
  form.bio = ''
  form.phone = ''
  form.email = ''
  originalPhone.value = ''
  originalEmail.value = ''
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

:deep(.el-button--primary) {
  background-color: #FF2442;
  border-color: #FF2442;
}

:deep(.el-button--primary:hover) {
  background-color: #E6203C;
  border-color: #E6203C;
}
</style>
