<template>
  <el-card class="card-shadow !border-0" header="我的预约">
    <el-table :data="apps" stripe>
      <el-table-column prop="hospitalName" label="医院" min-width="180" />
      <el-table-column prop="deptName" label="科室" width="120" />
      <el-table-column prop="doctorName" label="医生" width="120" />
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ row.workDate }} {{ row.timeSlot === 'AM' ? '上午' : '下午' }}</template>
      </el-table-column>
      <el-table-column prop="source" label="来源" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.source === 'REFERRAL' ? 'warning' : 'info'" effect="plain">
            {{ row.source === 'REFERRAL' ? '转诊' : '自助' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'BOOKED'" link type="danger" @click="cancel(row)">取消</el-button>
          <el-button v-if="row.status === 'VISITED'" link type="primary" @click="viewRecord(row)">查看病历</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- record dialog with print -->
    <el-dialog v-model="recordVisible" title="病情单据" width="720px">
      <PrintSheet v-if="record" :data="record" />
      <template #footer>
        <el-button @click="recordVisible = false">关闭</el-button>
        <el-button type="primary" @click="printSheet" :icon="Printer">打印病情单据</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { patientApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Printer } from '@element-plus/icons-vue'
import PrintSheet from '@/components/PrintSheet.vue'

const apps = ref([])
const recordVisible = ref(false)
const record = ref(null)

const labelMap = { BOOKED: '待就诊', VISITED: '已就诊', CANCELLED: '已取消' }
const typeMap  = { BOOKED: 'primary', VISITED: 'success', CANCELLED: 'info' }
const statusLabel = s => labelMap[s] || s
const statusType = s => typeMap[s] || ''

async function load() { apps.value = (await patientApi.myAppointments()).data }
async function cancel(row) {
  await ElMessageBox.confirm('确认取消该预约？号源会自动回补。', '提示', { type: 'warning' })
  await patientApi.cancel(row.id)
  ElMessage.success('已取消')
  load()
}
async function viewRecord(row) {
  record.value = (await patientApi.recordByAppointment(row.id)).data
  recordVisible.value = true
}
function printSheet() {
  nextTick(() => window.print())
}
onMounted(load)
</script>
