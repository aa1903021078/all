<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column prop="content" label="投诉内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">{{ row.status === 1 ? '已处理' : '待处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复内容" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="replyTime" label="回复时间" width="170" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link @click="handleReply(row)">回复</el-button>
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

    <!-- Reply Dialog -->
    <el-dialog v-model="replyVisible" title="回复投诉" width="550px" destroy-on-close>
      <el-descriptions :column="1" border style="margin-bottom: 20px">
        <el-descriptions-item label="订单号">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentRow.customerName }}</el-descriptions-item>
        <el-descriptions-item label="投诉内容">{{ currentRow.content }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="80px">
        <el-form-item label="回复" prop="reply">
          <el-input v-model="replyForm.reply" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getComplaintList, replyComplaint, deleteComplaint } from '@/api/index.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10 })

const replyVisible = ref(false)
const submitLoading = ref(false)
const replyFormRef = ref(null)
const currentRow = ref({})
const replyForm = reactive({ id: null, reply: '' })
const replyRules = { reply: [{ required: true, message: '请输入回复内容', trigger: 'blur' }] }

const getList = async () => {
  loading.value = true
  try {
    const res = await getComplaintList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleReply = (row) => {
  currentRow.value = { ...row }
  replyForm.id = row.id
  replyForm.reply = ''
  replyVisible.value = true
}

const handleSubmitReply = async () => {
  await replyFormRef.value.validate()
  submitLoading.value = true
  try {
    await replyComplaint({ id: replyForm.id, reply: replyForm.reply })
    ElMessage.success('回复成功')
    replyVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该投诉记录？', '提示', { type: 'warning' }).then(async () => {
    await deleteComplaint(row.id)
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
