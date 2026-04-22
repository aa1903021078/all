<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">我的评价</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">发表评价</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="score" label="评分" width="200" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.score" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList" @current-change="getList" />
      </div>
    </el-card>

    <!-- Add Evaluation Dialog -->
    <el-dialog v-model="dialogVisible" title="发表评价" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="选择订单" prop="orderId">
          <el-select v-model="formData.orderId" placeholder="请选择已完成的订单" style="width: 100%" @change="handleOrderSelect">
            <el-option v-for="o in completedOrders" :key="o.id" :label="o.orderNo + ' - ' + (o.packageName || '')" :value="o.id" />
          </el-select>
          <div v-if="completedOrders.length === 0 && !ordersLoading" class="no-orders-tip">
            暂无可评价的订单（仅已完成且未评价的订单可评价）
          </div>
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-rate v-model="formData.score" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-score show-text
            :texts="['很差', '较差', '一般', '满意', '非常满意']" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="4" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { getEvaluationList, addEvaluation, getMyOrders } from '@/api/index.js'

const store = useStore()
const user = computed(() => store.getters.user)

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10 })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({ orderId: null, score: 5, content: '' })
const formRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  score: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

const completedOrders = ref([])
const ordersLoading = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const res = await getEvaluationList({ ...queryParams, customerId: user.value.id })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadCompletedOrders = async () => {
  ordersLoading.value = true
  try {
    const res = await getMyOrders(user.value.id)
    const orders = res.data.records || res.data || []
    const allOrders = Array.isArray(orders) ? orders : []
    const evaluatedOrderIds = new Set(tableData.value.map(e => e.orderId))
    completedOrders.value = allOrders.filter(o => o.status === 3 && !evaluatedOrderIds.has(o.id))
  } finally {
    ordersLoading.value = false
  }
}

const handleOrderSelect = () => {}

const handleAdd = async () => {
  formData.orderId = null
  formData.score = 5
  formData.content = ''
  dialogVisible.value = true
  await loadCompletedOrders()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await addEvaluation({
      orderId: formData.orderId,
      customerId: user.value.id,
      score: formData.score,
      content: formData.content
    })
    ElMessage.success('评价提交成功')
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
.no-orders-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
