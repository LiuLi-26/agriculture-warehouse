<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon><Shop /></el-icon>
        <span>农产品仓储系统</span>
        <el-tag type="warning" size="small" class="role-tag">仓库管理员</el-tag>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/warehouse/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/warehouse/product">
          <el-icon><Goods /></el-icon>
          <span>农产品管理</span>
        </el-menu-item>
        <el-menu-item index="/warehouse/location">
          <el-icon><Grid /></el-icon>
          <span>货位管理</span>
        </el-menu-item>
        <el-menu-item index="/warehouse/inbound">
          <el-icon><Upload /></el-icon>
          <span>入库管理</span>
        </el-menu-item>
        <el-menu-item index="/warehouse/outbound">
          <el-icon><Download /></el-icon>
          <span>出库管理</span>
        </el-menu-item>
        <el-menu-item index="/warehouse/alert">
          <el-icon><Bell /></el-icon>
          <span>预警中心</span>
        </el-menu-item>
        <el-menu-item index="/warehouse/environment">
          <el-icon><Sunny /></el-icon>
          <span>环境监测</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/warehouse/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="user-role warehouse">仓库管理员</span>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const username = ref(localStorage.getItem('username') || '仓库管理员')

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    ElMessage.success('退出成功')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
  position: relative;
}

.role-tag {
  position: absolute;
  right: 10px;
  top: 20px;
}

.menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-role {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-role.warehouse {
  color: #e6a23c;
  background: #fdf6ec;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
}

.user-info .el-icon {
  margin-right: 4px;
}

.main {
  background-color: #f0f2f6;
  padding: 20px;
}
</style>