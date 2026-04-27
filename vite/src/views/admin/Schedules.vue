<template>
  <el-card class="card-shadow !border-0" header="排班调配">
    <el-table :data="list" stripe>
      <el-table-column prop="workDate" label="日期" width="140" />
      <el-table-column prop="timeSlot" label="时段" width="80">
        <template #default="{ row }">{{ row.timeSlot === 'AM' ? '上午' : '下午' }}</template>
      </el-table-column>
      <el-table-column prop="hospitalName" label="医院" />
      <el-table-column prop="deptName" label="科室" width="120" />
      <el-table-column prop="doctorName" label="医生" width="120" />
      <el-table-column label="号源" width="180">
        <template #default="{ row }">
          <el-progress :percentage="Math.round((row.totalQuota - row.remainingQuota) * 100 / Math.max(1,row.totalQuota))"
            :format="() => `${row.remainingQuota}/${row.totalQuota}`" />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 'OPEN' ? 'success' : 'info'">
            {{ row.status === 'OPEN' ? '正常' : '已停诊' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="70">
        <template #default="{ row }"><el-tag size="small" effect="plain" type="info">v{{ row.version }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="adjust(row)">调整号源</el-button>
          <el-button v-if="row.status === 'OPEN'" link type="danger" @click="close(row)">停诊</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="调整号源" width="360px">
      <el-form label-width="100px">
        <el-form-item label="新号源数">
          <el-input-number v-model="newQuota" :min="0" :max="200" />
        </el-form-item>
        <p class="text-xs text-slate-400">当前版本号 v{{ cur?.version }}，提交时将做乐观锁校验。</p>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdjust">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const visible = ref(false)
const cur = ref(null)
const newQuota = ref(0)

async function load() { list.value = (await adminApi.schedules()).data }
function adjust(row) { cur.value = row; newQuota.value = row.totalQuota; visible.value = true }
async function confirmAdjust() {
  await adminApi.updateSchedule(cur.value.id, { totalQuota: newQuota.value })
  ElMessage.success('已保存'); visible.value = false; load()
}
async function close(row) {
  const { value } = await ElMessageBox.prompt('停诊原因', '停诊', { type: 'warning' })
  await adminApi.closeSchedule(row.id, value); ElMessage.success('已停诊'); load()
}
onMounted(load)
</script>
