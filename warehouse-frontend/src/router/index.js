import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  // 管理员路由
  {
    path: '/admin',
    component: Layout,
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', role: 'ADMIN' }
      },
      {
        path: 'product',
        name: 'AdminProduct',
        component: () => import('@/views/Product.vue'),
        meta: { title: '农产品管理', role: 'ADMIN' }
      },
      {
        path: 'location',
        name: 'AdminLocation',
        component: () => import('@/views/Location.vue'),
        meta: { title: '货位管理', role: 'ADMIN' }
      },
      {
        path: 'inbound',
        name: 'AdminInbound',
        component: () => import('@/views/Inbound.vue'),
        meta: { title: '入库管理', role: 'ADMIN' }
      },
      {
        path: 'outbound',
        name: 'AdminOutbound',
        component: () => import('@/views/Outbound.vue'),
        meta: { title: '出库管理', role: 'ADMIN' }
      },
      {
        path: 'alert',
        name: 'AdminAlert',
        component: () => import('@/views/Alert.vue'),
        meta: { title: '预警中心', role: 'ADMIN' }
      },
      {
        path: 'environment',
        name: 'AdminEnvironment',
        component: () => import('@/views/Environment.vue'),
        meta: { title: '环境监测', role: 'ADMIN' }
      },
      {
        path: 'log',
        name: 'AdminLog',
        component: () => import('@/views/Log.vue'),
        meta: { title: '操作日志', role: 'ADMIN' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', role: 'ADMIN' }
      },
      {
        path: 'user-list',
        name: 'AdminUserList',
        component: () => import('@/views/UserList.vue'),
        meta: { title: '用户管理', role: 'ADMIN' }
      },
      {
        path: 'supplier',
        name: 'AdminSupplier',
        component: () => import('@/views/Supplier.vue'),
        meta: { title: '供应商管理', role: 'ADMIN' }
      },
      {
        path: 'customer',
        name: 'AdminCustomer',
        component: () => import('@/views/Customer.vue'),
        meta: { title: '客户管理', role: 'ADMIN' }
      },
      {
        path: 'report-inbound',
        name: 'AdminReportInbound',
        component: () => import('@/views/ReportInbound.vue'),
        meta: { title: '入库统计报表', role: 'ADMIN' }
      },
      {
        path: 'report-outbound',
        name: 'AdminReportOutbound',
        component: () => import('@/views/ReportOutbound.vue'),
        meta: { title: '出库统计报表', role: 'ADMIN' }
      },
      {
        path: 'report-waste',
        name: 'AdminReportWaste',
        component: () => import('@/views/ReportWaste.vue'),
        meta: { title: '损耗统计报表', role: 'ADMIN' }
      },
      {
        path: 'system-config',
        name: 'AdminSystemConfig',
        component: () => import('@/views/SystemConfig.vue'),
        meta: { title: '系统设置', role: 'ADMIN' }
      },
      {
        path: 'inventory-check',
        name: 'AdminInventoryCheck',
        component: () => import('@/views/InventoryCheck.vue'),
        meta: { title: '库存盘点', role: 'ADMIN' }
      },
      {
        path: 'backup',
        name: 'AdminBackup',
        component: () => import('@/views/Backup.vue'),
        meta: { title: '数据备份', role: 'ADMIN' }
      },
      {
        path: 'help',
        name: 'AdminHelp',
        component: () => import('@/views/Help.vue'),
        meta: { title: '操作指南', role: 'ADMIN' }
      }
    ]
  },
  // 仓库管理员路由
  {
    path: '/warehouse',
    component: Layout,
    redirect: '/warehouse/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'WarehouseDashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', role: 'WAREHOUSE' }
      },
      {
        path: 'product',
        name: 'WarehouseProduct',
        component: () => import('@/views/Product.vue'),
        meta: { title: '农产品管理', role: 'WAREHOUSE' }
      },
      {
        path: 'location',
        name: 'WarehouseLocation',
        component: () => import('@/views/Location.vue'),
        meta: { title: '货位管理', role: 'WAREHOUSE' }
      },
      {
        path: 'inbound',
        name: 'WarehouseInbound',
        component: () => import('@/views/Inbound.vue'),
        meta: { title: '入库管理', role: 'WAREHOUSE' }
      },
      {
        path: 'outbound',
        name: 'WarehouseOutbound',
        component: () => import('@/views/Outbound.vue'),
        meta: { title: '出库管理', role: 'WAREHOUSE' }
      },
      {
        path: 'alert',
        name: 'WarehouseAlert',
        component: () => import('@/views/Alert.vue'),
        meta: { title: '预警中心', role: 'WAREHOUSE' }
      },
      {
        path: 'environment',
        name: 'WarehouseEnvironment',
        component: () => import('@/views/Environment.vue'),
        meta: { title: '环境监测', role: 'WAREHOUSE' }
      },
      {
        path: 'profile',
        name: 'WarehouseProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', role: 'WAREHOUSE' }
      }
    ]
  },
  // 客户路由
  {
    path: '/customer',
    component: Layout,
    redirect: '/customer/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'CustomerDashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', role: 'CUSTOMER' }
      },
      {
        path: 'product',
        name: 'CustomerProduct',
        component: () => import('@/views/Product.vue'),
        meta: { title: '农产品查看', role: 'CUSTOMER' }
      },
      {
        path: 'location',
        name: 'CustomerLocation',
        component: () => import('@/views/Location.vue'),
        meta: { title: '货位查看', role: 'CUSTOMER' }
      },
      {
        path: 'alert',
        name: 'CustomerAlert',
        component: () => import('@/views/Alert.vue'),
        meta: { title: '预警中心', role: 'CUSTOMER' }
      },
      {
        path: 'environment',
        name: 'CustomerEnvironment',
        component: () => import('@/views/Environment.vue'),
        meta: { title: '环境监测', role: 'CUSTOMER' }
      },
      {
        path: 'profile',
        name: 'CustomerProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', role: 'CUSTOMER' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 未登录跳转登录页
  if (to.path !== '/login' && !token) {
    return '/login'
  }

  // 已登录访问登录页，根据角色跳转
  if (to.path === '/login' && token) {
    if (role === 'ADMIN') return '/admin/dashboard'
    if (role === 'WAREHOUSE') return '/warehouse/dashboard'
    return '/customer/dashboard'
  }

  // 角色权限验证
  if (token && to.meta?.role) {
    if (to.meta.role !== role) {
      if (role === 'ADMIN') return '/admin/dashboard'
      if (role === 'WAREHOUSE') return '/warehouse/dashboard'
      return '/customer/dashboard'
    }
  }

  return true
})

export default router