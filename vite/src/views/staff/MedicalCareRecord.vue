<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-date-picker v-model="queryParams.recordDate" type="date" value-format="YYYY-MM-DD"
            placeholder="选择日期" clearable style="width: 180px" />
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增记录</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="customerName" label="客户姓名" min-width="100" />
        <el-table-column prop="recordDate" label="记录日期" width="120" align="center" />
        <el-table-column prop="content" label="护理内容" min-width="280" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="关联订单" prop="orderId">
          <el-select v-model="form.orderId" placeholder="请选择订单/客户" filterable style="width: 100%"
            @change="handleOrderChange">
            <el-option v-for="item in orderList" :key="item.id"
              :label="`${item.customerName} - ${item.packageName}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD"
            placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="护理内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8"
            placeholder="请详细记录医疗护理内容，包括：&#10;1. 检查项目及结果&#10;2. 护理操作&#10;3. 用药情况&#10;4. 注意事项" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="医疗护理记录详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="客户姓名">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="记录日期">{{ detailData.recordDate }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section">
        <h4>护理内容</h4>
        <div class="detail-content">{{ detailData.content || '暂无' }}</div>
      </div>
      <div v-if="detailData.remark" class="detail-section">
        <h4>备注</h4>
        <div class="detail-content">{{ detailData.remark }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCareMedicalList, addCareMedical, updateCareMedical, deleteCareMedical, getOrderList } from '@/api/index.js'

const store = useStore()
const currentUser = computed(() => store.getters.user)

// Query
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10, recordDate: '', staffId: '' })

const getList = async () => {
  loading.value = true
  try {
    queryParams.staffId = currentUser.value?.id
    const res = await getCareMedicalList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.current = 1; getList() }
const handleReset = () => { queryParams.recordDate = ''; queryParams.current = 1; getList() }

// Orders
const orderList = ref([])
const loadOrders = async () => {
  try {
    const res = await getOrderList({ current: 1, size: 200, staffId: currentUser.value?.id })
    orderList.value = res.data.records || []
  } catch { /* ignore */ }
}

// Form
const dialogVisible = ref(false)
const dialogTitle = ref('新增医疗护理记录')
const formRef = ref(null)
const submitLoading = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  orderId: null,
  customerId: null,
  customerName: '',
  staffId: null,
  recordDate: '',
  content: '',
  remark: ''
})

const rules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  content: [{ required: true, message: '请输入护理内容', trigger: 'blur' }]
}

const handleOrderChange = (val) => {
  const order = orderList.value.find(o => o.id === val)
  if (order) {
    form.customerName = order.customerName
    form.customerId = order.customerId
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, orderId: null, customerId: null, customerName: '', staffId: null, recordDate: '', content: '', remark: '' })
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogTitle.value = '新增医疗护理记录'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑医疗护理记录'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch { return }
  form.staffId = currentUser.value?.id
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCareMedical(form)
      ElMessage.success('修改成功')
    } else {
      await addCareMedical(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该护理记录？', '提示', { type: 'warning' }).then(async () => {
    await deleteCareMedical(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// Detail
const detailVisible = ref(false)
const detailData = ref({})

const handleView = (row) => {
  detailData.value = { ...row }
  detailVisible.value = true
}

onMounted(() => {
  getList()
  loadOrders()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
.detail-section { margin-top: 20px; }
.detail-section h4 { margin-bottom: 8px; color: #303133; font-size: 15px; border-left: 3px solid #409eff; padding-left: 10px; }
.detail-content { background: #f5f7fa; padding: 12px 16px; border-radius: 4px; white-space: pre-wrap; line-height: 1.8; color: #606266; font-size: 14px; }
</style>
