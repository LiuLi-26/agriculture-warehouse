<template>
  <div class="inventory-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">盘点次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingCount || 0 }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon positive">
              <el-icon><Top /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.positiveDifference || 0 }}</div>
              <div class="stat-label">盘盈数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon negative">
              <el-icon><Bottom /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.negativeDifference || 0 }}</div>
              <div class="stat-label">盘亏数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <el-card class="action-card" shadow="hover">
      <div class="action-bar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增盘点
        </el-button>
        <el-button @click="fetchStatistics">
          <el-icon><Refresh /></el-icon>
          刷新统计
        </el-button>
      </div>
    </el-card>

    <!-- 盘点记录表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📋 盘点记录</span>
        </div>
      </template>
      <el-table :data="checkRecords" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="checkNo" label="盘点单号" width="150" />
        <el-table-column prop="checkDate" label="盘点日期" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="locationCode" label="货位编号" width="120" />
        <el-table-column prop="systemQuantity" label="系统库存" width="100" />
        <el-table-column prop="actualQuantity" label="实际库存" width="100" />
        <el-table-column prop="difference" label="差异" width="80">
          <template #default="{ row }">
            <span :style="{ color: row.difference > 0 ? '#67c23a' : (row.difference < 0 ? '#f56c6c' : '#909399') }">
              {{ row.difference > 0 ? '+' : '' }}{{ row.difference }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="primary"
              link
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增盘点弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增盘点" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable @change="onProductChange">
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="货位" prop="locationId">
          <el-select v-model="form.locationId" placeholder="请选择货位" filterable>
            <el-option
              v-for="item in locationList"
              :key="item.id"
              :label="item.locationCode"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="系统库存" prop="systemQuantity">
          <el-input-number v-model="form.systemQuantity" :min="0" :max="99999" />
        </el-form-item>
        <el-form-item label="实际库存" prop="actualQuantity">
          <el-input-number v-model="form.actualQuantity" :min="0" :max="99999" @change="onActualChange" />
        </el-form-item>
        <el-form-item label="差异" v-if="difference !== null">
          <span :style="{ color: difference > 0 ? '#67c23a' : (difference < 0 ? '#f56c6c' : '#909399') }">
            {{ difference > 0 ? '盘盈 +' : (difference < 0 ? '盘亏 ' : '持平 ') }}{{ Math.abs(difference) }}
          </span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
const formRef = ref()
const productList = ref([])
const locationList = ref([])
const checkRecords = ref([])
const statistics = ref({})

const form = reactive({
  productId: null,
  locationId: null,
  systemQuantity: 0,
  actualQuantity: 0,
  remark: ''
})

const difference = computed(() => {
  return form.actualQuantity - form.systemQuantity
})

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  locationId: [{ required: true, message: '请选择货位', trigger: 'change' }],
  systemQuantity: [{ required: true, message: '请输入系统库存', trigger: 'blur' }],
  actualQuantity: [{ required: true, message: '请输入实际库存', trigger: 'blur' }]
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  if (status === 'PENDING') return 'warning'
  if (status === 'COMPLETED') return 'success'
  return 'info'
}

// 获取商品列表
const fetchProducts = async () => {
  try {
    const res = await api.getProducts()
    productList.value = res || []
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

// 获取货位列表
const fetchLocations = async () => {
  try {
    const res = await api.getLocations()
    locationList.value = res || []
  } catch (error) {
    console.error('获取货位列表失败:', error)
  }
}

// 获取盘点记录
const fetchChecks = async () => {
  loading.value = true
  try {
    const res = await api.get('/inventory/checks')
    checkRecords.value = res || []
  } catch (error) {
    console.error('获取盘点记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取统计
const fetchStatistics = async () => {
  try {
    const res = await api.get('/inventory/checks/statistics')
    statistics.value = res || {}
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

// 商品变化
const onProductChange = () => {
  // 可以根据选择的商品获取系统库存
}

// 实际库存变化
const onActualChange = () => {
  // 差异自动计算
}

// 新增盘点
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 确认盘点
const handleConfirm = (row) => {
  ElMessageBox.confirm(`确定要确认盘点单 "${row.checkNo}" 吗？确认后将更新库存。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.put(`/inventory/checks/${row.id}/confirm`)
      ElMessage.success('确认成功')
      await fetchChecks()
      await fetchStatistics()
    } catch (error) {
      console.error('确认失败:', error)
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await api.post('/inventory/checks', form)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    await fetchChecks()
    await fetchStatistics()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.productId = null
  form.locationId = null
  form.systemQuantity = 0
  form.actualQuantity = 0
  form.remark = ''
}

onMounted(() => {
  fetchProducts()
  fetchLocations()
  fetchChecks()
  fetchStatistics()
})
</script>

<style scoped>
.inventory-container {
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
.stat-icon.pending { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.positive { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-icon.negative { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

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

.action-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.action-bar {
  display: flex;
  gap: 10px;
}

.table-card {
  border-radius: 12px;
}

.card-header {
  font-weight: bold;
}
</style>