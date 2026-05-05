<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="aside">
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
        <!-- 首页 - 所有角色可见 -->
        <el-menu-item :index="getPath('/dashboard')">
          <el-icon><DataLine /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 基础数据管理 -->
        <el-sub-menu index="basic">
          <template #title>
            <el-icon><Folder /></el-icon>
            <span>基础数据</span>
          </template>
          <!-- 农产品管理：管理员和仓库管理员可见，客户不可见 -->
          <el-menu-item v-if="userRole !== 'CUSTOMER'" :index="getPath('/product')">
            <el-icon><Goods /></el-icon>
            <span>农产品管理</span>
          </el-menu-item>
          <!-- 客户只能查看农产品 -->
          <el-menu-item v-else :index="getPath('/product')">
            <el-icon><Goods /></el-icon>
            <span>农产品查看</span>
          </el-menu-item>

          <!-- 货位管理：管理员和仓库管理员可见，客户不可见 -->
          <el-menu-item v-if="userRole !== 'CUSTOMER'" :index="getPath('/location')">
            <el-icon><Grid /></el-icon>
            <span>货位管理</span>
          </el-menu-item>
          <!-- 客户只能查看货位 -->
          <el-menu-item v-else :index="getPath('/location')">
            <el-icon><Grid /></el-icon>
            <span>货位查看</span>
          </el-menu-item>

          <!-- 供应商管理 - 仅管理员 -->
          <el-menu-item v-if="userRole === 'ADMIN'" :index="getPath('/supplier')">
            <el-icon><OfficeBuilding /></el-icon>
            <span>供应商管理</span>
          </el-menu-item>

          <!-- 用户管理 - 仅管理员 -->
          <el-menu-item v-if="userRole === 'ADMIN'" :index="getPath('/user-list')">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>

          <!-- 客户管理 - 仅管理员 -->
          <el-menu-item v-if="userRole === 'ADMIN'" :index="getPath('/customer')">
            <el-icon><User /></el-icon>
            <span>客户管理</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 仓储作业 - 仅管理员和仓库管理员可见，客户不可见 -->
        <el-sub-menu v-if="userRole !== 'CUSTOMER'" index="operation">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>仓储作业</span>
          </template>
          <el-menu-item :index="getPath('/inbound')">
            <el-icon><Upload /></el-icon>
            <span>入库管理</span>
          </el-menu-item>
          <el-menu-item :index="getPath('/outbound')">
            <el-icon><Download /></el-icon>
            <span>出库管理</span>
          </el-menu-item>
          <el-menu-item v-if="userRole === 'ADMIN'" :index="getPath('/inventory-check')">
            <el-icon><List /></el-icon>
            <span>库存盘点</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 监控与预警 - 所有角色可见 -->
        <el-sub-menu index="monitor">
          <template #title>
            <el-icon><Bell /></el-icon>
            <span>监控预警</span>
          </template>
          <el-menu-item :index="getPath('/alert')">
            <el-icon><Warning /></el-icon>
            <span>预警中心</span>
          </el-menu-item>
          <el-menu-item :index="getPath('/environment')">
            <el-icon><Sunny /></el-icon>
            <span>环境监测</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 统计报表 - 仅管理员可见 -->
        <el-sub-menu v-if="userRole === 'ADMIN'" index="report">
          <template #title>
            <el-icon><PieChart /></el-icon>
            <span>统计报表</span>
          </template>
          <el-menu-item :index="getPath('/report-inbound')">
            <el-icon><Upload /></el-icon>
            <span>入库统计</span>
          </el-menu-item>
          <el-menu-item :index="getPath('/report-outbound')">
            <el-icon><Download /></el-icon>
            <span>出库统计</span>
          </el-menu-item>
          <el-menu-item :index="getPath('/report-waste')">
            <el-icon><Warning /></el-icon>
            <span>损耗统计</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 系统管理 - 仅管理员可见 -->
        <el-sub-menu v-if="userRole === 'ADMIN'" index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item :index="getPath('/log')">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item :index="getPath('/system-config')">
            <el-icon><Tools /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
          <el-menu-item :index="getPath('/backup')">
            <el-icon><FolderOpened /></el-icon>
            <span>数据备份</span>
          </el-menu-item>
          <el-menu-item :index="getPath('/help')">
            <el-icon><QuestionFilled /></el-icon>
            <span>操作指南</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 个人中心 - 所有角色可见 -->
        <el-menu-item :index="getPath('/profile')">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: getPath('/dashboard') }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="user-role" :class="roleClass">{{ roleText }}</span>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
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

// 从 localStorage 获取用户信息
const username = ref(localStorage.getItem('username') || '管理员')
const userRole = ref(localStorage.getItem('role') || 'CUSTOMER')

// 角色文本和样式
const roleText = computed(() => {
  if (userRole.value === 'ADMIN') return '系统管理员'
  if (userRole.value === 'WAREHOUSE') return '仓库管理员'
  return '客户'
})

const roleClass = computed(() => {
  if (userRole.value === 'ADMIN') return 'role-admin'
  if (userRole.value === 'WAREHOUSE') return 'role-warehouse'
  return 'role-customer'
})

// 根据角色生成正确的路径
const getPath = (path) => {
  if (userRole.value === 'ADMIN') return `/admin${path}`
  if (userRole.value === 'WAREHOUSE') return `/warehouse${path}`
  return `/customer${path}`
}

// 当前激活的菜单
const activeMenu = computed(() => {
  const currentPath = route.path
  if (currentPath.startsWith('/admin/') ||
      currentPath.startsWith('/warehouse/') ||
      currentPath.startsWith('/customer/')) {
    return currentPath
  }
  return getPath(route.path)
})

// 当前页面标题
const currentTitle = computed(() => route.meta?.title || '')

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push(getPath('/profile'))
  } else if (command === 'logout') {
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
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
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
  overflow-y: auto;
}

.menu:not(.el-menu--collapse) {
  width: 240px;
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
  gap: 16px;
}

.user-role {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 20px;
}

.role-admin {
  color: #f56c6c;
  background: #fef0f0;
}

.role-warehouse {
  color: #e6a23c;
  background: #fdf6ec;
}

.role-customer {
  color: #67c23a;
  background: #f0f9eb;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
  font-size: 14px;
  gap: 4px;
}

.main {
  background-color: #f0f2f6;
  padding: 20px;
}

/* 滚动条样式 */
.aside::-webkit-scrollbar,
.menu::-webkit-scrollbar {
  width: 6px;
}

.aside::-webkit-scrollbar-track,
.menu::-webkit-scrollbar-track {
  background: #1f2d3d;
}

.aside::-webkit-scrollbar-thumb,
.menu::-webkit-scrollbar-thumb {
  background: #4a6a8a;
  border-radius: 3px;
}
</style>