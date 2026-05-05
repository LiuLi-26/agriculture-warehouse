<template>
  <div class="backup-container">
    <!-- 操作栏 -->
    <el-card class="action-card" shadow="hover">
      <div class="action-bar">
        <el-button type="primary" @click="handleBackup" :loading="backupLoading">
          <el-icon><Document /></el-icon>
          立即备份
        </el-button>
        <el-button @click="fetchBackupFiles">
          <el-icon><Refresh /></el-icon>
          刷新列表
        </el-button>
      </div>
    </el-card>

    <!-- 备份文件列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📁 备份文件列表</span>
          <el-tag type="info">备份目录: ./backups/</el-tag>
        </div>
      </template>

      <el-table :data="backupFiles" v-loading="loading" stripe>
        <el-table-column prop="fileName" label="文件名" min-width="300" />
        <el-table-column prop="fileSizeStr" label="文件大小" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDownload(row)">
              下载
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="backupFiles.length === 0 && !loading" description="暂无备份文件" />
    </el-card>

    <!-- 备份说明 -->
    <el-card class="help-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>💡 备份说明</span>
        </div>
      </template>
      <div class="help-content">
        <p>• 点击"立即备份"将导出当前数据库的全部数据</p>
        <p>• 备份文件保存在服务器的 ./backups/ 目录下</p>
        <p>• 可下载备份文件到本地保存</p>
        <p>• 建议定期备份重要数据</p>
        <p>• 恢复数据时需要使用 MySQL 命令行工具</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const backupLoading = ref(false)
const backupFiles = ref([])

// 获取备份文件列表
const fetchBackupFiles = async () => {
  loading.value = true
  try {
    const res = await api.get('/backup/files')
    backupFiles.value = res || []
  } catch (error) {
    console.error('获取备份文件列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 执行备份
const handleBackup = async () => {
  backupLoading.value = true
  try {
    const res = await api.post('/backup')
    ElMessage.success(res)
    await fetchBackupFiles()
  } catch (error) {
    console.error('备份失败:', error)
    ElMessage.error('备份失败')
  } finally {
    backupLoading.value = false
  }
}

// 下载备份文件
const handleDownload = (row) => {
  window.open(`/api/backup/download/${row.fileName}`, '_blank')
}

// 删除备份文件
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除备份文件 "${row.fileName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.delete(`/backup/${row.fileName}`)
      ElMessage.success('删除成功')
      await fetchBackupFiles()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  fetchBackupFiles()
})
</script>

<style scoped>
.backup-container {
  padding: 0;
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
  margin-bottom: 20px;
  border-radius: 12px;
}

.help-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.help-content {
  color: #666;
  font-size: 14px;
  line-height: 1.8;
}

.help-content p {
  margin: 5px 0;
}
</style>