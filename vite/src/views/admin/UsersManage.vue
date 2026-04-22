<template>
  <div class="yq-card">
    <div class="flex gap-3 mb-4">
      <el-input v-model="keyword" placeholder="搜索用户名/手机号" clearable class="max-w-[260px]" @keyup.enter="reload" />
      <el-button type="primary" @click="reload">搜索</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="userId" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">正常</el-tag>
          <el-tag v-else type="info">已禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.role !== 'ADMIN'" size="small"
                     :type="row.status === 1 ? 'warning' : 'success'"
                     @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button v-if="row.role !== 'ADMIN'" size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="flex justify-center mt-4">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                     layout="prev, pager, next" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUsers, setUserStatus, deleteUser } from '@/api/biz'

const list = ref([]); const total = ref(0); const page = ref(1); const size = 10
const keyword = ref('')
function reload() { page.value = 1; load() }
async function load() {
  const d = await pageUsers({ page: page.value, size, keyword: keyword.value })
  list.value = d.records; total.value = d.total
}
async function toggleStatus(row) {
  await setUserStatus(row.userId, row.status === 1 ? 0 : 1)
  ElMessage.success('已更新')
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '警告', { type: 'warning' })
  await deleteUser(row.userId)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>
