<template>
  <div class="environment-container">
    <!-- 区域环境卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="item in environmentData" :key="item.zone">
        <el-card class="env-card" :class="getStatusClass(item.status)" shadow="hover">
          <div class="env-header">
            <h3>{{ item.zone }}</h3>
            <el-tag :type="getStatusTagType(item.status)" size="large">
              {{ getStatusText(item.status) }}
            </el-tag>
          </div>
          <div class="env-content">
            <div class="env-item">
              <span class="label">温度</span>
              <span class="value" :style="{ color: getTempColor(item) }">
                {{ item.temperature }}℃
              </span>
            </div>
            <div class="env-item">
              <span class="label">湿度</span>
              <span class="value" :style="{ color: getHumidityColor(item) }">
                {{ item.humidity }}%
              </span>
            </div>
            <div class="env-item">
              <span class="label">更新时间</span>
              <span class="value time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 环境预警列表 -->
    <el-card class="alert-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>🌡️ 环境异常预警</span>
          <el-tag type="danger">实时监测</el-tag>
        </div>
      </template>
      <el-table :data="alerts" stripe v-loading="loading">
        <el-table-column prop="zone" label="区域" width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'TEMPERATURE' ? 'danger' : 'info'">
              {{ row.type === 'TEMPERATURE' ? '温度' : '湿度' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentValue" label="当前值" width="120" />
        <el-table-column prop="minValue" label="正常范围" width="180">
          <template #default="{ row }">
            {{ row.minValue }} ~ {{ row.maxValue }}
          </template>
        </el-table-column>
        <el-table-column prop="message" label="预警信息" min-width="250" />
        <el-table-column prop="level" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.level === 'CRITICAL' ? 'danger' : 'warning'">
              {{ row.level === 'CRITICAL' ? '严重' : '警告' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="alerts.length === 0 && !loading" description="暂无环境异常" />
    </el-card>

    <!-- 阈值配置（仅管理员可见） -->
    <el-card v-if="isAdmin" class="threshold-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>⚙️ 环境阈值配置</span>
          <el-button type="primary" link @click="saveThresholds">保存配置</el-button>
        </div>
      </template>
      <el-table :data="thresholds" stripe>
        <el-table-column prop="zone" label="区域" width="120" />
        <el-table-column label="温度范围(℃)" width="250">
          <template #default="{ row }">
            <el-input-number v-model="row.tempMin" :min="-30" :max="row.tempMax" size="small" />
            ~
            <el-input-number v-model="row.tempMax" :min="row.tempMin" :max="50" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="湿度范围(%)" width="250">
          <template #default="{ row }">
            <el-input-number v-model="row.humidityMin" :min="0" :max="row.humidityMax" size="small" />
            ~
            <el-input-number v-model="row.humidityMax" :min="row.humidityMin" :max="100" size="small" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const environmentData = ref([])
const alerts = ref([])
const thresholds = ref([])
const isAdmin = ref(localStorage.getItem('role') === 'ADMIN')

let timer = null

// 获取环境数据
const fetchEnvironmentData = async () => {
  try {
    const res = await api.get('/environment/latest')
    environmentData.value = res || []
  } catch (error) {
    console.error('获取环境数据失败:', error)
  }
}

// 获取预警列表
const fetchAlerts = async () => {
  loading.value = true
  try {
    const res = await api.get('/environment/alerts')
    alerts.value = res || []
  } catch (error) {
    console.error('获取预警失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取阈值配置
const fetchThresholds = async () => {
  if (!isAdmin.value) return
  try {
    const res = await api.get('/environment/thresholds')
    thresholds.value = res || []
  } catch (error) {
    console.error('获取阈值配置失败:', error)
  }
}

// 保存阈值配置
const saveThresholds = async () => {
  try {
    for (const threshold of thresholds.value) {
      await api.put(`/environment/thresholds/${threshold.zone}`, {
        tempMin: threshold.tempMin,
        tempMax: threshold.tempMax,
        humidityMin: threshold.humidityMin,
        humidityMax: threshold.humidityMax
      })
    }
    ElMessage.success('阈值配置已保存')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 辅助函数
const getStatusClass = (status) => {
  if (status === 'CRITICAL') return 'env-critical'
  if (status === 'WARNING') return 'env-warning'
  return ''
}

const getStatusTagType = (status) => {
  if (status === 'CRITICAL') return 'danger'
  if (status === 'WARNING') return 'warning'
  return 'success'
}

const getStatusText = (status) => {
  if (status === 'CRITICAL') return '严重异常'
  if (status === 'WARNING') return '预警'
  return '正常'
}

const getTempColor = (item) => {
  const threshold = thresholds.value.find(t => t.zone === item.zone)
  if (!threshold) return '#333'
  if (item.temperature < threshold.tempMin || item.temperature > threshold.tempMax) return '#f56c6c'
  return '#67c23a'
}

const getHumidityColor = (item) => {
  const threshold = thresholds.value.find(t => t.zone === item.zone)
  if (!threshold) return '#333'
  if (item.humidity < threshold.humidityMin || item.humidity > threshold.humidityMax) return '#f56c6c'
  return '#67c23a'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString()
}

// 启动定时刷新
const startAutoRefresh = () => {
  timer = setInterval(() => {
    fetchEnvironmentData()
    fetchAlerts()
  }, 30000) // 30秒刷新一次
}

onMounted(() => {
  fetchEnvironmentData()
  fetchAlerts()
  fetchThresholds()
  startAutoRefresh()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.environment-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.env-card {
  border-radius: 12px;
  transition: all 0.3s;
}

.env-card.env-critical {
  border-left: 4px solid #f56c6c;
  background: #fef0f0;
}

.env-card.env-warning {
  border-left: 4px solid #e6a23c;
  background: #fdf6ec;
}

.env-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.env-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.env-content {
  display: flex;
  justify-content: space-around;
}

.env-item {
  text-align: center;
}

.env-item .label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.env-item .value {
  font-size: 20px;
  font-weight: bold;
}

.env-item .value.time {
  font-size: 12px;
  font-weight: normal;
  color: #666;
}

.alert-card, .threshold-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>