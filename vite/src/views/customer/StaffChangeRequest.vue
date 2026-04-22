<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">工作人员更换申请</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">申请更换</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="oldStaffName" label="原工作人员" min-width="120" />
        <el-table-column prop="reason" label="更换原因" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="changeStatusTag(row.status)" size="small">{{ changeStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reply || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList" @current-change="getList" />
      </div>
    </el-card>

    <!-- Add Change Request Dialog -->
    <el-dialog v-model="dialogVisible" title="申请更换工作人员" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="选择订单" prop="orderId">
          <el-select v-model="formData.orderId" placeholder="请选择进行中的订单" style="width: 100%" @change="handleOrderChange">
            <el-option v-for="o in activeOrders" :key="o.id" :label="o.orderNo + ' - ' + (o.packageName || '')" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择人员" prop="oldStaffId">
          <el-select v-model="formData.oldStaffId" placeholder="请选择要更换的工作人员" style="width: 100%" :loading="staffLoading">
            <el-option v-for="s in orderStaffList" :key="s.staffId || s.id" :label="s.staffName + ' (' + (s.roleName || getRoleName(s.role)) + ')'" :value="s.staffId || s.id" />
          </el-select>
          <div v-if="formData.orderId && orderStaffList.length === 0 && !staffLoading" class="no-staff-tip">该订单暂无指派工作人员</div>
        </el-form-item>
        <el-form-item label="更换原因" prop="reason">
          <el-input v-model="formData.reason" type="textarea" :rows="4" placeholder="请详细说明更换原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { getStaffChangeList, addStaffChange, getMyOrders, getStaffByOrder } from '@/api/index.js'

const store = useStore()
const user = computed(() => store.getters.user)

const changeStatusLabel = (s) => ({ 0: '待审核', 1: '已批准', 2: '已拒绝' })[s] || '未知'
const changeStatusTag = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' })[s] || 'info'
const getRoleName = (role) => ({ 1: '管理员', 2: '月嫂', 3: '营养师', 4: '护理人员' })[role] || '工作人员'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10 })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({ orderId: null, oldStaffId: null, reason: '' })
const formRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  oldStaffId: [{ required: true, message: '请选择要更换的工作人员', trigger: 'change' }],
  reason: [{ required: true, message: '请输入更换原因', trigger: 'blur' }]
}

const activeOrders = ref([])
const orderStaffList = ref([])
const staffLoading = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const res = await getStaffChangeList({ ...queryParams, customerId: user.value.id })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadActiveOrders = async () => {
  try {
    const res = await getMyOrders(user.value.id)
    const orders = res.data.records || res.data || []
    const allOrders = Array.isArray(orders) ? orders : []
    activeOrders.value = allOrders.filter(o => o.status === 1 || o.status === 2)
  } catch {
    activeOrders.value = []
  }
}

const handleOrderChange = async (orderId) => {
  formData.oldStaffId = null
  orderStaffList.value = []
  if (!orderId) return
  staffLoading.value = true
  try {
    const res = await getStaffByOrder(orderId)
    orderStaffList.value = res.data || []
  } catch {
    orderStaffList.value = []
  } finally {
    staffLoading.value = false
  }
}

const handleAdd = async () => {
  formData.orderId = null
  formData.oldStaffId = null
  formData.reason = ''
  orderStaffList.value = []
  dialogVisible.value = true
  await loadActiveOrders()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await addStaffChange({
      orderId: formData.orderId,
      customerId: user.value.id,
      oldStaffId: formData.oldStaffId,
      reason: formData.reason
    })
    ElMessage.success('更换申请提交成功')
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
.no-staff-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
