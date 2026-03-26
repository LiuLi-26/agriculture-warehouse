<template>
  <div class="log-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row" v-if="isAdmin">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ statistics.totalCount }}</div>
          <div class="stat-label">总操作次数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ statistics.loginCount }}</div>
          <div class="stat-label">登录次数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ statistics.createCount }}</div>
          <div class="stat-label">新增操作</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ statistics.updateCount + statistics.deleteCount }}</div>
          <div class="stat-label">修改/删除</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="操作用户" v-if="isAdmin">
          <el-input v-model="searchForm.userId" placeholder="用户ID" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" clearable placeholder="请选择">
            <el-option label="登录" value="LOGIN" />
            <el-option label="新增" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="searchForm.module" clearable placeholder="请选择">
            <el-option label="农产品管理" value="农产品管理" />
            <el-option label="货位管理" value="货位管理" />
            <el-option label="入库管理" value="入库管理" />
            <el-option label="出库管理" value="出库管理" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card class="log-card">
      <el-table :data="logList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTagType(row.operationType)">
              {{ getOperationText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="description" label="操作描述" min-width="200" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时(ms)" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="详情" width="60">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchLogs"
        @current-change="fetchLogs"
        class="pagination"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="操作详情" width="600px">
      <pre class="detail-json">{{ detailData }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const detailVisible = ref(false)
const detailData = ref('')
const isAdmin = ref(localStorage.getItem('role') === 'ADMIN')

const logList = ref([])
const dateRange = ref([])
const statistics = ref({
  totalCount: 0,
  loginCount: 0,
  createCount: 0,
  updateCount: 0,
  deleteCount: 0
})

const searchForm = reactive({
  userId: null,
  operationType: '',
  module: '',
  startTime: null,
  endTime: null,
  page: 1,
  size: 20
})

const page = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 获取操作类型标签
const getOperationTagType = (type) => {
  const map = {
    'LOGIN': 'success',
    'CREATE': 'primary',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'QUERY': 'info'
  }
  return map[type] || 'info'
}

const getOperationText = (type) => {
  const map = {
    'LOGIN': '登录',
    'CREATE': '新增',
    'UPDATE': '更新',
    'DELETE': '删除',
    'QUERY': '查询'
  }
  return map[type] || type
}

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: page.current,
      size: page.size
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const res = await api.getLogs(params)
    logList.value = res.content || []
    page.total = res.totalElements || 0
  } catch (error) {
    console.error('获取日志失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  if (!isAdmin.value) return
  try {
    const res = await api.getLogStatistics()
    statistics.value = res
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  page.current = 1
  fetchLogs()
}

// 重置搜索
const resetSearch = () => {
  searchForm.userId = null
  searchForm.operationType = ''
  searchForm.module = ''
  dateRange.value = []
  page.current = 1
  fetchLogs()
}

// 显示详情
const showDetail = (row) => {
  try {
    const detail = JSON.parse(row.detail || '{}')
    detailData.value = JSON.stringify(detail, null, 2)
  } catch {
    detailData.value = row.detail || '无详情'
  }
  detailVisible.value = true
}

onMounted(() => {
  fetchLogs()
  fetchStatistics()
})
</script>

<style scoped>
.log-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  border-radius: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.search-card, .log-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-json {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 12px;
  max-height: 400px;
  overflow-y: auto;
}
</style>