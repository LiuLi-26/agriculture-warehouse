<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon><Shop /></el-icon>
        <span>农产品仓储系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-menu-item index="/product">
          <el-icon><Goods /></el-icon>
          <span>农产品管理</span>
        </el-menu-item>

        <el-menu-item index="/location">
          <el-icon><Grid /></el-icon>
          <span>货位管理</span>
        </el-menu-item>

        <el-menu-item index="/inbound">
          <el-icon><Upload /></el-icon>
          <span>入库管理</span>
        </el-menu-item>

        <el-menu-item index="/outbound">
          <el-icon><Download /></el-icon>
          <span>出库管理</span>
        </el-menu-item>

        <el-menu-item index="/alert">
          <el-icon><Bell /></el-icon>
          <span>预警中心</span>
        </el-menu-item>

        <el-menu-item index="/environment">
          <el-icon><Sunny /></el-icon>
          <span>环境监测</span>
        </el-menu-item>

        <!-- 操作日志 - 仅管理员可见 -->
        <el-menu-item v-if="isAdmin" index="/log">
          <el-icon><Document /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-right">
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

const username = ref(localStorage.getItem('username') || '管理员')

// 判断是否为管理员（用于控制菜单显示）
const isAdmin = computed(() => {
  const role = localStorage.getItem('role')
  return role === 'ADMIN'
})

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 退出登录
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
}

.logo .el-icon {
  font-size: 24px;
  margin-right: 8px;
}

.menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
  font-size: 14px;
}

.user-info .el-icon {
  margin-right: 4px;
}

.main {
  background-color: #f0f2f6;
  padding: 20px;
}
</style>