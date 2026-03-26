<template>
  <div class="inbound-container">
    <!-- 入库表单 -->
    <el-card class="form-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>商品入库</span>
          <span class="header-tip">系统将智能分配最佳货位</span>
        </div>
      </template>

      <el-form :model="inboundForm" :rules="inboundRules" ref="inboundFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="商品" prop="productId">
              <el-select v-model="inboundForm.productId" placeholder="请选择商品" filterable>
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
            <el-form-item label="入库数量" prop="quantity">
              <el-input-number v-model="inboundForm.quantity" :min="1" :max="9999" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="过期日期" prop="expiryDate">
              <el-date-picker
                v-model="inboundForm.expiryDate"
                type="date"
                placeholder="选择过期日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" @click="handleInbound" :loading="inboundLoading">
            执行入库
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 推荐货位展示 -->
    <el-card v-if="recommendLocation" class="recommend-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>智能推荐货位</span>
          <el-tag type="success">评分: {{ recommendScore }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="货位编号">
          <el-tag size="large">{{ recommendLocation.locationCode }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="区域">{{ recommendLocation.zone }}</el-descriptions-item>
        <el-descriptions-item label="容量">{{ recommendLocation.capacity }} kg</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="recommendLocation.isOccupied ? 'danger' : 'success'">
            {{ recommendLocation.isOccupied ? '已占用' : '空闲' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="存放商品ID">
          {{ recommendLocation.currentProductId || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="评分详情">
          <el-tooltip :content="scoreDetail" placement="top">
            <el-button link>查看详情</el-button>
          </el-tooltip>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 入库记录列表 -->
    <el-card class="record-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>入库记录</span>
          <el-button type="primary" link @click="fetchInboundRecords">刷新</el-button>
        </div>
      </template>
      <el-table :data="inboundRecords" v-loading="recordLoading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="locationCode" label="货位编号" width="120" />
        <el-table-column prop="zone" label="区域" width="100" />
        <el-table-column prop="expiryDate" label="过期日期" width="120" />
        <el-table-column prop="inboundTime" label="入库时间" width="180" />
      </el-table>

      <el-pagination
        v-model:current-page="recordPage.current"
        v-model:page-size="recordPage.size"
        :total="recordPage.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchInboundRecords"
        @current-change="fetchInboundRecords"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const inboundFormRef = ref()
const inboundLoading = ref(false)
const recordLoading = ref(false)
const productList = ref([])
const inboundRecords = ref([])
const recommendLocation = ref(null)
const recommendScore = ref(0)
const scoreDetail = ref('')

const inboundForm = reactive({
  productId: null,
  quantity: 1,
  expiryDate: ''
})

const inboundRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }],
  expiryDate: [{ required: true, message: '请选择过期日期', trigger: 'change' }]
}

const recordPage = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取商品列表
const fetchProducts = async () => {
  try {
    const res = await api.getProducts()
    productList.value = res
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

// 获取入库记录
const fetchInboundRecords = async () => {
  recordLoading.value = true
  try {
    const res = await api.getInboundRecords()
    // 关联商品名称
    const recordsWithName = res.map(record => ({
      ...record,
      productName: productList.value.find(p => p.id === record.productId)?.name || '未知'
    }))
    inboundRecords.value = recordsWithName
    recordPage.total = recordsWithName.length
  } catch (error) {
    console.error('获取入库记录失败:', error)
  } finally {
    recordLoading.value = false
  }
}

// 智能分配货位（前端模拟推荐）
const simulateRecommend = async () => {
  const product = productList.value.find(p => p.id === inboundForm.productId)
  if (!product) return

  const locations = await api.getLocations()
  const freeLocations = locations.filter(l => !l.isOccupied)
  const sameProductLocations = locations.filter(l =>
    l.isOccupied && l.currentProductId === inboundForm.productId
  )

  let candidates = [...sameProductLocations, ...freeLocations]
  if (candidates.length === 0) {
    recommendLocation.value = null
    return
  }

  // 简单评分
  let bestLocation = null
  let bestScore = -1
  let bestScoreDetail = ''

  for (const loc of candidates) {
    let score = 0
    let details = []

    // 相同商品
    if (loc.isOccupied && loc.currentProductId === inboundForm.productId) {
      score += 100
      details.push('相同商品 +100')
    }

    // 存储条件匹配
    if (product.storageCondition === '冷藏' && loc.zone === '冷藏区') {
      score += 60
      details.push('冷藏条件匹配 +60')
    } else if (product.storageCondition === '冷冻' && loc.zone === '冷冻区') {
      score += 60
      details.push('冷冻条件匹配 +60')
    } else if ((!product.storageCondition || product.storageCondition === '常温') &&
               !loc.zone.includes('冷藏') && !loc.zone.includes('冷冻')) {
      score += 60
      details.push('常温条件匹配 +60')
    }

    // 靠近出口
    const num = parseInt(loc.locationCode.split('-')[1]) || 1
    score += Math.floor(40 / num)
    details.push(`靠近出口 +${Math.floor(40 / num)}`)

    // 容量充足
    if (loc.capacity >= inboundForm.quantity) {
      score += 20
      details.push('容量充足 +20')
    }

    if (score > bestScore) {
      bestScore = score
      bestLocation = loc
      bestScoreDetail = details.join(' | ')
    }
  }

  recommendLocation.value = bestLocation
  recommendScore.value = bestScore
  scoreDetail.value = bestScoreDetail
}

// 执行入库
const handleInbound = async () => {
  await inboundFormRef.value.validate()
  inboundLoading.value = true

  try {
    const res = await api.inbound({
      productId: inboundForm.productId,
      quantity: inboundForm.quantity,
      expiryDate: inboundForm.expiryDate
    })

    ElMessage.success(res.message)
    resetForm()
    await fetchInboundRecords()
    recommendLocation.value = null
  } catch (error) {
    console.error('入库失败:', error)
  } finally {
    inboundLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  inboundForm.productId = null
  inboundForm.quantity = 1
  inboundForm.expiryDate = ''
  recommendLocation.value = null
  inboundFormRef.value?.resetFields()
}

// 监听商品选择变化
const watchProduct = () => {
  // 商品变化时重新推荐货位
  if (inboundForm.productId) {
    simulateRecommend()
  }
}

// 监听数量变化
const watchQuantity = () => {
  if (inboundForm.productId && inboundForm.quantity) {
    simulateRecommend()
  }
}

// 监听商品和数量变化
import { watch } from 'vue'
watch(() => inboundForm.productId, watchProduct)
watch(() => inboundForm.quantity, watchQuantity)

onMounted(async () => {
  await fetchProducts()
  await fetchInboundRecords()
})
</script>

<style scoped>
.inbound-container {
  padding: 0;
}

.form-card, .recommend-card, .record-card {
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

.recommend-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>