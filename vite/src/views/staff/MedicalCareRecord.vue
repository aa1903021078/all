<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-date-picker v-model="queryParams.recordDate" type="date" value-format="YYYY-MM-DD"
            placeholder="选择日期" clearable style="width: 180px" />
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增记录</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="customerName" label="客户姓名" min-width="100" />
        <el-table-column prop="infantName" label="婴儿" min-width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.infantName">{{ row.infantName }}</span>
            <el-tag v-else type="info" size="small">仅产妇</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordDate" label="记录日期" width="120" align="center" />
        <el-table-column label="产妇检查" min-width="200">
          <template #default="{ row }">
            <span v-if="row.maternalContent">{{ summarizeMaternalContent(row.maternalContent) }}</span>
            <span v-else class="text-muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="婴儿检查" min-width="200">
          <template #default="{ row }">
            <span v-if="row.infantContent">{{ summarizeInfantContent(row.infantContent) }}</span>
            <span v-else class="text-muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList" @current-change="getList" />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="关联订单" prop="orderId">
          <el-select v-model="form.orderId" placeholder="请选择订单/客户" filterable style="width: 100%"
            @change="handleOrderChange">
            <el-option v-for="item in orderList" :key="item.id"
              :label="`${item.customerName} - ${item.packageName}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择婴儿" v-if="infantList.length > 0">
          <el-select v-model="form.infantId" clearable placeholder="选择检查的婴儿（不选则仅记录产妇检查）" style="width: 100%">
            <el-option v-for="item in infantList" :key="item.id"
              :label="item.name + (item.gender === 1 ? '(男)' : '(女)')" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD"
            placeholder="选择日期" style="width: 100%" />
        </el-form-item>

        <el-divider content-position="left">产妇检查（填空）</el-divider>
        <el-row :gutter="16">
          <el-col :span="8" v-for="field in maternalFields" :key="field.key">
            <el-form-item :label="field.label" :label-width="field.labelWidth || '120px'">
              <el-input v-model="maternalForm[field.key]" :placeholder="field.placeholder || '请填写'" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">婴儿检查（填空）</el-divider>
        <el-row :gutter="16">
          <el-col :span="12" v-for="field in infantFields" :key="field.key">
            <el-form-item :label="field.label" :label-width="field.labelWidth || '120px'">
              <el-input v-model="infantForm[field.key]" :placeholder="field.placeholder || '请填写'" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider />
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="医疗护理记录详情" width="750px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="客户姓名">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="记录日期">{{ detailData.recordDate }}</el-descriptions-item>
        <el-descriptions-item label="婴儿" v-if="detailData.infantName">{{ detailData.infantName }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section" v-if="detailMaternalParsed">
        <h4>产妇检查</h4>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item v-for="field in maternalFields" :key="field.key" :label="field.label">
            {{ detailMaternalParsed[field.key] || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section" v-if="detailInfantParsed">
        <h4>婴儿检查</h4>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item v-for="field in infantFields" :key="field.key" :label="field.label">
            {{ detailInfantParsed[field.key] || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-if="detailData.remark" class="detail-section">
        <h4>备注</h4>
        <div class="detail-content">{{ detailData.remark }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCareMedicalList, addCareMedical, updateCareMedical, deleteCareMedical, getOrderList, getInfantsByOrder } from '@/api/index.js'

const store = useStore()
const currentUser = computed(() => store.getters.user)

const maternalFields = [
  { key: '体温', label: '体温', placeholder: '如 36.5°C' },
  { key: '体重', label: '体重', placeholder: '如 55kg' },
  { key: '血压', label: '血压', placeholder: '如 120/78mmHg' },
  { key: '脉搏', label: '脉搏', placeholder: '如 72次/分' },
  { key: '心率', label: '心率', placeholder: '如 72次/分' },
  { key: '子宫复旧情况', label: '子宫复旧情况', placeholder: '如 恢复良好' },
  { key: '恶露观察', label: '恶露观察', placeholder: '如 浅红色，量少' },
  { key: '伤口情况', label: '伤口情况', placeholder: '如 愈合良好' },
  { key: '乳房充盈程度', label: '乳房充盈程度', placeholder: '如 泌乳充足' },
  { key: '是否有硬块', label: '是否有硬块', placeholder: '如 无' },
  { key: '乳房是否皲裂', label: '乳房是否皲裂', placeholder: '如 无' },
  { key: '乳腺疏通', label: '乳腺疏通', placeholder: '如 通畅' }
]

const infantFields = [
  { key: '体温体重身长', label: '体温/体重/身长', placeholder: '如 36.7°C, 3.5kg, 52cm' },
  { key: '黄疸值', label: '黄疸值', placeholder: '如 5.8 或 已退' },
  { key: '大小便次数与性状', label: '大小便次数与性状', placeholder: '如 大便3次黄色，小便8次' },
  { key: '红屁股情况', label: '红屁股情况', placeholder: '如 无' },
  { key: '尿布疹情况', label: '尿布疹情况', placeholder: '如 无' }
]

const createEmptyMaternalForm = () => {
  const obj = {}
  maternalFields.forEach(f => { obj[f.key] = '' })
  return obj
}

const createEmptyInfantForm = () => {
  const obj = {}
  infantFields.forEach(f => { obj[f.key] = '' })
  return obj
}

// Query
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10, recordDate: '', staffId: '' })

const getList = async () => {
  loading.value = true
  try {
    queryParams.staffId = currentUser.value?.id
    const res = await getCareMedicalList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.current = 1; getList() }
const handleReset = () => { queryParams.recordDate = ''; queryParams.current = 1; getList() }

// Orders
const orderList = ref([])
const infantList = ref([])
const loadOrders = async () => {
  try {
    const res = await getOrderList({ current: 1, size: 200, staffId: currentUser.value?.id })
    orderList.value = res.data.records || []
  } catch { /* ignore */ }
}

// Form
const dialogVisible = ref(false)
const dialogTitle = ref('新增医疗护理记录')
const formRef = ref(null)
const submitLoading = ref(false)
const isEdit = ref(false)
const maternalForm = reactive(createEmptyMaternalForm())
const infantForm = reactive(createEmptyInfantForm())

const form = reactive({
  id: null,
  orderId: null,
  customerName: '',
  staffId: null,
  infantId: null,
  recordDate: '',
  maternalContent: '',
  infantContent: '',
  remark: ''
})

const rules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const handleOrderChange = async (val) => {
  const order = orderList.value.find(o => o.id === val)
  if (order) form.customerName = order.customerName
  infantList.value = []
  form.infantId = null
  if (val) {
    try {
      const res = await getInfantsByOrder(val)
      infantList.value = res.data || []
    } catch { /* ignore */ }
  }
}

const parseJsonSafe = (str) => {
  if (!str) return null
  try { return JSON.parse(str) } catch { return null }
}

const summarizeMaternalContent = (content) => {
  const obj = parseJsonSafe(content)
  if (!obj) return content?.substring(0, 60) || ''
  const parts = []
  if (obj['体温']) parts.push(`体温${obj['体温']}`)
  if (obj['血压']) parts.push(`血压${obj['血压']}`)
  if (obj['子宫复旧情况']) parts.push(`子宫${obj['子宫复旧情况']}`)
  return parts.join('，') || '已记录'
}

const summarizeInfantContent = (content) => {
  const obj = parseJsonSafe(content)
  if (!obj) return content?.substring(0, 60) || ''
  const parts = []
  if (obj['体温体重身长']) parts.push(obj['体温体重身长'])
  if (obj['黄疸值']) parts.push(`黄疸${obj['黄疸值']}`)
  return parts.join('，') || '已记录'
}

const resetForm = () => {
  Object.assign(form, { id: null, orderId: null, customerName: '', staffId: null, infantId: null, recordDate: '', maternalContent: '', infantContent: '', remark: '' })
  Object.assign(maternalForm, createEmptyMaternalForm())
  Object.assign(infantForm, createEmptyInfantForm())
  infantList.value = []
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogTitle.value = '新增医疗护理记录'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑医疗护理记录'
  Object.assign(form, { ...row })
  const mObj = parseJsonSafe(row.maternalContent)
  if (mObj) Object.assign(maternalForm, mObj)
  const iObj = parseJsonSafe(row.infantContent)
  if (iObj) Object.assign(infantForm, iObj)
  if (row.orderId) {
    try {
      const res = await getInfantsByOrder(row.orderId)
      infantList.value = res.data || []
    } catch { /* ignore */ }
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch { return }
  form.maternalContent = JSON.stringify(maternalForm)
  form.infantContent = JSON.stringify(infantForm)
  form.staffId = currentUser.value?.id
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCareMedical(form)
      ElMessage.success('修改成功')
    } else {
      await addCareMedical(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该医疗护理记录？', '提示', { type: 'warning' }).then(async () => {
    await deleteCareMedical(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// Detail
const detailVisible = ref(false)
const detailData = ref({})
const detailMaternalParsed = ref(null)
const detailInfantParsed = ref(null)

const handleView = (row) => {
  detailData.value = { ...row }
  detailMaternalParsed.value = parseJsonSafe(row.maternalContent)
  detailInfantParsed.value = parseJsonSafe(row.infantContent)
  detailVisible.value = true
}

onMounted(() => {
  getList()
  loadOrders()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
.text-muted { color: #909399; font-size: 13px; }
.detail-section { margin-top: 20px; }
.detail-section h4 { margin-bottom: 8px; color: #303133; font-size: 15px; border-left: 3px solid #409eff; padding-left: 10px; }
.detail-content { background: #f5f7fa; padding: 12px 16px; border-radius: 4px; white-space: pre-wrap; line-height: 1.8; color: #606266; font-size: 14px; }
</style>
