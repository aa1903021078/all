<template>
  <el-card class="card-shadow !border-0" header="转诊跟踪">
    <el-empty v-if="list.length === 0" description="暂无转诊记录" />
    <el-table v-else :data="list" stripe>
      <el-table-column prop="type" label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="row.type === 'UP' ? 'danger' : 'success'" size="small">
            {{ row.type === 'UP' ? '上转' : '下转' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="转出医院" min-width="160">
        <template #default="{ row }">{{ row.fromHospitalName }} · {{ row.fromDeptName }}</template>
      </el-table-column>
      <el-table-column label="目标医院" min-width="160">
        <template #default="{ row }">{{ row.toHospitalName }} · {{ row.toDeptName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ formatTime(row.updatedAt || row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="show(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="转诊单详情" width="600px">
      <div v-if="cur" class="space-y-2 text-sm">
        <div><span class="text-slate-400">类型：</span>{{ cur.type === 'UP' ? '上转（基层 → 上级）' : '下转（上级 → 基层）' }}</div>
        <div><span class="text-slate-400">状态：</span><el-tag size="small" :type="statusType(cur.status)">{{ statusLabel(cur.status) }}</el-tag></div>
        <div><span class="text-slate-400">转出：</span>{{ cur.fromHospitalName }} · {{ cur.fromDeptName }} · {{ cur.fromDoctorName }}</div>
        <div><span class="text-slate-400">目标：</span>{{ cur.toHospitalName }} · {{ cur.toDeptName || '-' }} · {{ cur.toDoctorName || '-' }}</div>
        <div><span class="text-slate-400">诊疗摘要：</span><div class="bg-slate-50 p-2 rounded mt-1">{{ cur.summary || '-' }}</div></div>
        <div v-if="cur.rehabPlan">
          <span class="text-slate-400">康复方案：</span><div class="bg-slate-50 p-2 rounded mt-1">{{ cur.rehabPlan }}</div>
        </div>
        <div v-if="cur.imageUrl">
          <span class="text-slate-400">附件：</span>
          <el-image :src="cur.imageUrl" :preview-src-list="[cur.imageUrl]" style="width: 200px; margin-top: 4px" />
        </div>
        <div v-if="cur.rejectReason" class="text-red-500">驳回原因：{{ cur.rejectReason }}</div>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { patientApi } from '@/api'

const list = ref([])
const visible = ref(false)
const cur = ref(null)

const labels = { DRAFT: '草稿', REVIEWING: '审核中', ACCEPTED: '已接收', VISITING: '就诊中', COMPLETED: '已完成', REJECTED: '已拒绝' }
const types  = { REVIEWING: 'warning', ACCEPTED: 'success', VISITING: 'primary', COMPLETED: '', REJECTED: 'danger', DRAFT: 'info' }
const statusLabel = s => labels[s] || s
const statusType  = s => types[s] || ''
const formatTime = (s) => s ? dayjs(s).format('YYYY-MM-DD HH:mm') : ''

function show(r) { cur.value = r; visible.value = true }

onMounted(async () => { list.value = (await patientApi.referrals()).data })
</script>
