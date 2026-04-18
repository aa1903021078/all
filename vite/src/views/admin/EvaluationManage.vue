<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border stripe @row-click="handleRowClick">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column prop="score" label="评分" width="180" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.score" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link :icon="Delete" @click.stop="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="detailVisible" title="评价详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="detailData.score" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="评价内容">{{ detailData.content || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEvaluationList, deleteEvaluation } from '@/api/index.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10 })
const detailVisible = ref(false)
const detailData = ref({})

const getList = async () => {
  loading.value = true
  try {
    const res = await getEvaluationList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleRowClick = (row) => {
  detailData.value = { ...row }
  detailVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该评价？', '提示', { type: 'warning' }).then(async () => {
    await deleteEvaluation(row.id)
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
