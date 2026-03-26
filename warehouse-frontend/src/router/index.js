import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  // 管理员路由
  {
    path: '/admin',
    component: () => import('@/components/AdminLayout.vue'),
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
      }
    ]
  },
  // 仓库管理员路由
  {
    path: '/warehouse',
    component: () => import('@/components/WarehouseLayout.vue'),
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
      }
    ]
  },
  // 客户路由
  {
    path: '/customer',
    component: () => import('@/components/CustomerLayout.vue'),
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

  // 角色权限验证：检查用户是否有权限访问该路由
  if (token && to.meta?.role) {
    if (to.meta.role !== role) {
      // 无权限，跳转到对应角色的首页
      if (role === 'ADMIN') return '/admin/dashboard'
      if (role === 'WAREHOUSE') return '/warehouse/dashboard'
      return '/customer/dashboard'
    }
  }

  return true
})

export default router