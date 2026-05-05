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
              <div class="stat-label">入库总数量(kg)</div>
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
              <div class="stat-label">入库总批次</div>
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
              <span>📊 每日入库趋势</span>
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
              <span>🥬 按商品统计</span>
            </div>
          </template>
          <div ref="productChartRef" class="chart" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>🏭 按供应商统计</span>
            </div>
          </template>
          <div ref="supplierChartRef" class="chart" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📋 入库明细</span>
        </div>
      </template>
      <el-table :data="inboundRecords" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="supplierName" label="供应商" width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="locationCode" label="货位编号" width="120" />
        <el-table-column prop="expiryDate" label="过期日期" width="120" />
        <el-table-column prop="inboundTime" label="入库时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const dateRange = ref([])
const statistics = ref({})
const inboundRecords = ref([])

const lineChartRef = ref(null)
const productChartRef = ref(null)
const supplierChartRef = ref(null)

let lineChart = null
let productChart = null
let supplierChart = null

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    let url = '/inbound/statistics'
    if (dateRange.value && dateRange.value.length === 2) {
      url += `?startDate=${dateRange.value[0]}&endDate=${dateRange.value[1]}`
    }
    const res = await api.get(url)
    statistics.value = res

    // 同时获取入库记录用于表格
    const records = await api.getInboundRecords()
    inboundRecords.value = records || []

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
  // 每日入库趋势折线图
  if (lineChartRef.value) {
    if (lineChart) lineChart.dispose()
    lineChart = echarts.init(lineChartRef.value)

    const dailyStats = statistics.value.dailyStats || {}
    const dates = Object.keys(dailyStats).sort()
    const quantities = dates.map(date => dailyStats[date])

    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates, name: '日期' },
      yAxis: { type: 'value', name: '入库数量(kg)' },
      series: [{
        data: quantities,
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        lineStyle: { color: '#409EFF', width: 3 },
        itemStyle: { color: '#409EFF' }
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

  // 供应商统计饼图
  if (supplierChartRef.value) {
    if (supplierChart) supplierChart.dispose()
    supplierChart = echarts.init(supplierChartRef.value)

    const supplierStats = statistics.value.supplierStats || {}
    const supplierData = Object.entries(supplierStats).map(([name, value]) => ({ name, value }))

    supplierChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: '50%',
        data: supplierData,
        emphasis: { scale: true }
      }]
    })
  }
}

// 重置筛选
const resetFilter = () => {
  dateRange.value = []
  fetchStatistics()
}

// 窗口大小变化时重新调整图表
window.addEventListener('resize', () => {
  if (lineChart) lineChart.resize()
  if (productChart) productChart.resize()
  if (supplierChart) supplierChart.resize()
})

onMounted(() => {
  fetchStatistics()
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