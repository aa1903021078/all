<template>
  <el-card class="card-shadow !border-0">
    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="待审核（接收）" name="incoming"></el-tab-pane>
      <el-tab-pane label="我发起的" name="outgoing"></el-tab-pane>
    </el-tabs>

    <el-table :data="list" stripe>
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.type === 'UP' ? 'danger' : 'success'">{{ row.type === 'UP' ? '上转' : '下转' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="patientName" label="患者" width="100" />
      <el-table-column label="转出医院" min-width="150">
        <template #default="{ row }">{{ row.fromHospitalName }} · {{ row.fromDeptName }}</template>
      </el-table-column>
      <el-table-column label="目标" min-width="150">
        <template #default="{ row }">{{ row.toHospitalName }} · {{ row.toDeptName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="show(row)">详情</el-button>
          <template v-if="tab === 'incoming' && row.status === 'REVIEWING'">
            <el-button link type="success" @click="approve(row)">通过</el-button>
            <el-button link type="danger" @click="reject(row)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="转诊单详情" width="600px">
      <div v-if="cur" class="space-y-2 text-sm">
        <p><strong>类型：</strong>{{ cur.type === 'UP' ? '上转' : '下转' }} | <strong>状态：</strong>{{ statusLabel(cur.status) }}</p>
        <p><strong>患者：</strong>{{ cur.patientName }}</p>
        <p><strong>转出：</strong>{{ cur.fromHospitalName }} · {{ cur.fromDeptName }} · {{ cur.fromDoctorName }}</p>
        <p><strong>目标：</strong>{{ cur.toHospitalName }} · {{ cur.toDeptName || '-' }}</p>
        <p><strong>诊疗摘要：</strong></p>
        <div class="bg-slate-50 p-2 rounded">{{ cur.summary || '-' }}</div>
        <p v-if="cur.rehabPlan"><strong>康复方案：</strong></p>
        <div v-if="cur.rehabPlan" class="bg-slate-50 p-2 rounded">{{ cur.rehabPlan }}</div>
        <div v-if="cur.imageUrl">
          <strong>附件：</strong>
          <el-image :src="cur.imageUrl" :preview-src-list="[cur.imageUrl]" style="width:200px" />
        </div>
        <p v-if="cur.rejectReason" class="text-red-500"><strong>驳回原因：</strong>{{ cur.rejectReason }}</p>
      </div>
    </el-dialog>

    <el-dialog v-model="approveVisible" title="审核通过 - 锁定目标号源" width="600px">
      <p class="text-sm text-slate-500 mb-3">请为该患者选择目标排班，通过后系统将自动扣减号源并为患者生成预约。</p>
      <el-table :data="targetSchedules" stripe @row-click="(r) => targetScheduleId = r.id" highlight-current-row
        :row-class-name="({row}) => row.id === targetScheduleId ? 'bg-sky-50' : ''">
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时段" width="80">
          <template #default="{ row }">{{ row.timeSlot === 'AM' ? '上午' : '下午' }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" label="医生" width="120" />
        <el-table-column prop="deptName" label="科室" />
        <el-table-column label="余号" width="80">
          <template #default="{ row }">{{ row.remainingQuota }}/{{ row.totalQuota }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!targetScheduleId" @click="confirmApprove">确认通过</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { doctorApi, catalogApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const tab = ref('incoming')
const list = ref([])
const detailVisible = ref(false)
const cur = ref(null)
const approveVisible = ref(false)
const targetSchedules = ref([])
const targetScheduleId = ref(null)
const approving = ref(null)

const labels = { DRAFT: '草稿', REVIEWING: '审核中', ACCEPTED: '已接收', VISITING: '就诊中', COMPLETED: '已完成', REJECTED: '已拒绝' }
const types  = { REVIEWING: 'warning', ACCEPTED: 'success', VISITING: 'primary', COMPLETED: '', REJECTED: 'danger', DRAFT: 'info' }
const statusLabel = s => labels[s] || s
const statusType = s => types[s] || ''

async function load() {
  list.value = tab.value === 'incoming'
    ? (await doctorApi.incoming()).data
    : (await doctorApi.outgoing()).data
}
function show(row) { cur.value = row; detailVisible.value = true }

async function approve(row) {
  approving.value = row
  targetScheduleId.value = null
  targetSchedules.value = (await catalogApi.schedules({
    hospitalId: row.toHospitalId, deptId: row.toDeptId
  })).data
  approveVisible.value = true
}
async function confirmApprove() {
  await doctorApi.approveReferral(approving.value.id, targetScheduleId.value)
  ElMessage.success('审核通过，已为患者锁定号源')
  approveVisible.value = false
  load()
}
async function reject(row) {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回转诊', { type: 'warning' })
  await doctorApi.rejectReferral(row.id, value)
  ElMessage.success('已驳回'); load()
}

onMounted(load)
</script>
