<template>
  <div class="min-h-screen bg-bg flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-20 h-20 bg-primary rounded-full mb-4">
          <span class="text-white text-3xl font-bold">小</span>
        </div>
        <h1 class="text-3xl font-bold text-gray-900">校园小红书</h1>
        <p class="text-gray-500 mt-2">分享你的校园生活</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="bg-white rounded-2xl shadow-lg p-8"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            :disabled="loading"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="w-full"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>

        <div class="text-center text-sm text-gray-500">
          还没有账号？
          <router-link to="/register" class="text-primary hover:text-primary-dark">立即注册</router-link>
        </div>
      </el-form>

      <div class="text-center mt-6 text-xs text-gray-400">
        测试账号: test / 123456
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, max: 20, message: '密码长度在3-20个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true

  try {
    const data = await login({
      username: form.username,
      password: form.password
    })

    userStore.setUser({
      token: data.token,
      userId: data.userId,
      nickname: data.nickname,
      avatar: data.avatar || ''
    })
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
:deep(.el-input__wrapper) {
  border-radius: 12px;
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
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
}

:deep(.el-button--primary:hover) {
  background-color: #E6203C;
  border-color: #E6203C;
}

:deep(.el-form-item__error) {
  padding-top: 4px;
}
</style>
