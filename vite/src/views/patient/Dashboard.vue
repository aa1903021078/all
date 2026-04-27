<template>
  <div>
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6" v-for="c in cards" :key="c.title">
        <el-card class="card-shadow !border-0">
          <div class="flex justify-between items-center">
            <div>
              <div class="text-slate-400 text-sm">{{ c.title }}</div>
              <div class="text-2xl font-semibold mt-2" :style="{ color: c.color }">{{ c.value }}</div>
            </div>
            <el-icon :size="36" :color="c.color"><component :is="c.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="14">
        <el-card class="card-shadow !border-0" header="待就诊预约">
          <el-empty v-if="upcoming.length === 0" description="暂无待就诊预约" />
          <el-table v-else :data="upcoming" stripe>
            <el-table-column prop="hospitalName" label="医院" />
            <el-table-column prop="deptName" label="科室" width="120" />
            <el-table-column prop="doctorName" label="医生" width="120" />
            <el-table-column prop="workDate" label="日期" width="120" />
            <el-table-column prop="timeSlot" label="时段" width="80" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="card-shadow !border-0" header="最近通知">
          <el-empty v-if="notes.length === 0" description="暂无消息" />
          <ul v-else class="space-y-2">
            <li v-for="n in notes.slice(0,5)" :key="n.id" class="border-b pb-2 last:border-b-0">
              <div class="font-medium">{{ n.title }}</div>
              <div class="text-xs text-slate-400">{{ n.createdAt }}</div>
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { patientApi } from '@/api'

const apps = ref([])
const records = ref([])
const referrals = ref([])
const notes = ref([])

const upcoming = computed(() => apps.value.filter(a => a.status === 'BOOKED'))

const cards = computed(() => [
  { title: '待就诊预约', value: upcoming.value.length, icon: 'Calendar', color: '#0ea5e9' },
  { title: '历史就诊', value: records.value.length, icon: 'Document', color: '#10b981' },
  { title: '转诊单', value: referrals.value.length, icon: 'Connection', color: '#f59e0b' },
  { title: '未读消息', value: notes.value.filter(n => !n.readFlag).length, icon: 'BellFilled', color: '#ef4444' }
])

onMounted(async () => {
  const [a, r, ref_data, n] = await Promise.all([
    patientApi.myAppointments(),
    patientApi.records(),
    patientApi.referrals(),
    patientApi.notifications()
  ])
  apps.value = a.data; records.value = r.data; referrals.value = ref_data.data; notes.value = n.data
})
</script>
