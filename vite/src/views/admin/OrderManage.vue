<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable style="width: 200px" @keyup.enter="handleQuery" />
          <el-select v-model="queryParams.status" placeholder="订单状态" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="customerName" label="客户姓名" min-width="100" />
        <el-table-column prop="packageName" label="套餐名称" min-width="140" />
        <el-table-column prop="totalPrice" label="总价" width="120" align="right">
          <template #default="{ row }">¥{{ Number(row.totalPrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedDate" label="预期日期" min-width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link @click="handleStatus(row, 1)">审核通过</el-button>
            <el-button v-if="row.status === 1" type="primary" link @click="handleStatus(row, 2)">开始服务</el-button>
            <el-button v-if="row.status === 2" type="info" link @click="handleStatus(row, 3)">完成服务</el-button>
            <el-button v-if="row.status < 3" type="danger" link @click="handleStatus(row, 4)">取消</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, updateOrderStatus, deleteOrder } from '@/api/index.js'

const statusOptions = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已确认' },
  { value: 2, label: '服务中' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已取消' }
]

const statusLabel = (s) => statusOptions.find(o => o.value === s)?.label || '未知'
const statusTag = (s) => ({ 0: 'warning', 1: '', 2: 'success', 3: 'info', 4: 'danger' })[s] || 'info'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10, orderNo: '', status: '' })

const getList = async () => {
  loading.value = true
  try {
    const res = await getOrderList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.current = 1; getList() }
const handleReset = () => { queryParams.orderNo = ''; queryParams.status = ''; queryParams.current = 1; getList() }

const statusActionLabel = { 1: '审核通过', 2: '开始服务', 3: '完成服务', 4: '取消订单' }

const handleStatus = (row, status) => {
  ElMessageBox.confirm(`确认${statusActionLabel[status]}？`, '提示', { type: 'warning' }).then(async () => {
    await updateOrderStatus({ id: row.id, status })
    ElMessage.success('操作成功')
    getList()
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该订单？', '提示', { type: 'warning' }).then(async () => {
    await deleteOrder(row.id)
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
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
