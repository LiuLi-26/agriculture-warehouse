<template>
  <div class="product-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="水果" value="水果" />
            <el-option label="蔬菜" value="蔬菜" />
            <el-option label="肉类" value="肉类" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleAdd">新增农产品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="hover">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="storageCondition" label="存储条件" width="100">
          <template #default="{ row }">
            <el-tag :type="getStorageTagType(row.storageCondition)">
              {{ row.storageCondition || '常温' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shelfLifeDays" label="保质期(天)" width="100" />
        <el-table-column prop="alertThreshold" label="预警阈值(天)" width="120">
          <template #default="{ row }">
            <el-tag :type="getThresholdTagType(row.alertThreshold, row.shelfLifeDays)">
              {{ row.alertThreshold || 7 }} 天
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="水果" value="水果" />
            <el-option label="蔬菜" value="蔬菜" />
            <el-option label="肉类" value="肉类" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="存储条件" prop="storageCondition">
          <el-select v-model="form.storageCondition" placeholder="请选择存储条件">
            <el-option label="常温" value="常温" />
            <el-option label="冷藏" value="冷藏" />
            <el-option label="冷冻" value="冷冻" />
          </el-select>
        </el-form-item>

        <el-form-item label="保质期(天)" prop="shelfLifeDays">
          <el-input-number
            v-model="form.shelfLifeDays"
            :min="1"
            :max="365"
            :step="1"
            @change="onShelfLifeChange"
          />
          <span class="form-tip">建议阈值: {{ suggestedThreshold }} 天</span>
        </el-form-item>

        <el-form-item label="预警阈值(天)" prop="alertThreshold">
          <el-input-number
            v-model="form.alertThreshold"
            :min="1"
            :max="form.shelfLifeDays || 365"
            :step="1"
          />
          <span class="form-tip">剩余天数 ≤ 此值时触发预警</span>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
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
const dialogTitle = ref('')
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

const searchForm = reactive({
  name: '',
  category: ''
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const form = reactive({
  name: '',
  category: '',
  storageCondition: '常温',
  shelfLifeDays: 30,
  alertThreshold: 7,
  description: ''
})

// 根据保质期计算建议阈值
const suggestedThreshold = computed(() => {
  const days = form.shelfLifeDays
  if (!days) return 7
  if (days <= 7) return Math.max(1, Math.floor(days * 0.5))
  if (days <= 30) return Math.max(3, Math.floor(days * 0.3))
  if (days <= 90) return Math.max(7, Math.floor(days * 0.2))
  return Math.min(Math.floor(days * 0.15), 60)
})

// 保质期变化时自动更新建议阈值
const onShelfLifeChange = () => {
  // 如果用户没有手动修改过预警阈值，则自动设置为建议值
  if (!isEdit.value && form.alertThreshold === 7) {
    form.alertThreshold = suggestedThreshold.value
  }
}

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  shelfLifeDays: [{ required: true, message: '请输入保质期', trigger: 'blur' }],
  alertThreshold: [
    { required: true, message: '请输入预警阈值', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value > form.shelfLifeDays) {
          callback(new Error('预警阈值不能大于保质期'))
        } else if (value < 1) {
          callback(new Error('预警阈值至少为1天'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取存储条件标签类型
const getStorageTagType = (condition) => {
  if (condition === '冷藏') return 'info'
  if (condition === '冷冻') return 'danger'
  return 'success'
}

// 获取阈值标签类型
const getThresholdTagType = (threshold, shelfLife) => {
  if (!threshold) return 'info'
  const ratio = threshold / shelfLife
  if (ratio <= 0.2) return 'danger'
  if (ratio <= 0.4) return 'warning'
  return 'success'
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    let res
    if (searchForm.name) {
      res = await api.searchProducts(searchForm.name)
    } else {
      res = await api.getProducts()
    }

    // 分类筛选
    if (searchForm.category) {
      res = res.filter(item => item.category === searchForm.category)
    }

    tableData.value = res
    page.total = res.length
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  page.current = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  fetchData()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增农产品'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑农产品'
  Object.assign(form, {
    name: row.name,
    category: row.category,
    storageCondition: row.storageCondition || '常温',
    shelfLifeDays: row.shelfLifeDays,
    alertThreshold: row.alertThreshold || 7,
    description: row.description || ''
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该农产品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.deleteProduct(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await api.updateProduct(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await api.createProduct(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
    // 处理重复商品名错误
    if (error.message && error.message.includes('Duplicate entry')) {
      ElMessage.error('商品名称已存在，请使用其他名称')
    } else {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.name = ''
  form.category = ''
  form.storageCondition = '常温'
  form.shelfLifeDays = 30
  form.alertThreshold = 7
  form.description = ''
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.product-container {
  padding: 0;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.table-card {
  border-radius: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-left: 12px;
}
</style>