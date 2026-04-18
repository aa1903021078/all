<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="customerName" label="客户" min-width="90" />
        <el-table-column prop="oldStaffName" label="原工作人员" min-width="100" />
        <el-table-column prop="newStaffName" label="新工作人员" min-width="100" />
        <el-table-column prop="reason" label="原因" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="changeStatusTag(row.status)">{{ changeStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link @click="handleAction(row, 1)">批准</el-button>
              <el-button type="warning" link @click="handleAction(row, 2)">拒绝</el-button>
            </template>
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

    <!-- Handle Dialog -->
    <el-dialog v-model="handleVisible" :title="handleTitle" width="550px" destroy-on-close>
      <el-descriptions :column="1" border style="margin-bottom: 20px">
        <el-descriptions-item label="订单号">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentRow.customerName }}</el-descriptions-item>
        <el-descriptions-item label="原工作人员">{{ currentRow.oldStaffName }}</el-descriptions-item>
        <el-descriptions-item label="更换原因">{{ currentRow.reason }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="100px">
        <el-form-item v-if="handleForm.status === 1" label="新工作人员" prop="newStaffId">
          <el-select v-model="handleForm.newStaffId" filterable placeholder="请选择新工作人员" style="width: 100%">
            <el-option v-for="item in staffOptions" :key="item.id"
              :label="`${item.realName}（${staffRoleLabel(item.role)}）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复说明">
          <el-input v-model="handleForm.reply" type="textarea" :rows="3" placeholder="可选填回复说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStaffChangeList, handleStaffChange, deleteStaffChange, getStaffList } from '@/api/index.js'

const changeStatusLabel = (s) => ({ 0: '待审核', 1: '已批准', 2: '已拒绝' })[s] || '未知'
const changeStatusTag = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' })[s] || 'info'
const staffRoleLabel = (r) => ({ 1: '月嫂', 2: '营养师', 3: '护理人员' })[r] || '未知'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10 })

const handleVisible = ref(false)
const handleTitle = ref('')
const submitLoading = ref(false)
const handleFormRef = ref(null)
const currentRow = ref({})
const handleForm = reactive({ id: null, status: null, newStaffId: null, reply: '' })
const staffOptions = ref([])

const handleRules = {
  newStaffId: [{ required: true, message: '请选择新工作人员', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getStaffChangeList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadStaffOptions = async () => {
  try {
    const res = await getStaffList()
    staffOptions.value = res.data || []
  } catch { /* ignore */ }
}

const handleAction = (row, status) => {
  currentRow.value = { ...row }
  handleForm.id = row.id
  handleForm.status = status
  handleForm.newStaffId = null
  handleForm.reply = ''
  handleTitle.value = status === 1 ? '批准更换请求' : '拒绝更换请求'
  if (status === 1) loadStaffOptions()
  handleVisible.value = true
}

const handleSubmit = async () => {
  if (handleForm.status === 1) {
    await handleFormRef.value.validate()
  }
  submitLoading.value = true
  try {
    await handleStaffChange({
      id: handleForm.id,
      status: handleForm.status,
      newStaffId: handleForm.newStaffId,
      reply: handleForm.reply
    })
    ElMessage.success('操作成功')
    handleVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该记录？', '提示', { type: 'warning' }).then(async () => {
    await deleteStaffChange(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

onMounted(() => getList())
</script>

<style scoped>
.app-container { padding: 20px; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
