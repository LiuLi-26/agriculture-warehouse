<template>
  <div class="report-container">
    <!-- 日期筛选 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchStatistics">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleAdd">新增损耗记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="12">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalQuantity || 0 }}</div>
              <div class="stat-label">损耗总数量(kg)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon count">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">损耗总批次</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📊 每日损耗趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>🥬 按商品统计损耗</span>
            </div>
          </template>
          <div ref="productChartRef" class="chart" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>⚠️ 按损耗类型统计</span>
            </div>
          </template>
          <div ref="typeChartRef" class="chart" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📋 损耗明细</span>
        </div>
      </template>
      <el-table :data="wasteRecords" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="wasteTypeName" label="损耗类型" width="120" />
        <el-table-column prop="wasteDate" label="损耗日期" width="120" />
        <el-table-column prop="reason" label="原因" min-width="200" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增损耗记录弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增损耗记录" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable>
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="损耗数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="损耗类型" prop="wasteType">
          <el-select v-model="form.wasteType" placeholder="请选择损耗类型">
            <el-option label="过期损耗" value="EXPIRED" />
            <el-option label="损坏损耗" value="DAMAGED" />
            <el-option label="其他损耗" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="损耗日期" prop="wasteDate">
          <el-date-picker
            v-model="form.wasteDate"
            type="date"
            placeholder="选择损耗日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入损耗原因" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const dateRange = ref([])
const statistics = ref({})
const wasteRecords = ref([])
const productList = ref([])

const lineChartRef = ref(null)
const productChartRef = ref(null)
const typeChartRef = ref(null)

let lineChart = null
let productChart = null
let typeChart = null

const form = reactive({
  productId: null,
  quantity: 1,
  wasteType: 'EXPIRED',
  wasteDate: '',
  reason: ''
})

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入损耗数量', trigger: 'blur' }],
  wasteType: [{ required: true, message: '请选择损耗类型', trigger: 'change' }],
  wasteDate: [{ required: true, message: '请选择损耗日期', trigger: 'change' }]
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

// 获取损耗记录
const fetchWasteRecords = async () => {
  try {
    const res = await api.get('/waste')
    wasteRecords.value = res || []
  } catch (error) {
    console.error('获取损耗记录失败:', error)
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    let url = '/waste/statistics'
    if (dateRange.value && dateRange.value.length === 2) {
      url += `?startDate=${dateRange.value[0]}&endDate=${dateRange.value[1]}`
    }
    const res = await api.get(url)
    statistics.value = res

    // 渲染图表
    renderCharts()
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 渲染图表
const renderCharts = () => {
  // 每日损耗趋势折线图
  if (lineChartRef.value) {
    if (lineChart) lineChart.dispose()
    lineChart = echarts.init(lineChartRef.value)

    const dailyStats = statistics.value.dailyStats || {}
    const dates = Object.keys(dailyStats).sort()
    const quantities = dates.map(date => dailyStats[date])

    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates, name: '日期' },
      yAxis: { type: 'value', name: '损耗数量(kg)' },
      series: [{
        data: quantities,
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        lineStyle: { color: '#F56C6C', width: 3 },
        itemStyle: { color: '#F56C6C' }
      }]
    })
  }

  // 商品统计饼图
  if (productChartRef.value) {
    if (productChart) productChart.dispose()
    productChart = echarts.init(productChartRef.value)

    const productStats = statistics.value.productStats || {}
    const productData = Object.entries(productStats).map(([name, value]) => ({ name, value }))

    productChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: '50%',
        data: productData,
        emphasis: { scale: true }
      }]
    })
  }

  // 损耗类型统计饼图
  if (typeChartRef.value) {
    if (typeChart) typeChart.dispose()
    typeChart = echarts.init(typeChartRef.value)

    const typeStats = statistics.value.typeStats || {}
    const typeData = Object.entries(typeStats).map(([name, value]) => ({ name, value }))

    typeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: '50%',
        data: typeData,
        emphasis: { scale: true }
      }]
    })
  }
}

// 新增损耗记录
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await api.post('/waste', form)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    await fetchWasteRecords()
    await fetchStatistics()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 删除损耗记录
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该损耗记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.delete(`/waste/${row.id}`)
      ElMessage.success('删除成功')
      await fetchWasteRecords()
      await fetchStatistics()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

// 重置筛选
const resetFilter = () => {
  dateRange.value = []
  fetchStatistics()
}

// 重置表单
const resetForm = () => {
  form.productId = null
  form.quantity = 1
  form.wasteType = 'EXPIRED'
  form.wasteDate = ''
  form.reason = ''
}

// 窗口大小变化时重新调整图表
window.addEventListener('resize', () => {
  if (lineChart) lineChart.resize()
  if (productChart) productChart.resize()
  if (typeChart) typeChart.resize()
})

onMounted(async () => {
  await fetchProducts()
  await fetchWasteRecords()
  await fetchStatistics()
})
</script>

<style scoped>
.report-container {
  padding: 0;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.filter-form {
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
.stat-icon.count { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }

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

.charts-row {
  margin-bottom: 20px;
}

.chart-card, .table-card {
  border-radius: 12px;
}

.card-header {
  font-weight: bold;
}

.chart {
  width: 100%;
}
</style>