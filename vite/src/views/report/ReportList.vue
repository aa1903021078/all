<template>
  <div>
    <el-form inline style="margin-bottom:16px">
      <el-form-item>
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="待处理" :value="0" />
          <el-option label="已处理" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="reporterId" label="举报人ID" width="100" />
      <el-table-column label="目标类型" width="100">
        <template #default="{ row }">{{ ['用户','图书','订单'][row.targetType] }}</template>
      </el-table-column>
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column prop="reason" label="举报原因" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'">
            {{ ['待处理','已处理','已驳回'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handle(row, 1)">处理</el-button>
            <el-button size="small" type="info" @click="handle(row, 2)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:16px" :current-page="query.page" :page-size="query.size"
      :total="total" @current-change="handlePageChange" layout="total, prev, pager, next" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportList, handleReport } from '../../api'

const query = reactive({ page: 1, size: 10, status: null })
const tableData = ref([])
const total = ref(0)

const loadData = async () => {
  const res = await getReportList(query)
  if (res.code === 200) {
    tableData.value = res.data.records
    total.value = res.data.total
  }
}

const handlePageChange = (page) => { query.page = page; loadData() }

const handle = async (row, status) => {
  const { value } = await ElMessageBox.prompt('请输入处理结果', '处理举报')
  await handleReport(row.id, { status, handleResult: value })
  ElMessage.success('处理成功')
  loadData()
}

onMounted(loadData)
</script>
