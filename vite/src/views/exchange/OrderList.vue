<template>
  <div>
    <el-form inline style="margin-bottom:16px">
      <el-form-item>
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="待确认" :value="0" />
          <el-option label="已接受" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="待交换" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="requesterId" label="申请人ID" width="100" />
      <el-table-column prop="requesterBookId" label="申请书ID" width="100" />
      <el-table-column prop="ownerId" label="持有人ID" width="100" />
      <el-table-column prop="ownerBookId" label="目标书ID" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="exchangeLocation" label="交换地点" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
    </el-table>
    <el-pagination style="margin-top:16px" :current-page="query.page" :page-size="query.size"
      :total="total" @current-change="handlePageChange" layout="total, prev, pager, next" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getExchangeList } from '../../api'

const query = reactive({ page: 1, size: 10, status: null })
const tableData = ref([])
const total = ref(0)

const statusText = (s) => ['待确认','已接受','已拒绝','待交换','已完成','已取消'][s]
const statusType = (s) => ['warning','primary','danger','info','success','info'][s]

const loadData = async () => {
  const res = await getExchangeList(query)
  if (res.code === 200) {
    tableData.value = res.data.records
    total.value = res.data.total
  }
}

const handlePageChange = (page) => { query.page = page; loadData() }

onMounted(loadData)
</script>
