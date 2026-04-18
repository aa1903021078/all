<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">我的订单</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="packageName" label="套餐名称" min-width="140" />
        <el-table-column prop="totalPrice" label="总价" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ Number(row.totalPrice).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedDate" label="预期日期" width="120" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 3" type="warning" link :icon="StarFilled" @click="handleEvaluate(row)">评价</el-button>
            <el-button v-if="row.status === 2 || row.status === 3" type="danger" link :icon="Warning" @click="handleComplaint(row)">投诉</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList" @current-change="getList" />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="套餐名称">{{ detailData.packageName }}</el-descriptions-item>
        <el-descriptions-item label="总价">
          <span class="price-text">¥{{ Number(detailData.totalPrice || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detailData.status)">{{ statusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预期日期">{{ detailData.expectedDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detailData.startDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detailData.endDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="maternalInfo" style="margin-top: 20px">
        <h4 style="margin-bottom: 12px; color: #303133;">产妇信息</h4>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="姓名">{{ maternalInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ maternalInfo.age }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ maternalInfo.phone }}</el-descriptions-item>
          <el-descriptions-item label="分娩方式">{{ maternalInfo.deliveryType }}</el-descriptions-item>
          <el-descriptions-item label="分娩日期">{{ maternalInfo.deliveryDate }}</el-descriptions-item>
          <el-descriptions-item label="健康备注">{{ maternalInfo.healthNote || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-if="infantListInfo.length > 0" style="margin-top: 20px">
        <h4 style="margin-bottom: 12px; color: #303133;">婴儿信息（共{{ infantListInfo.length }}个）</h4>
        <el-table :data="infantListInfo" border size="small">
          <el-table-column prop="name" label="姓名/昵称" min-width="100" />
          <el-table-column label="性别" width="60" align="center">
            <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
          </el-table-column>
          <el-table-column prop="birthDate" label="出生日期" width="120" />
          <el-table-column prop="birthWeight" label="出生体重(kg)" width="110" align="center" />
          <el-table-column prop="healthNote" label="健康备注" min-width="200" />
        </el-table>
      </div>

      <div v-if="staffList.length > 0" style="margin-top: 20px">
        <h4 style="margin-bottom: 12px; color: #303133;">指派工作人员</h4>
        <el-table :data="staffList" border size="small">
          <el-table-column prop="staffName" label="姓名" min-width="100" />
          <el-table-column prop="roleName" label="角色" min-width="100">
            <template #default="{ row }">{{ row.roleName || getRoleName(row.role) }}</template>
          </el-table-column>
          <el-table-column prop="infantName" label="负责婴儿" min-width="100">
            <template #default="{ row }">{{ row.infantName || '通用/产妇' }}</template>
          </el-table-column>
          <el-table-column prop="phone" label="联系电话" min-width="130" />
        </el-table>
      </div>
      <el-empty v-else-if="!staffLoading" description="暂无指派工作人员" :image-size="60" />
    </el-dialog>

    <!-- Evaluate Dialog -->
    <el-dialog v-model="evalDialogVisible" title="评价订单" width="500px" destroy-on-close>
      <el-form ref="evalFormRef" :model="evalForm" :rules="evalRules" label-width="80px">
        <el-form-item label="订单号">
          <el-input :model-value="evalForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-rate v-model="evalForm.score" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-score />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="evalForm.content" type="textarea" :rows="4" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evalDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="evalSubmitLoading" @click="handleSubmitEval">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- Complaint Dialog -->
    <el-dialog v-model="complaintDialogVisible" title="投诉" width="500px" destroy-on-close>
      <el-form ref="complaintFormRef" :model="complaintForm" :rules="complaintRules" label-width="80px">
        <el-form-item label="订单号">
          <el-input :model-value="complaintForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="投诉内容" prop="content">
          <el-input v-model="complaintForm.content" type="textarea" :rows="4" placeholder="请详细描述您的投诉内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="complaintDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="complaintSubmitLoading" @click="handleSubmitComplaint">提交投诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { View, StarFilled, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { getMyOrders, getStaffByOrder, addEvaluation, addComplaint, getMaternalByOrder, getInfantsByOrder } from '@/api/index.js'

const store = useStore()
const user = computed(() => store.getters.user)

const statusOptions = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已确认' },
  { value: 2, label: '服务中' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已取消' }
]
const statusLabel = (s) => statusOptions.find(o => o.value === s)?.label || '未知'
const statusTag = (s) => ({ 0: 'warning', 1: '', 2: 'success', 3: 'info', 4: 'danger' })[s] || 'info'
const getRoleName = (role) => ({ 1: '管理员', 2: '月嫂', 3: '营养师', 4: '护理人员' })[role] || '工作人员'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10 })

// Detail
const detailVisible = ref(false)
const detailData = ref({})
const staffList = ref([])
const staffLoading = ref(false)
const maternalInfo = ref(null)
const infantListInfo = ref([])

// Evaluation
const evalDialogVisible = ref(false)
const evalSubmitLoading = ref(false)
const evalFormRef = ref(null)
const evalForm = reactive({ orderId: null, orderNo: '', score: 5, content: '' })
const evalRules = {
  score: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

// Complaint
const complaintDialogVisible = ref(false)
const complaintSubmitLoading = ref(false)
const complaintFormRef = ref(null)
const complaintForm = reactive({ orderId: null, orderNo: '', content: '' })
const complaintRules = {
  content: [{ required: true, message: '请输入投诉内容', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getMyOrders(user.value.id)
    const records = res.data.records || res.data || []
    const allRecords = Array.isArray(records) ? records : []
    total.value = res.data.total || allRecords.length
    const start = (queryParams.current - 1) * queryParams.size
    tableData.value = allRecords.slice(start, start + queryParams.size)
    if (res.data.total !== undefined) {
      tableData.value = allRecords
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleDetail = async (row) => {
  detailData.value = { ...row }
  staffList.value = []
  maternalInfo.value = null
  infantListInfo.value = []
  detailVisible.value = true
  staffLoading.value = true
  try {
    const [staffRes, maternalRes, infantRes] = await Promise.all([
      getStaffByOrder(row.id),
      getMaternalByOrder(row.id),
      getInfantsByOrder(row.id)
    ])
    staffList.value = staffRes.data || []
    maternalInfo.value = maternalRes.data || null
    infantListInfo.value = infantRes.data || []
  } catch {
    staffList.value = []
  } finally {
    staffLoading.value = false
  }
}

const handleEvaluate = (row) => {
  evalForm.orderId = row.id
  evalForm.orderNo = row.orderNo
  evalForm.score = 5
  evalForm.content = ''
  evalDialogVisible.value = true
}

const handleSubmitEval = async () => {
  await evalFormRef.value.validate()
  evalSubmitLoading.value = true
  try {
    await addEvaluation({
      orderId: evalForm.orderId,
      customerId: user.value.id,
      score: evalForm.score,
      content: evalForm.content
    })
    ElMessage.success('评价提交成功')
    evalDialogVisible.value = false
  } finally {
    evalSubmitLoading.value = false
  }
}

const handleComplaint = (row) => {
  complaintForm.orderId = row.id
  complaintForm.orderNo = row.orderNo
  complaintForm.content = ''
  complaintDialogVisible.value = true
}

const handleSubmitComplaint = async () => {
  await complaintFormRef.value.validate()
  complaintSubmitLoading.value = true
  try {
    await addComplaint({
      orderId: complaintForm.orderId,
      customerId: user.value.id,
      content: complaintForm.content
    })
    ElMessage.success('投诉提交成功')
    complaintDialogVisible.value = false
  } finally {
    complaintSubmitLoading.value = false
  }
}

onMounted(() => getList())
</script>

<style scoped>
.app-container { padding: 20px; }
.card-header { display: flex; align-items: center; }
.header-title { font-size: 18px; font-weight: 600; color: #303133; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
.price-text { font-weight: 600; color: #e6a23c; }
</style>
