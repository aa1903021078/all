<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="queryParams.name" placeholder="套餐名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增套餐</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="套餐名称" min-width="150" />
        <el-table-column prop="price" label="价格" width="120" align="right">
          <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="duration" label="服务天数" width="100" align="center">
          <template #default="{ row }">{{ row.duration }}天</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList" @current-change="getList" />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="服务天数" prop="duration">
          <el-input-number v-model="formData.duration" :min="1" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入套餐描述" />
        </el-form-item>
        <el-form-item label="包含服务">
          <el-input v-model="formData.services" type="textarea" :rows="3" placeholder="请输入包含的服务项目描述" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload class="image-uploader" :http-request="handleUpload" :show-file-list="false" accept="image/*">
            <el-image v-if="formData.image" :src="formData.image" style="width: 148px; height: 148px" fit="cover" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="套餐详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="套餐名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ Number(detailData.price || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="服务天数">{{ detailData.duration }}天</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'info'" size="small">{{ detailData.status === 1 ? '上架' : '下架' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="包含服务" :span="2">{{ detailData.services || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detailData.image" style="margin-top: 16px; text-align: center;">
        <el-image :src="detailData.image" style="max-width: 300px; max-height: 200px" fit="contain" :preview-src-list="[detailData.image]" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPackageList, addPackage, updatePackage, deletePackage, uploadFile } from '@/api/index.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10, name: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({ id: null, name: '', price: 0, duration: 28, description: '', services: '', image: '', status: 1 })
const detailVisible = ref(false)
const detailData = ref({})

const formRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入服务天数', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getPackageList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.current = 1; getList() }
const handleReset = () => { queryParams.name = ''; queryParams.current = 1; getList() }

const resetForm = () => {
  Object.assign(formData, { id: null, name: '', price: 0, duration: 28, description: '', services: '', image: '', status: 1 })
}

const handleAdd = () => { resetForm(); dialogTitle.value = '新增套餐'; dialogVisible.value = true }
const handleEdit = (row) => { resetForm(); Object.assign(formData, { ...row }); dialogTitle.value = '编辑套餐'; dialogVisible.value = true }
const handleDetail = (row) => { detailData.value = { ...row }; detailVisible.value = true }

const handleUpload = async (options) => {
  const fd = new FormData()
  fd.append('file', options.file)
  try {
    const res = await uploadFile(fd)
    formData.image = res.data
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (formData.id) {
      await updatePackage(formData)
      ElMessage.success('更新成功')
    } else {
      await addPackage(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该套餐？', '提示', { type: 'warning' }).then(async () => {
    await deletePackage(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

onMounted(() => getList())
</script>

<style scoped>
.app-container { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.toolbar-right { display: flex; align-items: center; gap: 10px; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
.image-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer;
  overflow: hidden; transition: var(--el-transition-duration-fast);
  width: 148px; height: 148px; display: flex; align-items: center; justify-content: center;
}
.image-uploader :deep(.el-upload:hover) { border-color: var(--el-color-primary); }
.uploader-icon { font-size: 28px; color: #8c939d; }
</style>
