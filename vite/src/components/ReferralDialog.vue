<template>
  <el-dialog
    :model-value="visible"
    title="发起转诊"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="转诊类型">
        <el-radio-group v-model="form.type">
          <el-radio label="UP">上转（基层 → 上级）</el-radio>
          <el-radio label="DOWN">下转（上级 → 基层）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="目标医院">
        <el-select v-model="form.toHospitalId" placeholder="选择目标医院" filterable style="width: 100%"
          @change="loadDepts">
          <el-option v-for="h in hospitals" :key="h.id" :label="`${h.name}（${h.level}）`" :value="h.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标科室">
        <el-select v-model="form.toDeptId" placeholder="可选" filterable clearable style="width: 100%">
          <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="诊疗摘要">
        <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="病情、当前用药、转诊原因..." />
      </el-form-item>
      <el-form-item label="康复方案" v-if="form.type === 'DOWN'">
        <el-input v-model="form.rehabPlan" type="textarea" :rows="3" placeholder="下转时的康复治疗建议..." />
      </el-form-item>
      <el-form-item label="附件 / 影像">
        <el-upload
          :action="uploadUrl"
          :headers="{ Authorization: 'Bearer ' + token }"
          :show-file-list="false"
          :on-success="onOk"
          accept="image/*,.pdf"
        >
          <el-button :icon="Upload">点击上传</el-button>
        </el-upload>
        <div v-if="form.imageUrl" class="mt-2">
          <el-image :src="form.imageUrl" :preview-src-list="[form.imageUrl]" style="width: 140px" />
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">提交转诊申请</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { catalogApi, doctorApi, uploadUrl } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const props = defineProps({
  visible: Boolean,
  patientId: [Number, String],
  defaultSummary: String
})
const emit = defineEmits(['update:visible', 'ok'])

const us = useUserStore()
const token = computed(() => us.token)

const hospitals = ref([])
const depts = ref([])
const loading = ref(false)
const form = reactive({
  type: 'UP', toHospitalId: null, toDeptId: null,
  summary: '', rehabPlan: '', imageUrl: ''
})

watch(() => props.visible, async (v) => {
  if (v) {
    form.type = 'UP'; form.toHospitalId = null; form.toDeptId = null
    form.summary = props.defaultSummary || ''; form.rehabPlan = ''; form.imageUrl = ''
    if (!hospitals.value.length) hospitals.value = (await catalogApi.hospitals()).data
  }
})

async function loadDepts() {
  form.toDeptId = null
  depts.value = (await catalogApi.departments(form.toHospitalId)).data
}

function onOk(res) {
  if (res?.code === 0) { form.imageUrl = res.data.url; ElMessage.success('上传成功') }
  else ElMessage.error(res?.msg || '上传失败')
}

async function submit() {
  if (!form.toHospitalId) return ElMessage.warning('请选择目标医院')
  if (!form.summary) return ElMessage.warning('请填写诊疗摘要')
  loading.value = true
  try {
    await doctorApi.createReferral({
      patientId: props.patientId,
      type: form.type,
      toHospitalId: form.toHospitalId,
      toDeptId: form.toDeptId,
      summary: form.summary,
      rehabPlan: form.rehabPlan,
      imageUrl: form.imageUrl
    })
    emit('ok'); emit('update:visible', false)
  } finally { loading.value = false }
}
</script>
