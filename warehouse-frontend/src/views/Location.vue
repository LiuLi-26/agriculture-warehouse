<template>
  <div class="location-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="区域">
          <el-select v-model="searchForm.zone" placeholder="请选择区域" clearable>
            <el-option label="A区" value="A区" />
            <el-option label="B区" value="B区" />
            <el-option label="冷藏区" value="冷藏区" />
            <el-option label="冷冻区" value="冷冻区" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="空闲" value="false" />
            <el-option label="占用" value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleAdd">新增货位</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总货位数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon free">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.free }}</div>
              <div class="stat-label">空闲货位</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon occupied">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.occupied }}</div>
              <div class="stat-label">占用货位</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon remaining">
              <el-icon><PieChart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalRemainingCapacity }}</div>
              <div class="stat-label">总剩余容量(kg)</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-card class="table-card" shadow="hover">
      <el-table :data="filteredData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="locationCode" label="货位编号" min-width="120" />
        <el-table-column prop="zone" label="区域" width="100">
          <template #default="{ row }">
            <el-tag :type="getZoneTagType(row.zone)">
              {{ row.zone }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="总容量(kg)" width="100" />
        <el-table-column prop="remainingCapacity" label="剩余容量(kg)" width="150">
          <template #default="{ row }">
            <div v-if="row.capacity">
              <el-progress
                :percentage="getUsagePercent(row)"
                :color="getUsageColor(row)"
                :format="() => `${row.remainingCapacity || 0} / ${row.capacity}`"
              />
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isOccupied" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isOccupied ? 'danger' : 'success'">
              {{ row.isOccupied ? '占用' : '空闲' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentProductId" label="存放商品ID" width="120">
          <template #default="{ row }">
            {{ row.currentProductId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              :disabled="row.isOccupied"
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="货位编号" prop="locationCode">
          <el-input v-model="form.locationCode" placeholder="如: A-01-01" />
        </el-form-item>
        <el-form-item label="区域" prop="zone">
          <el-select v-model="form.zone" placeholder="请选择区域">
            <el-option label="A区" value="A区" />
            <el-option label="B区" value="B区" />
            <el-option label="冷藏区" value="冷藏区" />
            <el-option label="冷冻区" value="冷冻区" />
          </el-select>
        </el-form-item>
        <el-form-item label="容量(kg)" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
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
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

const searchForm = reactive({
  zone: '',
  status: ''
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const form = reactive({
  locationCode: '',
  zone: '',
  capacity: 100
})

const rules = {
  locationCode: [{ required: true, message: '请输入货位编号', trigger: 'blur' }],
  zone: [{ required: true, message: '请选择区域', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}

// 统计数据
const stats = computed(() => {
  const total = tableData.value.length
  const free = tableData.value.filter(item => !item.isOccupied).length
  const occupied = total - free
  return { total, free, occupied }
})

// 总剩余容量
const totalRemainingCapacity = computed(() => {
  return tableData.value.reduce((sum, item) => {
    return sum + (item.remainingCapacity || 0)
  }, 0)
})

// 使用率
const usageRate = computed(() => {
  if (stats.value.total === 0) return 0
  return Math.round((stats.value.occupied / stats.value.total) * 100)
})

// 筛选后的数据
const filteredData = computed(() => {
  let data = [...tableData.value]

  if (searchForm.zone) {
    data = data.filter(item => item.zone === searchForm.zone)
  }

  if (searchForm.status !== '') {
    const isOccupied = searchForm.status === 'true'
    data = data.filter(item => item.isOccupied === isOccupied)
  }

  page.total = data.length
  const start = (page.current - 1) * page.size
  const end = start + page.size
  return data.slice(start, end)
})

// 计算使用率百分比
const getUsagePercent = (row) => {
  if (!row.capacity || row.capacity === 0) return 0
  const used = row.capacity - (row.remainingCapacity || 0)
  return Math.round((used / row.capacity) * 100)
}

// 根据使用率获取进度条颜色
const getUsageColor = (row) => {
  const percent = getUsagePercent(row)
  if (percent >= 90) return '#f56c6c'
  if (percent >= 70) return '#e6a23c'
  return '#67c23a'
}

// 获取区域标签类型
const getZoneTagType = (zone) => {
  if (zone === '冷藏区') return 'info'
  if (zone === '冷冻区') return 'danger'
  return 'success'
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getLocations()
    tableData.value = res || []
    page.total = res?.length || 0
  } catch (error) {
    console.error('获取数据失败:', error)
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
  searchForm.zone = ''
  searchForm.status = ''
  page.current = 1
}

// 更新分页
const updatePage = () => {
  // 分页变化时自动更新显示
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增货位'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑货位'
  Object.assign(form, {
    locationCode: row.locationCode,
    zone: row.zone,
    capacity: row.capacity
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  if (row.isOccupied) {
    ElMessage.warning('货位被占用，无法删除')
    return
  }

  ElMessageBox.confirm('确定要删除该货位吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.deleteLocation(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await api.updateLocation(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await api.createLocation(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.locationCode = ''
  form.zone = ''
  form.capacity = 100
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.location-container {
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
.stat-icon.free { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-icon.occupied { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.remaining { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

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