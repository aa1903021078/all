<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">我的投诉</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">提交投诉</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="content" label="投诉内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已处理' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reply || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column prop="replyTime" label="回复时间" min-width="170">
          <template #default="{ row }">{{ row.replyTime || '-' }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList" @current-change="getList" />
      </div>
    </el-card>

    <!-- Add Complaint Dialog -->
    <el-dialog v-model="dialogVisible" title="提交投诉" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="选择订单" prop="orderId">
          <el-select v-model="formData.orderId" placeholder="请选择订单" style="width: 100%">
            <el-option v-for="o in myOrders" :key="o.id" :label="o.orderNo + ' - ' + (o.packageName || '')" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="投诉内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="5" placeholder="请详细描述您的投诉内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交投诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { getComplaintList, addComplaint, getMyOrders } from '@/api/index.js'

const store = useStore()
const user = computed(() => store.getters.user)

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10 })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({ orderId: null, content: '' })
const formRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  content: [{ required: true, message: '请输入投诉内容', trigger: 'blur' }]
}

const myOrders = ref([])

const getList = async () => {
  loading.value = true
  try {
    const res = await getComplaintList({ ...queryParams, customerId: user.value.id })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadMyOrders = async () => {
  try {
    const res = await getMyOrders(user.value.id)
    const orders = res.data.records || res.data || []
    myOrders.value = Array.isArray(orders) ? orders : []
  } catch {
    myOrders.value = []
  }
}

const handleAdd = async () => {
  formData.orderId = null
  formData.content = ''
  dialogVisible.value = true
  await loadMyOrders()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await addComplaint({
      orderId: formData.orderId,
      customerId: user.value.id,
      content: formData.content
    })
    ElMessage.success('投诉提交成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => getList())
</script>

<style scoped>
.app-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 18px; font-weight: 600; color: #303133; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
