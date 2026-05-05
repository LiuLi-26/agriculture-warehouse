<template>
  <div class="supplier-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.name" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="水果类" value="水果类" />
            <el-option label="蔬菜类" value="蔬菜类" />
            <el-option label="肉类" value="肉类" />
            <el-option label="粮食类" value="粮食类" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleAdd">新增供应商</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="hover">
      <el-table :data="filteredData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="供应商名称" min-width="150" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.category || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="updatePage"
        @current-change="updatePage"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" clearable>
            <el-option label="水果类" value="水果类" />
            <el-option label="蔬菜类" value="蔬菜类" />
            <el-option label="肉类" value="肉类" />
            <el-option label="粮食类" value="粮食类" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
  contactPerson: '',
  phone: '',
  email: '',
  address: '',
  category: '',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

// 筛选后的数据
const filteredData = computed(() => {
  let data = [...tableData.value]

  if (searchForm.name) {
    data = data.filter(item => item.name.includes(searchForm.name))
  }

  if (searchForm.category) {
    data = data.filter(item => item.category === searchForm.category)
  }

  page.total = data.length
  const start = (page.current - 1) * page.size
  const end = start + page.size
  return data.slice(start, end)
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/suppliers')
    tableData.value = res || []
  } catch (error) {
    console.error('获取数据失败:', error)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  page.current = 1
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  page.current = 1
}

// 更新分页
const updatePage = () => {}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增供应商'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑供应商'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除供应商 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.delete(`/suppliers/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await api.put(`/suppliers/${editId.value}`, form)
      ElMessage.success('修改成功')
    } else {
      await api.post('/suppliers', form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.name = ''
  form.contactPerson = ''
  form.phone = ''
  form.email = ''
  form.address = ''
  form.category = ''
  form.remark = ''
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.supplier-container {
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
</style>