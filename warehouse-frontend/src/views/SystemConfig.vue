<template>
  <div class="config-container">
    <el-card class="config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>⚙️ 系统设置</span>
          <div>
            <el-button type="primary" @click="saveAllConfigs" :loading="saving">
              保存所有设置
            </el-button>
            <el-button @click="fetchConfigs">刷新</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 预警设置 -->
        <el-tab-pane label="预警设置" name="warning">
          <el-form :model="configForm" label-width="180px">
            <el-form-item label="库存预警阈值（件）">
              <el-input-number
                v-model="configForm.stock_warning_threshold"
                :min="10"
                :max="500"
                @change="markChanged('stock_warning_threshold')"
              />
              <span class="form-tip">当库存低于此值时触发预警</span>
            </el-form-item>

            <el-form-item label="保质期预警天数">
              <el-input-number
                v-model="configForm.expiry_warning_days"
                :min="1"
                :max="30"
                @change="markChanged('expiry_warning_days')"
              />
              <span class="form-tip">剩余天数 ≤ 此值时触发保质期预警</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 系统设置 -->
        <el-tab-pane label="系统设置" name="system">
          <el-form :model="configForm" label-width="180px">
            <el-form-item label="系统名称">
              <el-input
                v-model="configForm.system_name"
                placeholder="请输入系统名称"
                @input="markChanged('system_name')"
              />
            </el-form-item>

            <el-form-item label="公司名称">
              <el-input
                v-model="configForm.company_name"
                placeholder="请输入公司名称"
                @input="markChanged('company_name')"
              />
            </el-form-item>

            <el-form-item label="默认每页显示数量">
              <el-select
                v-model="configForm.page_size"
                @change="markChanged('page_size')"
              >
                <el-option label="10条" :value="10" />
                <el-option label="20条" :value="20" />
                <el-option label="50条" :value="50" />
                <el-option label="100条" :value="100" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 业务设置 -->
        <el-tab-pane label="业务设置" name="business">
          <el-form :model="configForm" label-width="180px">
            <el-form-item label="入库默认单位">
              <el-select
                v-model="configForm.inbound_default_unit"
                @change="markChanged('inbound_default_unit')"
              >
                <el-option label="公斤(kg)" value="kg" />
                <el-option label="箱" value="箱" />
                <el-option label="件" value="件" />
                <el-option label="吨(t)" value="t" />
              </el-select>
            </el-form-item>

            <el-form-item label="是否启用先进先出">
              <el-switch
                v-model="configForm.outbound_fifo_enabled"
                active-value="true"
                inactive-value="false"
                @change="markChanged('outbound_fifo_enabled')"
              />
              <span class="form-tip">关闭后出库时手动选择批次</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 环境监测设置 -->
        <el-tab-pane label="环境监测" name="environment">
          <el-form :model="configForm" label-width="180px">
            <el-form-item label="监测数据采集间隔（秒）">
              <el-input-number
                v-model="configForm.environment_monitor_interval"
                :min="10"
                :max="300"
                :step="10"
                @change="markChanged('environment_monitor_interval')"
              />
              <span class="form-tip">模拟传感器数据采集频率</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 未保存提示 -->
      <div v-if="hasChanges" class="unsaved-tip">
        <el-alert
          title="有未保存的修改"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            您有未保存的修改，请点击"保存所有设置"按钮保存。
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('warning')
const hasChanges = ref(false)
const changedKeys = ref(new Set())

const configForm = reactive({
  // 预警设置
  stock_warning_threshold: 50,
  expiry_warning_days: 7,
  // 系统设置
  system_name: '农产品仓储管理系统',
  company_name: 'XX农业科技有限公司',
  page_size: 20,
  // 业务设置
  inbound_default_unit: 'kg',
  outbound_fifo_enabled: 'true',
  // 环境监测
  environment_monitor_interval: 30
})

// 标记已修改
const markChanged = (key) => {
  changedKeys.value.add(key)
  hasChanges.value = true
}

// 获取配置
const fetchConfigs = async () => {
  loading.value = true
  try {
    const res = await api.get('/system/config')
    if (res) {
      Object.keys(configForm).forEach(key => {
        if (res[key] !== undefined) {
          configForm[key] = res[key]
        }
      })
    }
    changedKeys.value.clear()
    hasChanges.value = false
  } catch (error) {
    console.error('获取配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存所有配置
const saveAllConfigs = async () => {
  if (changedKeys.value.size === 0) {
    ElMessage.info('没有需要保存的修改')
    return
  }

  saving.value = true
  try {
    const configsToSave = {}
    changedKeys.value.forEach(key => {
      configsToSave[key] = configForm[key]
    })

    await api.put('/system/config/batch', configsToSave)
    ElMessage.success('保存成功')
    changedKeys.value.clear()
    hasChanges.value = false
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 初始化默认配置
const initConfigs = async () => {
  try {
    await api.post('/system/config/init')
    ElMessage.success('初始化成功')
    await fetchConfigs()
  } catch (error) {
    console.error('初始化失败:', error)
  }
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.config-container {
  padding: 0;
}

.config-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-left: 12px;
}

.unsaved-tip {
  margin-top: 20px;
}
</style>