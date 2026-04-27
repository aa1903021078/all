<template>
  <el-card class="card-shadow !border-0" header="历次就诊记录">
    <el-empty v-if="records.length === 0" description="暂无就诊记录" />
    <el-timeline v-else>
      <el-timeline-item
        v-for="r in records"
        :key="r.id"
        :timestamp="formatDate(r.createdAt)"
        placement="top"
        type="primary"
      >
        <el-card shadow="hover">
          <div class="flex items-center justify-between mb-2">
            <div>
              <span class="font-medium">{{ r.hospitalName }} · {{ r.deptName }}</span>
              <span class="ml-2 text-slate-500 text-sm">{{ r.doctorName }}（{{ r.doctorTitle }}）</span>
            </div>
            <el-button link type="primary" :icon="Printer" @click="print(r)">打印病情单据</el-button>
          </div>
          <div class="text-sm space-y-1">
            <div><span class="text-slate-400">主诉：</span>{{ r.chiefComplaint || '-' }}</div>
            <div><span class="text-slate-400">诊断：</span><span class="font-medium text-sky-700">{{ r.diagnosis || '-' }}</span></div>
            <div><span class="text-slate-400">处方：</span>{{ r.prescription || '-' }}</div>
            <div><span class="text-slate-400">医嘱：</span>{{ r.advice || '-' }}</div>
            <div v-if="r.imageUrl">
              <span class="text-slate-400">附件：</span>
              <el-image :src="r.imageUrl" :preview-src-list="[r.imageUrl]" style="width: 100px" fit="cover" />
            </div>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>

    <el-dialog v-model="visible" title="病情单据" width="720px">
      <PrintSheet v-if="record" :data="record" />
      <template #footer>
        <el-button @click="visible = false">关闭</el-button>
        <el-button type="primary" :icon="Printer" @click="doPrint">打印</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import dayjs from 'dayjs'
import { patientApi } from '@/api'
import PrintSheet from '@/components/PrintSheet.vue'
import { Printer } from '@element-plus/icons-vue'

const records = ref([])
const visible = ref(false)
const record = ref(null)

const formatDate = (s) => dayjs(s).format('YYYY-MM-DD HH:mm')

async function print(r) {
  record.value = (await patientApi.recordByAppointment(r.appointmentId)).data
  visible.value = true
}
function doPrint() { nextTick(() => window.print()) }

onMounted(async () => { records.value = (await patientApi.records()).data })
</script>
