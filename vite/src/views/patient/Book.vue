<template>
  <el-card class="card-shadow !border-0">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-medium">在线预约挂号</span>
        <el-tag type="info" effect="plain">实时号源 · 乐观锁防超卖</el-tag>
      </div>
    </template>

    <el-form inline class="mb-4">
      <el-form-item label="医院">
        <el-select v-model="filter.hospitalId" placeholder="全部医院" clearable style="width: 220px"
          @change="onHospitalChange">
          <el-option v-for="h in hospitals" :key="h.id" :label="`${h.name}（${h.level}）`" :value="h.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="科室">
        <el-select v-model="filter.deptId" placeholder="全部科室" clearable style="width: 180px">
          <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="filter.from" type="date" value-format="YYYY-MM-DD" placeholder="从此日开始" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load" :icon="Search">查询</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="schedules.length === 0" description="未找到符合条件的号源" />
    <el-row :gutter="16" v-else>
      <el-col :span="8" v-for="s in schedules" :key="s.id" class="mb-4">
        <el-card shadow="hover" class="card-shadow !border-0">
          <div class="flex justify-between items-start">
            <div>
              <div class="text-base font-medium">{{ s.doctorName }}
                <el-tag size="small" type="success" class="ml-1">{{ s.doctorTitle }}</el-tag>
              </div>
              <div class="text-sm text-slate-500 mt-1">{{ s.hospitalName }} · {{ s.deptName }}</div>
              <div class="text-sm text-slate-500 mt-1">
                <el-icon><Calendar /></el-icon>
                {{ s.workDate }} · {{ s.timeSlot === 'AM' ? '上午' : '下午' }}
              </div>
            </div>
            <div class="text-right">
              <div class="text-2xl font-semibold"
                :class="s.remainingQuota > 0 ? 'text-emerald-500' : 'text-slate-300'">
                {{ s.remainingQuota }}
              </div>
              <div class="text-xs text-slate-400">/ {{ s.totalQuota }}</div>
            </div>
          </div>
          <el-button class="!w-full mt-3" type="primary" :disabled="s.remainingQuota <= 0 || s.status !== 'OPEN'"
            @click="book(s)">
            {{ s.status !== 'OPEN' ? '已停诊' : (s.remainingQuota <= 0 ? '已约满' : '立即预约') }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { catalogApi, patientApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const hospitals = ref([])
const depts = ref([])
const schedules = ref([])
const filter = reactive({ hospitalId: null, deptId: null, from: '' })

async function loadHospitals() {
  hospitals.value = (await catalogApi.hospitals()).data
}
async function loadDepts() {
  depts.value = (await catalogApi.departments(filter.hospitalId)).data
}
function onHospitalChange() { filter.deptId = null; loadDepts() }

async function load() {
  schedules.value = (await catalogApi.schedules({
    hospitalId: filter.hospitalId, deptId: filter.deptId, from: filter.from || undefined
  })).data
}

async function book(s) {
  await ElMessageBox.confirm(
    `确认预约：${s.hospitalName} · ${s.deptName} · ${s.doctorName}（${s.workDate} ${s.timeSlot === 'AM' ? '上午' : '下午'}）？`,
    '确认预约', { type: 'info' })
  try {
    await patientApi.book(s.id)
    ElMessage.success('预约成功！')
    load()
  } catch (e) { /* http interceptor handled */ }
}

onMounted(async () => { await loadHospitals(); await loadDepts(); await load() })
</script>
