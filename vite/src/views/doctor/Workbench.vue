<template>
  <div>
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card class="card-shadow !border-0">
          <div class="text-slate-400 text-sm">待接诊</div>
          <div class="text-2xl font-semibold text-sky-500 mt-2">{{ booked.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="card-shadow !border-0">
          <div class="text-slate-400 text-sm">今日已接诊</div>
          <div class="text-2xl font-semibold text-emerald-500 mt-2">{{ visitedToday }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="card-shadow !border-0">
          <div class="text-slate-400 text-sm">待审核转入</div>
          <div class="text-2xl font-semibold text-amber-500 mt-2">{{ pendingIncoming }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="card-shadow !border-0">
          <div class="text-slate-400 text-sm">我发起的转诊</div>
          <div class="text-2xl font-semibold text-indigo-500 mt-2">{{ outgoingCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="card-shadow !border-0">
      <template #header>
        <div class="flex justify-between items-center">
          <span class="font-medium">预约患者列表</span>
          <el-radio-group v-model="status" @change="load" size="small">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="BOOKED">待接诊</el-radio-button>
            <el-radio-button label="VISITED">已就诊</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="list" stripe>
        <el-table-column prop="patientName" label="患者" width="120" />
        <el-table-column prop="patientGender" label="性别" width="70" />
        <el-table-column prop="patientPhone" label="电话" width="140" />
        <el-table-column label="就诊时间" width="160">
          <template #default="{ row }">{{ row.workDate }} {{ row.timeSlot === 'AM' ? '上午' : '下午' }}</template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.source === 'REFERRAL' ? 'warning' : 'info'">
              {{ row.source === 'REFERRAL' ? '转诊' : '自助' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'BOOKED' ? 'primary' : (row.status === 'VISITED' ? 'success' : 'info')">
              {{ ({BOOKED:'待接诊',VISITED:'已就诊',CANCELLED:'已取消'})[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'BOOKED'" type="primary" link @click="goVisit(row)">开始接诊</el-button>
            <el-button v-if="row.status === 'VISITED'" type="success" link @click="goVisit(row)">查看 / 打印</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { doctorApi } from '@/api'

const router = useRouter()
const list = ref([])
const status = ref('BOOKED')
const incoming = ref([])
const outgoing = ref([])

const booked = computed(() => list.value.filter(x => x.status === 'BOOKED'))
const today = dayjs().format('YYYY-MM-DD')
const visitedToday = computed(() => list.value.filter(x => x.status === 'VISITED' && x.workDate === today).length)
const pendingIncoming = computed(() => incoming.value.filter(x => x.status === 'REVIEWING').length)
const outgoingCount = computed(() => outgoing.value.length)

async function load() {
  list.value = (await doctorApi.appointments(status.value)).data
}
async function loadCounts() {
  incoming.value = (await doctorApi.incoming()).data
  outgoing.value = (await doctorApi.outgoing()).data
}
function goVisit(row) { router.push(`/doctor/visit/${row.id}`) }

onMounted(async () => { await load(); await loadCounts() })
</script>
