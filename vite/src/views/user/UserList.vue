<template>
  <div>
    <el-form inline style="margin-bottom:16px">
      <el-form-item>
        <el-input v-model="query.keyword" placeholder="搜索昵称/姓名/学号" clearable @clear="loadData" />
      </el-form-item>
      <el-form-item>
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="未认证" :value="0" />
          <el-option label="已认证" :value="1" />
          <el-option label="已封禁" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="studentId" label="学号" width="120" />
      <el-table-column prop="college" label="学院" width="140" />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column prop="creditScore" label="信用分" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'">
            {{ row.status === 0 ? '未认证' : row.status === 1 ? '已认证' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleVerify(row)">认证</el-button>
          <el-button v-if="row.status !== 2" size="small" type="danger" @click="handleBan(row)">封禁</el-button>
          <el-button v-if="row.status === 2" size="small" type="warning" @click="handleUnban(row)">解封</el-button>
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
import { getUserList, verifyUser, banUser, unbanUser } from '../../api'

const query = reactive({ page: 1, size: 10, keyword: '', status: null })
const tableData = ref([])
const total = ref(0)

const loadData = async () => {
  const res = await getUserList(query)
  if (res.code === 200) {
    tableData.value = res.data.records
    total.value = res.data.total
  }
}

const handlePageChange = (page) => { query.page = page; loadData() }

const handleVerify = async (row) => {
  await ElMessageBox.confirm('确认通过该用户的校园认证？')
  await verifyUser(row.id)
  ElMessage.success('已认证')
  loadData()
}

const handleBan = async (row) => {
  await ElMessageBox.confirm('确认封禁该用户？')
  await banUser(row.id)
  ElMessage.success('已封禁')
  loadData()
}

const handleUnban = async (row) => {
  await ElMessageBox.confirm('确认解封该用户？')
  await unbanUser(row.id)
  ElMessage.success('已解封')
  loadData()
}

onMounted(loadData)
</script>
