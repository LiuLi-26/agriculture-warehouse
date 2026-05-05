<template>
  <div class="user-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option label="系统管理员" value="ADMIN" />
            <el-option label="仓库管理员" value="WAREHOUSE" />
            <el-option label="客户" value="CUSTOMER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon admin">
              <el-icon><Crown /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.adminCount }}</div>
              <div class="stat-label">管理员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon customer">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.customerCount }}</div>
              <div class="stat-label">客户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户表格 -->
    <el-card class="table-card" shadow="hover">
      <el-table :data="filteredData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditRole(row)">修改角色</el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              :disabled="row.id === currentUserId"
            >
              删除
            </el-button>
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

    <!-- 修改角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="400px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="用户名">
          <span>{{ roleForm.username }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="roleForm.role" placeholder="请选择角色">
            <el-option label="系统管理员" value="ADMIN" />
            <el-option label="仓库管理员" value="WAREHOUSE" />
            <el-option label="客户" value="CUSTOMER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRoleChange" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const submitting = ref(false)
const roleDialogVisible = ref(false)
const currentUserId = ref(localStorage.getItem('token') || 1)

const searchForm = reactive({
  username: '',
  role: ''
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const roleForm = reactive({
  userId: null,
  username: '',
  role: ''
})

// 统计数据
const stats = computed(() => {
  const total = tableData.value.length
  const adminCount = tableData.value.filter(item => item.role === 'ADMIN').length
  const customerCount = tableData.value.filter(item => item.role === 'CUSTOMER').length
  return { total, adminCount, customerCount }
})

// 筛选后的数据
const filteredData = computed(() => {
  let data = [...tableData.value]

  if (searchForm.username) {
    data = data.filter(item => item.username.includes(searchForm.username))
  }

  if (searchForm.role) {
    data = data.filter(item => item.role === searchForm.role)
  }

  page.total = data.length
  const start = (page.current - 1) * page.size
  const end = start + page.size
  return data.slice(start, end)
})

// 获取角色标签类型
const getRoleTagType = (role) => {
  if (role === 'ADMIN') return 'danger'
  if (role === 'WAREHOUSE') return 'warning'
  return 'success'
}

// 获取角色文本
const getRoleText = (role) => {
  if (role === 'ADMIN') return '系统管理员'
  if (role === 'WAREHOUSE') return '仓库管理员'
  return '客户'
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await api.get('/user/list')
    tableData.value = res || []
    page.total = res?.length || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  page.current = 1
}

// 重置搜索
const resetSearch = () => {
  searchForm.username = ''
  searchForm.role = ''
  page.current = 1
}

// 更新分页
const updatePage = () => {
  // 分页变化时自动更新显示
}

// 编辑角色
const handleEditRole = (row) => {
  roleForm.userId = row.id
  roleForm.username = row.username
  roleForm.role = row.role
  roleDialogVisible.value = true
}

// 提交角色修改
const submitRoleChange = async () => {
  submitting.value = true
  try {
    await api.put(`/user/${roleForm.userId}/role`, { role: roleForm.role })
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error('修改角色失败:', error)
  } finally {
    submitting.value = false
  }
}

// 删除用户
const handleDelete = (row) => {
  if (row.id === currentUserId.value) {
    ElMessage.warning('不能删除自己的账号')
    return
  }

  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.delete(`/user/${row.id}`)
      ElMessage.success('删除成功')
      fetchUserList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-container {
  padding: 0;
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
.stat-icon.admin { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.customer { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }

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

.table-card {
  border-radius: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>