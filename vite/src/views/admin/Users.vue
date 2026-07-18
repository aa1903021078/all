<template>
  <div>
    <div class="card filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索用户名/昵称" style="width: 220px" clearable @keyup.enter="reload" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px" @change="reload">
        <el-option label="正常" :value="0" />
        <el-option label="已封禁" :value="1" />
      </el-select>
      <el-button type="primary" @click="reload"><el-icon><Search /></el-icon>&nbsp;查询</el-button>
    </div>

    <div class="card mt-16" style="padding: 16px" v-loading="loading">
      <el-table :data="list" style="width: 100%">
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-8">
              <el-avatar :size="40" :src="row.avatar" />
              <div>
                <div class="bold">{{ row.nickname }}</div>
                <div class="muted text-sm">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="bio" label="简介" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'success'">{{ row.status === 1 ? '已封禁' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openRoles(row)">分配角色</el-button>
            <el-button
              v-if="row.status !== 1"
              size="small"
              type="danger"
              plain
              @click="changeStatus(row, 1)"
            >封禁</el-button>
            <el-button v-else size="small" type="success" plain @click="changeStatus(row, 0)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-16">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.current"
          @current-change="pageChange"
        />
      </div>
    </div>

    <!-- 角色分配 -->
    <el-dialog v-model="roleVisible" title="分配角色" width="420px">
      <div class="mb-12 muted">用户:<b>{{ currentUser.nickname }}</b></div>
      <el-checkbox-group v-model="selectedRoles">
        <el-checkbox v-for="r in allRoles" :key="r.id" :value="r.id" border class="role-check">
          {{ r.name }} <span class="muted text-sm">({{ r.code }})</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi, systemApi } from '@/api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '', status: null })

const roleVisible = ref(false)
const currentUser = ref({})
const allRoles = ref([])
const selectedRoles = ref([])

async function load() {
  loading.value = true
  try {
    const res = await userApi.adminPage({ ...query })
    list.value = (res.data && res.data.records) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}
function reload() {
  query.current = 1
  load()
}
function pageChange(p) {
  query.current = p
  load()
}

async function changeStatus(row, status) {
  await userApi.changeStatus(row.id, status)
  row.status = status
  ElMessage.success(status === 1 ? '已封禁' : '已解封')
}

async function openRoles(row) {
  currentUser.value = row
  roleVisible.value = true
  if (!allRoles.value.length) {
    allRoles.value = (await systemApi.roles()).data || []
  }
  selectedRoles.value = (await systemApi.userRoles(row.id)).data || []
}

async function saveRoles() {
  await systemApi.assignUserRoles(currentUser.value.id, selectedRoles.value)
  roleVisible.value = false
  ElMessage.success('角色分配成功')
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}
.role-check {
  display: block;
  margin: 0 0 10px;
}
</style>
