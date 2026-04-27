<template>
  <el-card class="card-shadow !border-0">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-medium">我的排班 · 号源管理</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增排班</el-button>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column prop="workDate" label="日期" width="140" />
      <el-table-column prop="timeSlot" label="时段" width="100">
        <template #default="{ row }">{{ row.timeSlot === 'AM' ? '上午' : '下午' }}</template>
      </el-table-column>
      <el-table-column label="号源" width="160">
        <template #default="{ row }">
          <el-progress :percentage="Math.round((row.totalQuota - row.remainingQuota) * 100 / Math.max(1, row.totalQuota))"
            :format="() => `${row.remainingQuota}/${row.totalQuota}`" />
        </template>
      </el-table-column>
      <el-table-column prop="hospitalName" label="医院" />
      <el-table-column prop="deptName" label="科室" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'" size="small">
            {{ row.status === 'OPEN' ? '正常' : '已停诊' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本号" width="90">
        <template #default="{ row }"><el-tag size="small" type="info" effect="plain">v{{ row.version }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'OPEN'" link type="danger" @click="close(row)">停诊</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="新增排班" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="出诊日期"><el-date-picker v-model="form.workDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="时段">
          <el-radio-group v-model="form.timeSlot">
            <el-radio label="AM">上午</el-radio><el-radio label="PM">下午</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="号源数"><el-input-number v-model="form.totalQuota" :min="1" :max="200" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { doctorApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const visible = ref(false)
const form = reactive({ workDate: '', timeSlot: 'AM', totalQuota: 20 })

async function load() { list.value = (await doctorApi.schedules()).data }
function openCreate() {
  Object.assign(form, { workDate: '', timeSlot: 'AM', totalQuota: 20 })
  visible.value = true
}
async function submit() {
  if (!form.workDate) return ElMessage.warning('请选择日期')
  await doctorApi.createSchedule(form)
  visible.value = false; ElMessage.success('已新增'); load()
}
async function close(row) {
  const { value } = await ElMessageBox.prompt('停诊后该排班下所有预约将自动取消并通知患者，请填写原因：', '停诊', { type: 'warning' })
  await doctorApi.closeSchedule(row.id, value)
  ElMessage.success('已停诊'); load()
}
onMounted(load)
</script>
