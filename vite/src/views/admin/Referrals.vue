<template>
  <el-card class="card-shadow !border-0">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-medium">转诊监管</span>
        <div class="space-x-2">
          <el-select v-model="status" clearable placeholder="状态" style="width:140px" @change="load">
            <el-option v-for="(l, k) in labels" :key="k" :label="l" :value="k" />
          </el-select>
          <el-select v-model="hospitalId" clearable placeholder="筛选医院" style="width:200px" @change="load">
            <el-option v-for="h in hospitals" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </div>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.type === 'UP' ? 'danger' : 'success'">{{ row.type === 'UP' ? '上转' : '下转' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="patientName" label="患者" width="100" />
      <el-table-column label="路径" min-width="240">
        <template #default="{ row }">{{ row.fromHospitalName }} → {{ row.toHospitalName }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }"><el-tag size="small" :type="types[row.status]">{{ labels[row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="setStatus(row, 'COMPLETED')" v-if="row.status !== 'COMPLETED' && row.status !== 'REJECTED'">标记完成</el-button>
          <el-button link type="danger" @click="setStatus(row, 'REJECTED')" v-if="row.status === 'REVIEWING'">强制驳回</el-button>
          <el-button link @click="setStatus(row, 'VISITING')" v-if="row.status === 'ACCEPTED'">置为就诊中</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const hospitals = ref([])
const status = ref('')
const hospitalId = ref(null)

const labels = { DRAFT: '草稿', REVIEWING: '审核中', ACCEPTED: '已接收', VISITING: '就诊中', COMPLETED: '已完成', REJECTED: '已拒绝' }
const types  = { REVIEWING: 'warning', ACCEPTED: 'success', VISITING: 'primary', COMPLETED: '', REJECTED: 'danger', DRAFT: 'info' }

async function load() { list.value = (await adminApi.referrals({ status: status.value, hospitalId: hospitalId.value })).data }
async function setStatus(row, s) {
  await ElMessageBox.confirm(`确认将转诊状态置为「${labels[s]}」？`, '提示', { type: 'warning' })
  await adminApi.setReferralStatus(row.id, s); ElMessage.success('已更新'); load()
}
onMounted(async () => { hospitals.value = (await adminApi.hospitals()).data; load() })
</script>
