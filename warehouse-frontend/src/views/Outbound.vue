<template>
  <div class="outbound-container">
    <!-- 出库表单 -->
    <el-card class="form-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>商品出库</span>
          <span class="header-tip">先进先出规则，优先出库临近过期的商品</span>
        </div>
      </template>

      <el-form :model="outboundForm" :rules="outboundRules" ref="outboundFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="商品" prop="productId">
              <el-select v-model="outboundForm.productId" placeholder="请选择商品" filterable @change="handleProductChange">
                <el-option
                  v-for="item in productList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出库数量" prop="quantity">
              <el-input-number
                v-model="outboundForm.quantity"
                :min="1"
                :max="maxQuantity || 1"
                :disabled="!outboundForm.productId"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="当前库存">
              <el-tag type="info" size="large">{{ currentStock }} 件</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" @click="handleOutbound" :loading="outboundLoading" :disabled="!canOutbound">
            执行出库
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 库存批次信息 -->
    <el-card v-if="stockBatches.length > 0" class="batch-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>库存批次（按过期时间排序）</span>
          <el-tag type="warning">先进先出</el-tag>
        </div>
      </template>
      <el-table :data="stockBatches" stripe>
        <el-table-column prop="batchId" label="批次ID" width="80" />
        <el-table-column prop="locationCode" label="货位编号" width="120" />
        <el-table-column prop="quantity" label="库存数量" width="100" />
        <el-table-column prop="expiryDate" label="过期日期" width="120">
          <template #default="{ row }">
            <el-tag :type="getExpiryTagType(row.expiryDate)">
              {{ row.expiryDate }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remainingDays" label="剩余天数" width="100">
          <template #default="{ row }">
            <span :style="{ color: getRemainingDaysColor(row.remainingDays) }">
              {{ row.remainingDays }} 天
            </span>
          </template>
        </el-table-column>
        <el-table-column label="出库优先级">
          <template #default="{ row }">
            <el-progress
              :percentage="getPriorityPercent(row)"
              :color="getPriorityColor(row)"
              :format="() => getPriorityText(row)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card v-else-if="outboundForm.productId" class="batch-card" shadow="hover">
      <el-alert title="该商品暂无库存" type="info" :closable="false" show-icon />
    </el-card>

    <!-- 出库记录列表 -->
    <el-card class="record-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>出库记录</span>
          <el-button type="primary" link @click="fetchOutboundRecords">刷新</el-button>
        </div>
      </template>
      <el-table :data="displayRecords" v-loading="recordLoading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="locationCode" label="货位编号" width="120" />
        <el-table-column prop="outboundTime" label="出库时间" width="180" />
      </el-table>

      <el-empty v-if="outboundRecords.length === 0 && !recordLoading" description="暂无出库记录" />

      <el-pagination
        v-if="outboundRecords.length > 0"
        v-model:current-page="recordPage.current"
        v-model:page-size="recordPage.size"
        :total="recordPage.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

// 响应式数据
const outboundFormRef = ref()
const outboundLoading = ref(false)
const recordLoading = ref(false)
const productList = ref([])
const outboundRecords = ref([])
const stockBatches = ref([])

// 表单数据
const outboundForm = reactive({
  productId: null,
  quantity: 1
})

// 表单验证规则
const outboundRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [
    { required: true, message: '请输入出库数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '出库数量至少为1', trigger: 'blur' }
  ]
}

// 分页数据
const recordPage = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 计算属性
const maxQuantity = computed(() => {
  return stockBatches.value.reduce((sum, batch) => sum + batch.quantity, 0)
})

const currentStock = computed(() => maxQuantity.value)

const canOutbound = computed(() => {
  return outboundForm.productId &&
         outboundForm.quantity > 0 &&
         outboundForm.quantity <= maxQuantity.value &&
         maxQuantity.value > 0
})

// 分页显示的记录
const displayRecords = computed(() => {
  const start = (recordPage.current - 1) * recordPage.size
  const end = start + recordPage.size
  return outboundRecords.value.slice(start, end)
})

// 获取商品列表
const fetchProducts = async () => {
  try {
    const res = await api.getProducts()
    productList.value = res || []
  } catch (error) {
    console.error('获取商品列表失败:', error)
    productList.value = []
  }
}

// 获取库存批次
const fetchStockBatches = async () => {
  if (!outboundForm.productId) {
    stockBatches.value = []
    return
  }

  try {
    const inboundRecords = await api.getInboundRecords()
    if (!inboundRecords || !Array.isArray(inboundRecords)) {
      stockBatches.value = []
      return
    }

    const productRecords = inboundRecords.filter(r => r.productId === outboundForm.productId)

    const sorted = [...productRecords].sort((a, b) => new Date(a.expiryDate) - new Date(b.expiryDate))

    const locations = await api.getLocations()
    const locationMap = new Map()
    if (locations && Array.isArray(locations)) {
      locations.forEach(l => locationMap.set(l.id, l))
    }

    const batches = sorted.map(record => {
      const location = locationMap.get(record.locationId)
      const expiryDate = new Date(record.expiryDate)
      const today = new Date()
      const remainingDays = Math.ceil((expiryDate - today) / (1000 * 60 * 60 * 24))

      return {
        batchId: record.id,
        locationCode: location?.locationCode || '未知',
        quantity: record.quantity,
        expiryDate: record.expiryDate,
        remainingDays: remainingDays
      }
    })

    stockBatches.value = batches
    // 重置出库数量为1，但不超过最大库存
    outboundForm.quantity = Math.min(1, maxQuantity.value || 1)
  } catch (error) {
    console.error('获取库存批次失败:', error)
    stockBatches.value = []
  }
}

// 获取出库记录
const fetchOutboundRecords = async () => {
  recordLoading.value = true
  try {
    const res = await api.getOutboundRecords()
    if (!res || !Array.isArray(res)) {
      outboundRecords.value = []
      recordPage.total = 0
      return
    }

    const recordsWithInfo = await Promise.all(res.map(async record => {
      const product = productList.value.find(p => p.id === record.productId)
      const locations = await api.getLocations()
      const location = locations?.find(l => l.id === record.locationId)
      return {
        ...record,
        productName: product?.name || '未知',
        locationCode: location?.locationCode || '未知'
      }
    }))

    outboundRecords.value = recordsWithInfo
    recordPage.total = recordsWithInfo.length
    recordPage.current = 1
  } catch (error) {
    console.error('获取出库记录失败:', error)
    outboundRecords.value = []
    recordPage.total = 0
  } finally {
    recordLoading.value = false
  }
}

// 分页变化
const handlePageChange = () => {
  // 分页变化时自动更新显示
}

// 商品变化
const handleProductChange = () => {
  outboundForm.quantity = 1
  fetchStockBatches()
}

// 执行出库
const handleOutbound = async () => {
  if (!outboundFormRef.value) return

  try {
    await outboundFormRef.value.validate()
  } catch (error) {
    return
  }

  outboundLoading.value = true

  try {
    const res = await api.outbound({
      productId: outboundForm.productId,
      quantity: outboundForm.quantity
    })

    ElMessage.success(res.message || '出库成功')
    resetForm()
    await fetchStockBatches()
    await fetchOutboundRecords()
  } catch (error) {
    console.error('出库失败:', error)
  } finally {
    outboundLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  outboundForm.productId = null
  outboundForm.quantity = 1
  stockBatches.value = []
  if (outboundFormRef.value) {
    outboundFormRef.value.resetFields()
  }
}

// 辅助函数
const getExpiryTagType = (expiryDate) => {
  const days = Math.ceil((new Date(expiryDate) - new Date()) / (1000 * 60 * 60 * 24))
  if (days < 0) return 'danger'
  if (days < 7) return 'warning'
  return 'success'
}

const getRemainingDaysColor = (days) => {
  if (days < 0) return '#f56c6c'
  if (days < 7) return '#e6a23c'
  return '#67c23a'
}

const getPriorityPercent = (row) => {
  if (row.remainingDays <= 0) return 100
  if (row.remainingDays <= 7) return 80
  if (row.remainingDays <= 30) return 50
  return 20
}

const getPriorityColor = (row) => {
  if (row.remainingDays <= 0) return '#f56c6c'
  if (row.remainingDays <= 7) return '#e6a23c'
  return '#67c23a'
}

const getPriorityText = (row) => {
  if (row.remainingDays <= 0) return '已过期'
  if (row.remainingDays <= 7) return '紧急'
  if (row.remainingDays <= 30) return '正常'
  return '宽松'
}

// 生命周期
onMounted(async () => {
  await fetchProducts()
  await fetchOutboundRecords()
})
</script>

<style scoped>
.outbound-container {
  padding: 0;
}

.form-card, .batch-card, .record-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.header-tip {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>