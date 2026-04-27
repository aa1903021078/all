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
      <el-col :span="12">
        <el-card class="card-shadow !border-0" header="最近转诊">
          <el-empty v-if="referrals.length === 0" description="暂无" />
          <el-table v-else :data="referrals.slice(0, 8)" size="small">
            <el-table-column prop="patientName" label="患者" width="80" />
            <el-table-column label="路径">
              <template #default="{ row }">{{ row.fromHospitalName }} → {{ row.toHospitalName }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="card-shadow !border-0" header="最近操作日志">
          <el-empty v-if="logs.length === 0" description="暂无" />
          <el-table v-else :data="logs.slice(0, 8)" size="small">
            <el-table-column prop="username" label="用户" width="100" />
            <el-table-column prop="action" label="操作" />
            <el-table-column label="结果" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.success ? 'success' : 'danger'">{{ row.success ? '成功' : '失败' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api'

const stats = ref({})
const referrals = ref([])
const logs = ref([])

const cards = computed(() => [
  { title: '医疗机构',  value: stats.value.hospitals  || 0, icon: 'OfficeBuilding', color: '#0ea5e9' },
  { title: '科室总数',  value: stats.value.departments || 0, icon: 'Menu', color: '#10b981' },
  { title: '在岗医生',  value: stats.value.doctors    || 0, icon: 'Avatar', color: '#f59e0b' },
  { title: '注册患者',  value: stats.value.patients   || 0, icon: 'User', color: '#ef4444' }
])

onMounted(async () => {
  stats.value = (await adminApi.stats()).data
  referrals.value = (await adminApi.referrals({})).data
  logs.value = (await adminApi.logs({ limit: 20 })).data
})
</script>
