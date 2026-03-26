<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon blue">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.products }}</div>
              <div class="stat-label">农产品种类</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon green">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.locations }}</div>
              <div class="stat-label">货位总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon orange">
              <el-icon><Upload /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inboundCount }}</div>
              <div class="stat-label">入库次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon red">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.alerts }}</div>
              <div class="stat-label">预警数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>库存占比</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>出入库趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近入库记录 -->
    <el-card class="recent-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>最近入库记录</span>
          <el-button type="primary" link @click="goToInbound">查看更多</el-button>
        </div>
      </template>
      <el-table :data="recentInbound" style="width: 100%">
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="quantity" label="数量" />
        <el-table-column prop="locationCode" label="货位" />
        <el-table-column prop="inboundTime" label="入库时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import api from '@/api'

const router = useRouter()
const pieChartRef = ref(null)
const lineChartRef = ref(null)

const stats = ref({
  products: 0,
  locations: 0,
  inboundCount: 0,
  alerts: 0
})

const recentInbound = ref([])

// 获取统计数据
const fetchStats = async () => {
  try {
    const [products, locations, inbound] = await Promise.all([
      api.getProducts(),
      api.getLocations(),
      api.getInboundRecords()
    ])

    stats.value.products = products.length
    stats.value.locations = locations.length
    stats.value.inboundCount = inbound.length
    stats.value.alerts = 0  // 预警功能后续添加

    // 最近5条入库记录
    recentInbound.value = inbound.slice(-5).reverse().map(record => ({
      ...record,
      productName: products.find(p => p.id === record.productId)?.name || '未知'
    }))

    // 初始化图表
    initCharts(products, inbound)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 初始化图表
const initCharts = (products, inbound) => {
  // 饼图：各品类库存占比
  const categoryMap = {}
  products.forEach(p => {
    const category = p.category || '其他'
    categoryMap[category] = (categoryMap[category] || 0) + 1
  })

  const pieData = Object.entries(categoryMap).map(([name, value]) => ({ name, value }))

  const pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: pieData,
      emphasis: { scale: true }
    }]
  })

  // 折线图：近7天出入库趋势
  const last7Days = []
  const inboundData = []
  const outboundData = []

  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    last7Days.push(`${date.getMonth() + 1}/${date.getDate()}`)
    inboundData.push(0)
    outboundData.push(0)
  }

  // 这里可以统计实际出入库数据，暂时用模拟数据
  const lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['入库', '出库'] },
    xAxis: { type: 'category', data: last7Days },
    yAxis: { type: 'value' },
    series: [
      { name: '入库', type: 'line', data: [12, 18, 25, 22, 30, 28, 35], smooth: true },
      { name: '出库', type: 'line', data: [8, 15, 20, 18, 25, 22, 30], smooth: true }
    ]
  })
}

const goToInbound = () => {
  router.push('/inbound')
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard {
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

.stat-icon.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-icon.orange { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.red { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

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

.chart-card .chart {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.recent-card {
  border-radius: 12px;
}
</style>