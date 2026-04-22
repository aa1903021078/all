<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 生活护理记录 -->
        <el-tab-pane label="生活护理记录（月嫂）" name="life">
          <el-table v-loading="lifeLoading" :data="lifeData" border stripe @row-click="(row) => showDetail('life', row)">
            <el-table-column prop="customerName" label="客户" min-width="100" />
            <el-table-column prop="staffName" label="月嫂" min-width="100" />
            <el-table-column prop="infantName" label="婴儿" min-width="80" align="center">
              <template #default="{ row }">
                <span v-if="row.infantName">{{ row.infantName }}</span>
                <el-tag v-else type="info" size="small">产妇</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordDate" label="记录日期" width="120" />
            <el-table-column prop="maternalContent" label="产妇护理内容" min-width="180" show-overflow-tooltip />
            <el-table-column prop="infantContent" label="婴儿护理内容" min-width="180" show-overflow-tooltip />
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          </el-table>
          <div class="pagination-container">
            <el-pagination v-model:current-page="lifeParams.current" v-model:page-size="lifeParams.size"
              :page-sizes="[10, 20, 50]" :total="lifeTotal" layout="total, sizes, prev, pager, next, jumper"
              @size-change="getLifeList" @current-change="getLifeList" />
          </div>
        </el-tab-pane>

        <!-- 医疗护理记录 -->
        <el-tab-pane label="医疗护理记录（医护人员）" name="medical">
          <el-table v-loading="medicalLoading" :data="medicalData" border stripe @row-click="(row) => showDetail('medical', row)">
            <el-table-column prop="customerName" label="客户" min-width="100" />
            <el-table-column prop="staffName" label="护理人员" min-width="100" />
            <el-table-column prop="infantName" label="婴儿" min-width="80" align="center">
              <template #default="{ row }">
                <span v-if="row.infantName">{{ row.infantName }}</span>
                <el-tag v-else type="info" size="small">产妇</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordDate" label="记录日期" width="120" />
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
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          </el-table>
          <div class="pagination-container">
            <el-pagination v-model:current-page="medicalParams.current" v-model:page-size="medicalParams.size"
              :page-sizes="[10, 20, 50]" :total="medicalTotal" layout="total, sizes, prev, pager, next, jumper"
              @size-change="getMedicalList" @current-change="getMedicalList" />
          </div>
        </el-tab-pane>

        <!-- 饮食方案 -->
        <el-tab-pane label="饮食方案" name="diet">
          <el-table v-loading="dietLoading" :data="dietData" border stripe @row-click="(row) => showDetail('diet', row)">
            <el-table-column prop="customerName" label="客户" min-width="90" />
            <el-table-column prop="staffName" label="营养师" min-width="90" />
            <el-table-column prop="planDate" label="日期" width="120" />
            <el-table-column prop="breakfast" label="早餐" min-width="120" show-overflow-tooltip />
            <el-table-column prop="lunch" label="午餐" min-width="120" show-overflow-tooltip />
            <el-table-column prop="dinner" label="晚餐" min-width="120" show-overflow-tooltip />
            <el-table-column prop="snack" label="加餐" min-width="100" show-overflow-tooltip />
            <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip />
          </el-table>
          <div class="pagination-container">
            <el-pagination v-model:current-page="dietParams.current" v-model:page-size="dietParams.size"
              :page-sizes="[10, 20, 50]" :total="dietTotal" layout="total, sizes, prev, pager, next, jumper"
              @size-change="getDietList" @current-change="getDietList" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Life Detail Dialog -->
    <el-dialog v-model="detailVisible" title="记录详情" width="700px">
      <template v-if="detailType === 'life'">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户">{{ detailData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="月嫂">{{ detailData.staffName }}</el-descriptions-item>
          <el-descriptions-item label="记录日期">{{ detailData.recordDate }}</el-descriptions-item>
          <el-descriptions-item label="婴儿" v-if="detailData.infantName">{{ detailData.infantName }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-section">
          <h4>产妇护理内容</h4>
          <div class="detail-content">{{ detailData.maternalContent || '暂无' }}</div>
        </div>
        <div class="detail-section">
          <h4>婴儿护理内容</h4>
          <div class="detail-content">{{ detailData.infantContent || '暂无' }}</div>
        </div>
        <div v-if="detailData.remark" class="detail-section">
          <h4>备注</h4>
          <div class="detail-content">{{ detailData.remark }}</div>
        </div>
      </template>
      <template v-else-if="detailType === 'medical'">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户">{{ detailData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="护理人员">{{ detailData.staffName }}</el-descriptions-item>
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
      </template>
      <template v-else>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户">{{ detailData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="营养师">{{ detailData.staffName }}</el-descriptions-item>
          <el-descriptions-item label="日期" :span="2">{{ detailData.planDate }}</el-descriptions-item>
          <el-descriptions-item label="早餐" :span="2">{{ detailData.breakfast || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="午餐" :span="2">{{ detailData.lunch || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="晚餐" :span="2">{{ detailData.dinner || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="加餐" :span="2">{{ detailData.snack || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '暂无' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCareLifeList, getCareMedicalList, getDietPlanList } from '@/api/index.js'

const activeTab = ref('life')

const maternalFields = [
  { key: '体温', label: '体温' }, { key: '体重', label: '体重' },
  { key: '血压', label: '血压' }, { key: '脉搏', label: '脉搏' },
  { key: '心率', label: '心率' }, { key: '子宫复旧情况', label: '子宫复旧情况' },
  { key: '恶露观察', label: '恶露观察' }, { key: '伤口情况', label: '伤口情况' },
  { key: '乳房充盈程度', label: '乳房充盈程度' }, { key: '是否有硬块', label: '是否有硬块' },
  { key: '乳房是否皲裂', label: '乳房是否皲裂' }, { key: '乳腺疏通', label: '乳腺疏通' }
]

const infantFields = [
  { key: '体温体重身长', label: '体温/体重/身长' }, { key: '黄疸值', label: '黄疸值' },
  { key: '大小便次数与性状', label: '大小便次数与性状' }, { key: '红屁股情况', label: '红屁股情况' },
  { key: '尿布疹情况', label: '尿布疹情况' }
]

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

// Life care
const lifeLoading = ref(false)
const lifeData = ref([])
const lifeTotal = ref(0)
const lifeParams = reactive({ current: 1, size: 10 })

// Medical care
const medicalLoading = ref(false)
const medicalData = ref([])
const medicalTotal = ref(0)
const medicalParams = reactive({ current: 1, size: 10 })

// Diet plan
const dietLoading = ref(false)
const dietData = ref([])
const dietTotal = ref(0)
const dietParams = reactive({ current: 1, size: 10 })

// Detail
const detailVisible = ref(false)
const detailType = ref('')
const detailData = ref({})
const detailMaternalParsed = ref(null)
const detailInfantParsed = ref(null)

const getLifeList = async () => {
  lifeLoading.value = true
  try {
    const res = await getCareLifeList(lifeParams)
    lifeData.value = res.data.records
    lifeTotal.value = res.data.total
  } finally {
    lifeLoading.value = false
  }
}

const getMedicalList = async () => {
  medicalLoading.value = true
  try {
    const res = await getCareMedicalList(medicalParams)
    medicalData.value = res.data.records
    medicalTotal.value = res.data.total
  } finally {
    medicalLoading.value = false
  }
}

const getDietList = async () => {
  dietLoading.value = true
  try {
    const res = await getDietPlanList(dietParams)
    dietData.value = res.data.records
    dietTotal.value = res.data.total
  } finally {
    dietLoading.value = false
  }
}

const handleTabChange = (tab) => {
  if (tab === 'life' && lifeData.value.length === 0) getLifeList()
  else if (tab === 'medical' && medicalData.value.length === 0) getMedicalList()
  else if (tab === 'diet' && dietData.value.length === 0) getDietList()
}

const showDetail = (type, row) => {
  detailType.value = type
  detailData.value = { ...row }
  detailMaternalParsed.value = null
  detailInfantParsed.value = null
  if (type === 'medical') {
    detailMaternalParsed.value = parseJsonSafe(row.maternalContent)
    detailInfantParsed.value = parseJsonSafe(row.infantContent)
  }
  detailVisible.value = true
}

onMounted(() => getLifeList())
</script>

<style scoped>
.app-container { padding: 20px; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
.text-muted { color: #909399; font-size: 13px; }
.detail-section { margin-top: 20px; }
.detail-section h4 { margin-bottom: 8px; color: #303133; font-size: 15px; border-left: 3px solid #409eff; padding-left: 10px; }
.detail-content { background: #f5f7fa; padding: 12px 16px; border-radius: 4px; white-space: pre-wrap; line-height: 1.8; color: #606266; font-size: 14px; }
</style>
