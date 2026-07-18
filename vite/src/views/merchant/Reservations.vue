<template>
  <div>
    <div class="flex items-center gap-12 mb-12">
      <span class="bold text-lg">预约管理</span>
      <el-radio-group v-model="filterStatus" @change="load">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待确认</el-radio-button>
        <el-radio-button :value="1">已确认</el-radio-button>
        <el-radio-button :value="3">已完成</el-radio-button>
      </el-radio-group>
      <el-button class="ml-auto" :disabled="!list.length" @click="exportCsv">
        <el-icon><Download /></el-icon>&nbsp;导出历史记录(CSV)
      </el-button>
    </div>

    <div class="card" style="padding: 16px" v-loading="loading">
      <el-table :data="list" style="width: 100%">
        <el-table-column prop="shopName" label="店铺" min-width="140" />
        <el-table-column label="预约时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.reserveTime) }}</template>
        </el-table-column>
        <el-table-column prop="peopleCount" label="人数" width="70" />
        <el-table-column label="联系人" min-width="160">
          <template #default="{ row }">
            {{ row.contactName }} <span class="muted">{{ row.contactPhone }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="handle(row, 1)">确认</el-button>
              <el-button size="small" type="danger" plain @click="handle(row, 2)">拒绝</el-button>
            </template>
            <el-button v-else-if="row.status === 1" size="small" type="primary" @click="handle(row, 3)">标记完成</el-button>
            <span v-else class="muted text-sm">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="暂无预约" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { reservationApi } from '@/api'

const list = ref([])
const loading = ref(false)
const filterStatus = ref(null)

const statusTextMap = { 0: '待确认', 1: '已确认', 2: '已拒绝', 3: '已完成', 4: '已取消' }
const statusTagMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'primary', 4: 'info' }
function statusText(s) {
  return statusTextMap[s] || '-'
}
function statusTag(s) {
  return statusTagMap[s] || 'info'
}
function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    const res = await reservationApi.merchant(filterStatus.value)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handle(row, status) {
  await reservationApi.handle(row.id, status)
  row.status = status
  ElMessage.success('操作成功')
  if (filterStatus.value != null) load()
}

function exportCsv() {
  if (!list.value.length) return ElMessage.warning('暂无数据可导出')
  const headers = ['店铺', '预约时间', '人数', '联系人', '联系电话', '备注', '状态']
  const rows = list.value.map((r) => [
    r.shopName || '',
    formatTime(r.reserveTime),
    r.peopleCount == null ? '' : r.peopleCount,
    r.contactName || '',
    r.contactPhone || '',
    String(r.remark || '').replace(/[\r\n]+/g, ' '),
    statusText(r.status),
  ])
  const csv = [headers].concat(rows)
    .map((row) => row.map((cell) => '"' + String(cell).replace(/"/g, '""') + '"').join(','))
    .join('\n')
  // 加 BOM 防止 Excel 打开中文乱码
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '预约记录_' + new Date().toISOString().slice(0, 10) + '.csv'
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出 ' + list.value.length + ' 条记录')
}

onMounted(load)
</script>
