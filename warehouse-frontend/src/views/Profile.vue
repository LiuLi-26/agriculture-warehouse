<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 左侧：个人信息 -->
      <el-col :span="12">
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="primary" link @click="editMode = !editMode">
                {{ editMode ? '取消' : '编辑' }}
              </el-button>
            </div>
          </template>

          <el-form :model="profileForm" :disabled="!editMode" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="getRoleTagType(profileForm.role)">
                {{ getRoleText(profileForm.role) }}
              </el-tag>
            </el-form-item>
            <el-form-item label="注册时间">
              {{ formatTime(profileForm.createTime) }}
            </el-form-item>
            <el-form-item v-if="editMode">
              <el-button type="primary" @click="updateProfile" :loading="updating">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：修改密码 -->
      <el-col :span="12">
        <el-card class="password-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>

          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="changing">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const editMode = ref(false)
const updating = ref(false)
const changing = ref(false)
const passwordFormRef = ref()

const profileForm = reactive({
  username: '',
  role: '',
  createTime: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const getRoleTagType = (role) => {
  if (role === 'ADMIN') return 'danger'
  if (role === 'WAREHOUSE') return 'warning'
  return 'success'
}

const getRoleText = (role) => {
  if (role === 'ADMIN') return '系统管理员'
  if (role === 'WAREHOUSE') return '仓库管理员'
  return '客户'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const fetchProfile = async () => {
  try {
    const res = await api.get('/user/profile')
    profileForm.username = res.username
    profileForm.role = res.role
    profileForm.createTime = res.createTime
  } catch (error) {
    console.error('获取个人信息失败:', error)
  }
}

const updateProfile = async () => {
  updating.value = true
  try {
    await api.put('/user/profile', {
      username: profileForm.username
    })
    ElMessage.success('个人信息更新成功')
    editMode.value = false
    localStorage.setItem('username', profileForm.username)
  } catch (error) {
    console.error('更新失败:', error)
  } finally {
    updating.value = false
  }
}

const changePassword = async () => {
  await passwordFormRef.value.validate()
  changing.value = true
  try {
    await api.put('/user/change-password', passwordForm)
    ElMessage.success('密码修改成功，请重新登录')
    setTimeout(() => {
      localStorage.clear()
      window.location.href = '/login'
    }, 1500)
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    changing.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 0;
}

.profile-card, .password-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>