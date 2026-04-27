<template>
  <div v-if="data">
    <el-page-header @back="$router.back()" :title="'返回'" :content="`接诊：${data.patient?.realName}`" class="mb-4" />

    <el-row :gutter="16">
      <el-col :span="9">
        <el-card class="card-shadow !border-0" header="患者档案">
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="姓名">{{ data.patient?.realName }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ data.patient?.gender || '-' }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ data.patient?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="身份证">{{ data.patient?.idCard || '-' }}</el-descriptions-item>
            <el-descriptions-item label="医院">{{ data.hospital?.name }}</el-descriptions-item>
            <el-descriptions-item label="科室">{{ data.department?.name }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="card-shadow !border-0 mt-4" header="历史病历">
          <el-empty v-if="!data.history?.length" description="暂无历史病历" />
          <ul v-else class="text-sm space-y-2">
            <li v-for="h in data.history" :key="h.id" class="border-b pb-2 last:border-b-0">
              <div class="font-medium">{{ h.diagnosis || '—' }}</div>
              <div class="text-xs text-slate-400">{{ h.createdAt }}</div>
            </li>
          </ul>
        </el-card>
      </el-col>

      <el-col :span="15">
        <el-card class="card-shadow !border-0">
          <template #header>
            <div class="flex justify-between items-center">
              <span class="font-medium">电子病历 · 诊断意见</span>
              <div class="space-x-2">
                <el-button type="primary" :icon="Edit" @click="save">{{ readonly ? '修改并保存' : '完成接诊' }}</el-button>
                <el-button :icon="Printer" @click="print">打印病情单据</el-button>
                <el-button type="warning" :icon="Connection" @click="referralVisible = true">发起转诊</el-button>
              </div>
            </div>
          </template>

          <el-form :model="form" label-position="top">
            <el-form-item label="主诉">
              <el-input v-model="form.chiefComplaint" type="textarea" :rows="2" placeholder="患者本次就诊主要不适..." />
            </el-form-item>
            <el-form-item label="诊断意见">
              <el-input v-model="form.diagnosis" type="textarea" :rows="2" placeholder="主要诊断 / 鉴别诊断" />
            </el-form-item>
            <el-form-item label="处方">
              <el-input v-model="form.prescription" type="textarea" :rows="3" placeholder="药品 + 用法用量..." />
            </el-form-item>
            <el-form-item label="医嘱与建议">
              <el-input v-model="form.advice" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="检查影像 / 附件（可上传）">
              <el-upload
                :action="uploadUrl"
                :headers="{ Authorization: 'Bearer ' + token }"
                :show-file-list="false"
                :on-success="onUploadOk"
                :before-upload="beforeUpload"
                accept="image/*"
              >
                <el-button :icon="Upload">点击上传</el-button>
              </el-upload>
              <div v-if="form.imageUrl" class="mt-2">
                <el-image :src="form.imageUrl" :preview-src-list="[form.imageUrl]" style="width: 160px" />
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="printVisible" title="病情单据" width="720px">
      <PrintSheet v-if="data" :data="data" />
      <template #footer>
        <el-button @click="printVisible = false">关闭</el-button>
        <el-button type="primary" :icon="Printer" @click="doPrint">打印</el-button>
      </template>
    </el-dialog>

    <ReferralDialog
      v-model:visible="referralVisible"
      :patient-id="data.patient?.id"
      :default-summary="form.diagnosis"
      @ok="onReferralOk"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Printer, Connection, Upload } from '@element-plus/icons-vue'
import { doctorApi, uploadUrl } from '@/api'
import { useUserStore } from '@/store/user'
import PrintSheet from '@/components/PrintSheet.vue'
import ReferralDialog from '@/components/ReferralDialog.vue'

const props = defineProps({ appointmentId: { type: [String, Number], required: true } })

const us = useUserStore()
const token = computed(() => us.token)

const data = ref(null)
const form = reactive({ chiefComplaint: '', diagnosis: '', prescription: '', advice: '', imageUrl: '' })
const printVisible = ref(false)
const referralVisible = ref(false)

const readonly = computed(() => !!data.value?.record)

async function load() {
  data.value = (await doctorApi.recordByAppointment(props.appointmentId)).data
  if (data.value.record) {
    Object.assign(form, {
      chiefComplaint: data.value.record.chiefComplaint || '',
      diagnosis: data.value.record.diagnosis || '',
      prescription: data.value.record.prescription || '',
      advice: data.value.record.advice || '',
      imageUrl: data.value.record.imageUrl || ''
    })
  }
}
async function save() {
  if (!form.diagnosis) { ElMessage.warning('请填写诊断意见'); return }
  await doctorApi.diagnose(props.appointmentId, form)
  ElMessage.success('已保存')
  await load()
}
function beforeUpload(file) {
  const ok = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp'].includes(file.type)
  if (!ok) { ElMessage.error('仅支持图片'); return false }
  if (file.size > 10 * 1024 * 1024) { ElMessage.error('文件不能超过 10MB'); return false }
  return true
}
function onUploadOk(res) {
  if (res?.code === 0) { form.imageUrl = res.data.url; ElMessage.success('上传成功') }
  else ElMessage.error(res?.msg || '上传失败')
}
function print() {
  if (!data.value.record) { ElMessage.info('请先保存诊断'); return }
  printVisible.value = true
}
function doPrint() { nextTick(() => window.print()) }
function onReferralOk() { ElMessage.success('转诊申请已提交') }

onMounted(load)
</script>
