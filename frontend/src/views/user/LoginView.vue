<template>
  <section class="page auth-page">
    <div class="auth-panel">
      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form
        ref="authFormRef"
        class="auth-form"
        label-position="top"
        :model="form"
        :rules="rules"
        @submit.prevent="submit"
      >
        <el-form-item v-if="mode === 'register'" label="昵称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="50" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            maxlength="32"
            placeholder="请输入 6 到 32 位密码"
            show-password
          />
        </el-form-item>
        <el-form-item v-if="mode === 'register'" label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            maxlength="32"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
        <el-button type="primary" native-type="submit" class="submit-button" :loading="submitting">
          {{ mode === 'login' ? '登录' : '注册并登录' }}
        </el-button>
      </el-form>
    </div>
  </section>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const mode = ref('login')
const submitting = ref(false)
const authFormRef = ref()
const form = reactive({
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const phonePattern = /^1[3-9]\d{9}$/

// 登录和注册共用一个表单，昵称只有注册模式下必填。
const validateNickname = (rule, value, callback) => {
  if (mode.value === 'register' && !String(value || '').trim()) {
    callback(new Error('请输入昵称'))
    return
  }
  callback()
}

// 前端先拦截手机号格式，避免浏览器或校验库透出英文提示。
const validatePhone = (rule, value, callback) => {
  const phone = String(value || '').trim()
  if (!phone) {
    callback(new Error('请输入手机号'))
    return
  }
  if (!phonePattern.test(phone)) {
    callback(new Error('请输入正确的手机格式'))
    return
  }
  callback()
}

const validatePassword = (rule, value, callback) => {
  const password = String(value || '')
  if (!password) {
    callback(new Error('请输入密码'))
    return
  }
  if (password.length < 6 || password.length > 32) {
    callback(new Error('密码长度必须为 6 到 32 位'))
    return
  }
  callback()
}

// 注册时要求两次密码一致，避免用户输错后直接创建账号。
const validateConfirmPassword = (rule, value, callback) => {
  if (mode.value !== 'register') {
    callback()
    return
  }
  if (!String(value || '')) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const rules = {
  nickname: [
    { validator: validateNickname, trigger: 'blur' },
    { max: 50, message: '昵称长度不能超过 50 个字符', trigger: 'blur' }
  ],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const submit = async () => {
  if (submitting.value) {
    return
  }
  try {
    await authFormRef.value?.validate()
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    if (mode.value === 'login') {
      await userStore.login({
        phone: form.phone.trim(),
        password: form.password
      })
      ElMessage.success('登录成功')
    } else {
      await userStore.register({
        nickname: form.nickname.trim(),
        phone: form.phone.trim(),
        password: form.password
      })
      ElMessage.success('注册成功')
    }
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    // 管理员账号登录后默认进入管理后台；普通用户仍按来源页返回。
    const target = userStore.user?.role === 1 && (!redirect || redirect === '/') ? '/admin' : (redirect || '/')
    router.push(target)
  } catch (error) {
    const message = error?.response?.data?.message || error?.message || '操作失败'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
}

watch(mode, () => {
  form.confirmPassword = ''
  authFormRef.value?.clearValidate()
})
</script>

<style scoped>
.auth-page {
  display: grid;
  min-height: calc(100vh - 150px);
  place-items: center;
}

.auth-panel {
  width: min(420px, 100%);
  padding: 24px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

.auth-form {
  margin-top: 8px;
}

.submit-button {
  width: 100%;
}
</style>
