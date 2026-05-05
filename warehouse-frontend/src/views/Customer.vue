<template>
  <div class="customer-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">客户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon active">
              <el-icon><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.activeCount || 0 }}</div>
              <div class="stat-label">活跃客户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon new">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.newCount || 0 }}</div>
              <div class="stat-label">本月新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.keyword" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 客户表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>👥 客户列表</span>
          <el-button type="primary" link @click="fetchCustomers">刷新</el-button>
        </div>
      </template>
      <el-table :data="filteredData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag type="success">客户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '正常' ? 'success' : 'danger'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '正常'"
              type="warning"
              link
              @click="handleDisable(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              link
              @click="handleEnable(row)"
            >
              启用
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="updatePage"
        @current-change="updatePage"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const customerList = ref([])
const statistics = ref({})

const searchForm = reactive({
  keyword: ''
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 筛选后的数据
const filteredData = computed(() => {
  let data = [...customerList.value]

  if (searchForm.keyword) {
    data = data.filter(item => item.username.includes(searchForm.keyword))
  }

  page.total = data.length
  const start = (page.current - 1) * page.size
  const end = start + page.size
  return data.slice(start, end)
})

// 获取客户列表
const fetchCustomers = async () => {
  loading.value = true
  try {
    const res = await api.get('/customers')
    customerList.value = res || []
  } catch (error) {
    console.error('获取客户列表失败:', error)
    customerList.value = []
  } finally {
    loading.value = false
  }
}

// 获取统计
const fetchStatistics = async () => {
  try {
    const res = await api.get('/customers/statistics')
    statistics.value = res || {}
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  page.current = 1
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  page.current = 1
}

// 更新分页
const updatePage = () => {}

// 禁用客户
const handleDisable = (row) => {
  ElMessageBox.confirm(`确定要禁用客户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.put(`/customers/${row.id}/disable`)
      ElMessage.success('禁用成功')
      // 重新加载数据
      await fetchCustomers()
      await fetchStatistics()
    } catch (error) {
      console.error('禁用失败:', error)
      ElMessage.error('禁用失败')
    }
  })
}

// 启用客户
const handleEnable = (row) => {
  ElMessageBox.confirm(`确定要启用客户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await api.put(`/customers/${row.id}/enable`)
      ElMessage.success('启用成功')
      // 重新加载数据
      await fetchCustomers()
      await fetchStatistics()
    } catch (error) {
      console.error('启用失败:', error)
      ElMessage.error('启用失败')
    }
  })
}

// 删除客户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除客户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.delete(`/customers/${row.id}`)
      ElMessage.success('删除成功')
      await fetchCustomers()
      await fetchStatistics()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  fetchCustomers()
  fetchStatistics()
})
</script>

<style scoped>
.customer-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon .el-icon {
  font-size: 28px;
  color: white;
}

.stat-icon.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.active { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-icon.new { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.table-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>