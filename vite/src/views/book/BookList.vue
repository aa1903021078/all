<template>
  <div>
    <el-form inline style="margin-bottom:16px">
      <el-form-item>
        <el-input v-model="query.keyword" placeholder="搜索书名" clearable @clear="loadData" />
      </el-form-item>
      <el-form-item>
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="待审核" :value="0" />
          <el-option label="上架中" :value="1" />
          <el-option label="已下架" :value="2" />
          <el-option label="已置换" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="书名" width="180" />
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column prop="isbn" label="ISBN" width="140" />
      <el-table-column prop="conditionLevel" label="品相" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'">
            {{ ['待审核','上架','下架','已置换'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleAudit(row, 1)">通过</el-button>
          <el-button v-if="row.status === 0" size="small" type="warning" @click="handleAudit(row, 2)">拒绝</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
import { getBookList, auditBook, deleteBook } from '../../api'

const query = reactive({ page: 1, size: 10, keyword: '', status: null })
const tableData = ref([])
const total = ref(0)

const loadData = async () => {
  const res = await getBookList(query)
  if (res.code === 200) {
    tableData.value = res.data.records
    total.value = res.data.total
  }
}

const handlePageChange = (page) => { query.page = page; loadData() }

const handleAudit = async (row, status) => {
  const action = status === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确认${action}该图书审核？`)
  await auditBook(row.id, { status })
  ElMessage.success(`已${action}`)
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该图书？')
  await deleteBook(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>
