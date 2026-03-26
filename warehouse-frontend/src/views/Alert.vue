<template>
  <div class="alert-container">
    <!-- 预警统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon expiry">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.expiryCount }}</div>
              <div class="stat-label">即将过期商品</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon lowStock">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lowStockCount }}</div>
              <div class="stat-label">库存不足商品</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon expired">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.expiredCount }}</div>
              <div class="stat-label">已过期商品</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon healthy">
              <el-icon><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.healthyCount }}</div>
              <div class="stat-label">健康库存</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 保质期预警（按商品阈值） -->
    <el-card class="alert-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📅 保质期预警（按商品独立阈值）</span>
          <el-tag type="warning">根据商品特性智能预警</el-tag>
        </div>
      </template>
      <el-table :data="expiryAlerts" stripe v-loading="loading">
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="alertThreshold" label="预警阈值" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.alertThreshold }} 天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="库存数量" width="100" />
        <el-table-column prop="locationCode" label="货位编号" width="120" />
        <el-table-column prop="expiryDate" label="过期日期" width="120">
          <template #default="{ row }">
            <el-tag :type="getExpiryAlertType(row.remainingDays, row.alertThreshold)">
              {{ row.expiryDate }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remainingDays" label="剩余天数" width="100">
          <template #default="{ row }">
            <span :style="{ color: getRemainingDaysColor(row.remainingDays, row.alertThreshold), fontWeight: 'bold' }">
              {{ row.remainingDays }} 天
            </span>
          </template>
        </el-table-column>
        <el-table-column label="紧急程度" width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="getExpiryPercent(row.remainingDays, row.alertThreshold)"
              :color="getExpiryProgressColor(row.remainingDays, row.alertThreshold)"
              :format="() => getExpiryLevel(row.remainingDays, row.alertThreshold)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleOutbound(row)">立即出库</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="expiryAlerts.length === 0 && !loading" description="暂无保质期预警" />
    </el-card>

    <!-- 库存阈值预警 -->
    <el-card class="alert-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📦 库存阈值预警</span>
          <el-tag type="danger">库存不足的商品</el-tag>
        </div>
      </template>
      <el-table :data="lowStockAlerts" stripe v-loading="loading">
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="totalStock" label="当前库存" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">{{ row.totalStock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stockThreshold" label="库存阈值" width="100">
          <template #default="{ row }">
            <el-tag size="small">≤ {{ row.stockThreshold }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warningLevel" label="预警级别" width="120">
          <template #default="{ row }">
            <el-tag :type="getLowStockTagType(row.warningLevel)">
              {{ row.warningLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="suggestion" label="建议" min-width="200" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleInbound(row)">立即入库</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="lowStockAlerts.length === 0 && !loading" description="暂无库存预警" />
    </el-card>

    <!-- 预警设置 -->
    <el-card class="setting-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>⚙️ 预警设置</span>
          <el-button type="primary" link @click="saveSettings">保存设置</el-button>
        </div>
      </template>
      <el-form :model="settings" label-width="150px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="全局库存预警阈值">
              <el-slider v-model="settings.stockThreshold" :min="10" :max="200" show-input />
              <span class="setting-tip">库存 ≤ {{ settings.stockThreshold }} 件时预警</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品保质期阈值">
              <el-button type="primary" link @click="openProductThresholdDialog">
                按商品单独设置
              </el-button>
              <span class="setting-tip">点击为不同商品设置独立的预警阈值</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 商品阈值设置弹窗 -->
    <el-dialog v-model="thresholdDialogVisible" title="商品保质期阈值设置" width="600px">
      <el-table :data="productList" max-height="400">
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="shelfLifeDays" label="保质期(天)" width="100" />
        <el-table-column label="预警阈值(天)" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.alertThreshold"
              :min="1"
              :max="row.shelfLifeDays || 180"
              :step="1"
              size="small"
              controls-position="right"
            />
          </template>
        </el-table-column>
        <el-table-column label="建议值" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">
              {{ getSuggestedThreshold(row.shelfLifeDays) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="thresholdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProductThresholds" :loading="savingThresholds">
          保存设置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()
const loading = ref(false)
const savingThresholds = ref(false)
const thresholdDialogVisible = ref(false)

// 统计数据
const stats = reactive({
  expiryCount: 0,
  lowStockCount: 0,
  expiredCount: 0,
  healthyCount: 0
})

// 保质期预警数据
const expiryAlerts = ref([])

// 库存不足预警数据
const lowStockAlerts = ref([])

// 商品列表（用于阈值设置）
const productList = ref([])

// 预警设置
const settings = reactive({
  stockThreshold: 50
})

// 根据保质期获取建议阈值
const getSuggestedThreshold = (shelfLifeDays) => {
  if (!shelfLifeDays) return 7
  if (shelfLifeDays <= 7) return Math.max(1, Math.floor(shelfLifeDays * 0.5))
  if (shelfLifeDays <= 30) return Math.max(3, Math.floor(shelfLifeDays * 0.3))
  if (shelfLifeDays <= 90) return Math.max(7, Math.floor(shelfLifeDays * 0.2))
  return Math.max(14, Math.floor(shelfLifeDays * 0.15))
}

// 获取预警数据
const fetchAlerts = async () => {
  loading.value = true
  try {
    // 获取所有商品（包含alertThreshold）
    const products = await api.getProducts()
    productList.value = products

    // 获取所有入库记录
    const inboundRecords = await api.getInboundRecords()

    // 按商品汇总库存
    const stockMap = new Map()
    inboundRecords.forEach(record => {
      const current = stockMap.get(record.productId) || { total: 0, batches: [] }
      current.total += record.quantity
      current.batches.push(record)
      stockMap.set(record.productId, current)
    })

    // 获取货位信息
    const locations = await api.getLocations()

    // 处理保质期预警（按商品独立阈值）
    const today = new Date()
    const expiryList = []
    let expiryCount = 0
    let expiredCount = 0
    let healthyCount = 0

    stockMap.forEach((stock, productId) => {
      const product = products.find(p => p.id === productId)
      if (!product) return

      // 获取该商品的预警阈值（优先使用商品设置，否则用默认7）
      const alertThreshold = product.alertThreshold || 7

      stock.batches.forEach(batch => {
        const expiryDate = new Date(batch.expiryDate)
        const remainingDays = Math.ceil((expiryDate - today) / (1000 * 60 * 60 * 24))
        const location = locations.find(l => l.id === batch.locationId)

        if (remainingDays <= alertThreshold && remainingDays >= 0) {
          expiryList.push({
            ...batch,
            productName: product.name,
            category: product.category,
            locationCode: location?.locationCode || '未知',
            remainingDays,
            alertThreshold
          })
          expiryCount++
        } else if (remainingDays < 0) {
          expiredCount++
        } else {
          healthyCount++
        }
      })
    })

    // 按紧急程度排序（剩余天数/阈值 比例越小越紧急）
    expiryList.sort((a, b) => {
      const ratioA = a.remainingDays / a.alertThreshold
      const ratioB = b.remainingDays / b.alertThreshold
      return ratioA - ratioB
    })
    expiryAlerts.value = expiryList

    // 处理库存不足预警
    const lowStockList = []
    let lowStockCount = 0

    stockMap.forEach((stock, productId) => {
      const product = products.find(p => p.id === productId)
      if (product && stock.total <= settings.stockThreshold) {
        let warningLevel = '正常'
        let suggestion = ''

        if (stock.total === 0) {
          warningLevel = '严重缺货'
          suggestion = '库存已清零，请立即补货！'
        } else if (stock.total <= settings.stockThreshold * 0.3) {
          warningLevel = '紧急'
          suggestion = '库存严重不足，建议立即采购'
        } else {
          warningLevel = '警告'
          suggestion = `库存低于预警线，建议补充至 ${settings.stockThreshold} 件以上`
        }

        lowStockList.push({
          productId,
          productName: product.name,
          category: product.category,
          totalStock: stock.total,
          stockThreshold: settings.stockThreshold,
          warningLevel,
          suggestion
        })
        lowStockCount++
      }
    })

    lowStockList.sort((a, b) => a.totalStock - b.totalStock)
    lowStockAlerts.value = lowStockList

    // 更新统计数据
    stats.expiryCount = expiryCount
    stats.lowStockCount = lowStockCount
    stats.expiredCount = expiredCount
    stats.healthyCount = healthyCount

  } catch (error) {
    console.error('获取预警数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取过期预警类型（基于商品阈值）
const getExpiryAlertType = (remainingDays, threshold) => {
  const ratio = remainingDays / threshold
  if (ratio <= 0.3) return 'danger'
  if (ratio <= 0.6) return 'warning'
  return 'info'
}

// 获取剩余天数颜色（基于商品阈值）
const getRemainingDaysColor = (days, threshold) => {
  const ratio = days / threshold
  if (ratio <= 0.3) return '#f56c6c'
  if (ratio <= 0.6) return '#e6a23c'
  return '#409eff'
}

// 获取过期进度百分比
const getExpiryPercent = (days, threshold) => {
  const ratio = days / threshold
  return Math.min(100, Math.max(0, (1 - ratio) * 100))
}

// 获取过期进度颜色
const getExpiryProgressColor = (days, threshold) => {
  const ratio = days / threshold
  if (ratio <= 0.3) return '#f56c6c'
  if (ratio <= 0.6) return '#e6a23c'
  return '#409eff'
}

// 获取紧急程度文本
const getExpiryLevel = (days, threshold) => {
  const ratio = days / threshold
  if (ratio <= 0) return '已过期'
  if (ratio <= 0.3) return '紧急'
  if (ratio <= 0.6) return '警告'
  return '关注'
}

// 获取库存不足标签类型
const getLowStockTagType = (level) => {
  if (level === '严重缺货') return 'danger'
  if (level === '紧急') return 'danger'
  if (level === '警告') return 'warning'
  return 'info'
}

// 打开商品阈值设置弹窗
const openProductThresholdDialog = () => {
  thresholdDialogVisible.value = true
}

// 保存商品阈值
const saveProductThresholds = async () => {
  savingThresholds.value = true
  try {
    // 批量更新商品的预警阈值
    for (const product of productList.value) {
      if (product.alertThreshold !== undefined) {
        await api.updateProduct(product.id, {
          name: product.name,
          category: product.category,
          storageCondition: product.storageCondition,
          shelfLifeDays: product.shelfLifeDays,
          description: product.description,
          alertThreshold: product.alertThreshold
        })
      }
    }
    ElMessage.success('商品阈值设置已保存')
    thresholdDialogVisible.value = false
    // 重新加载预警数据
    await fetchAlerts()
  } catch (error) {
    console.error('保存阈值失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    savingThresholds.value = false
  }
}

// 立即出库
const handleOutbound = (row) => {
  router.push({
    path: '/outbound',
    query: { productId: row.productId }
  })
}

// 立即入库
const handleInbound = (row) => {
  router.push({
    path: '/inbound',
    query: { productId: row.productId }
  })
}

// 保存全局设置
const saveSettings = () => {
  localStorage.setItem('alertSettings', JSON.stringify(settings))
  ElMessage.success('设置已保存')
  fetchAlerts()
}

// 加载设置
const loadSettings = () => {
  const saved = localStorage.getItem('alertSettings')
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      settings.stockThreshold = parsed.stockThreshold || 50
    } catch (e) {
      console.error('加载设置失败:', e)
    }
  }
}

onMounted(() => {
  loadSettings()
  fetchAlerts()
})
</script>

<style scoped>
.alert-container {
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

.stat-icon.expiry { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.lowStock { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.stat-icon.expired { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.healthy { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }

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

.alert-card, .setting-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.setting-tip {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}
</style>