<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>农产品仓储管理系统</h1>
        <p>智能仓储 · 高效管理</p>
      </div>

      <!-- 标签页：登录 / 注册 -->
      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名（2-16位）"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码（6-20位）"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" @click="handleLogin" :loading="loginLoading" class="submit-btn">
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名（2-16位，字母、数字、中文）"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码（6-20位）"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱（选填）"
                prefix-icon="Message"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="手机号（选填）"
                prefix-icon="Phone"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="success" size="large" @click="handleRegister" :loading="registerLoading" class="submit-btn">
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="login-tip">
        <p>测试账号：admin / 123456（管理员）</p>
        <p>仓库管理员：warehouse / 123456</p>
        <p>客户：注册后自动成为客户角色</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()
const loginFormRef = ref()
const registerFormRef = ref()
const loginLoading = ref(false)
const registerLoading = ref(false)
const activeTab = ref('login')

// 登录表单
const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 16, message: '用户名长度在2-16位之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ]
}

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

// 验证用户名（2-16位）
const validateUsername = (rule, value, callback) => {
  if (!value || value.trim() === '') {
    callback(new Error('用户名不能为空'))
  } else if (value.length < 2) {
    callback(new Error('用户名长度不能少于2位'))
  } else if (value.length > 16) {
    callback(new Error('用户名长度不能超过16位'))
  } else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字和中文'))
  } else {
    callback()
  }
}

// 验证密码（6-20位）
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else if (value.length > 20) {
    callback(new Error('密码长度不能超过20位'))
  } else {
    callback()
  }
}

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 验证邮箱（可选）
const validateEmail = (rule, value, callback) => {
  if (value && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

// 验证手机号（可选）
const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [{ validator: validateUsername, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }]
}

// 登录
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loginLoading.value = true

    const res = await api.login(loginForm)

    if (res.id) {
      localStorage.setItem('token', res.id)
      localStorage.setItem('username', res.username)
      localStorage.setItem('role', res.role)
      ElMessage.success(res.message)

      // 根据角色跳转到不同首页
      if (res.role === 'ADMIN') {
        router.push('/admin/dashboard')
      } else if (res.role === 'WAREHOUSE') {
        router.push('/warehouse/dashboard')
      } else {
        router.push('/customer/dashboard')
      }
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loginLoading.value = false
  }
}

// 注册
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true

    const res = await api.register({
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      email: registerForm.email,
      phone: registerForm.phone
    })

    if (res.id) {
      ElMessage.success(res.message || '注册成功，请登录')
      // 清空表单
      registerForm.username = ''
      registerForm.password = ''
      registerForm.confirmPassword = ''
      registerForm.email = ''
      registerForm.phone = ''
      // 切换到登录页
      activeTab.value = 'login'
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 450px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.tabs {
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
}

.login-tip {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  color: #999;
  font-size: 12px;
}

.login-tip p {
  margin: 5px 0;
}
</style>