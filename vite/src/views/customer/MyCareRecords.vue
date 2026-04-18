<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">护理记录查看</span>
        </div>
      </template>

      <!-- 婴儿筛选 -->
      <div v-if="infantList.length > 0" class="infant-filter">
        <span class="filter-label">查看婴儿：</span>
        <el-radio-group v-model="selectedInfantId" @change="handleInfantChange">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button v-for="infant in infantList" :key="infant.id" :value="infant.id">
            {{ infant.name }}{{ infant.gender === 1 ? '(男)' : '(女)' }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 生活护理记录 -->
        <el-tab-pane label="生活护理记录（月嫂）" name="life">
          <el-table v-loading="lifeLoading" :data="lifeRecords" border stripe>
            <el-table-column prop="staffName" label="月嫂" min-width="100" />
            <el-table-column prop="infantName" label="婴儿" min-width="80" align="center">
              <template #default="{ row }">
                <span v-if="row.infantName">{{ row.infantName }}</span>
                <el-tag v-else type="info" size="small">产妇</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordDate" label="日期" width="120" />
            <el-table-column prop="maternalContent" label="产妇护理内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="infantContent" label="婴儿护理内容" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :icon="View" @click="showLifeDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination v-model:current-page="lifeParams.current" v-model:page-size="lifeParams.size"
              :page-sizes="[10, 20, 50]" :total="lifeTotal" layout="total, sizes, prev, pager, next"
              @size-change="loadLifeRecords" @current-change="loadLifeRecords" />
          </div>
        </el-tab-pane>

        <!-- 医疗护理记录 -->
        <el-tab-pane label="医疗护理记录（医护人员）" name="medical">
          <el-table v-loading="medicalLoading" :data="medicalRecords" border stripe>
            <el-table-column prop="staffName" label="护理人员" min-width="100" />
            <el-table-column prop="infantName" label="婴儿" min-width="80" align="center">
              <template #default="{ row }">
                <span v-if="row.infantName">{{ row.infantName }}</span>
                <el-tag v-else type="info" size="small">产妇</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordDate" label="日期" width="120" />
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
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :icon="View" @click="showMedicalDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination v-model:current-page="medicalParams.current" v-model:page-size="medicalParams.size"
              :page-sizes="[10, 20, 50]" :total="medicalTotal" layout="total, sizes, prev, pager, next"
              @size-change="loadMedicalRecords" @current-change="loadMedicalRecords" />
          </div>
        </el-tab-pane>

        <!-- 饮食方案 -->
        <el-tab-pane label="饮食方案" name="diet">
          <el-table v-loading="dietLoading" :data="dietRecords" border stripe>
            <el-table-column prop="staffName" label="营养师" min-width="100" />
            <el-table-column prop="planDate" label="日期" width="120" />
            <el-table-column prop="breakfast" label="早餐" min-width="140" show-overflow-tooltip />
            <el-table-column prop="lunch" label="午餐" min-width="140" show-overflow-tooltip />
            <el-table-column prop="dinner" label="晚餐" min-width="140" show-overflow-tooltip />
            <el-table-column prop="snack" label="加餐" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :icon="View" @click="showDietDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination v-model:current-page="dietParams.current" v-model:page-size="dietParams.size"
              :page-sizes="[10, 20, 50]" :total="dietTotal" layout="total, sizes, prev, pager, next"
              @size-change="loadDietRecords" @current-change="loadDietRecords" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Life Care Detail -->
    <el-dialog v-model="lifeDetailVisible" title="生活护理记录详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="月嫂">{{ lifeDetail.staffName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ lifeDetail.recordDate }}</el-descriptions-item>
        <el-descriptions-item label="婴儿" v-if="lifeDetail.infantName">{{ lifeDetail.infantName }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section">
        <h4>产妇护理内容</h4>
        <div class="detail-content">{{ lifeDetail.maternalContent || '暂无' }}</div>
      </div>
      <div class="detail-section">
        <h4>婴儿护理内容</h4>
        <div class="detail-content">{{ lifeDetail.infantContent || '暂无' }}</div>
      </div>
      <div v-if="lifeDetail.remark" class="detail-section">
        <h4>备注</h4>
        <div class="detail-content">{{ lifeDetail.remark }}</div>
      </div>
    </el-dialog>

    <!-- Medical Care Detail -->
    <el-dialog v-model="medicalDetailVisible" title="医疗护理记录详情" width="750px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="护理人员">{{ medicalDetail.staffName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ medicalDetail.recordDate }}</el-descriptions-item>
        <el-descriptions-item label="婴儿" v-if="medicalDetail.infantName">{{ medicalDetail.infantName }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section" v-if="medicalMaternalParsed">
        <h4>产妇检查</h4>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item v-for="field in maternalFields" :key="field.key" :label="field.label">
            {{ medicalMaternalParsed[field.key] || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section" v-if="medicalInfantParsed">
        <h4>婴儿检查</h4>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item v-for="field in infantFields" :key="field.key" :label="field.label">
            {{ medicalInfantParsed[field.key] || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-if="medicalDetail.remark" class="detail-section">
        <h4>备注</h4>
        <div class="detail-content">{{ medicalDetail.remark }}</div>
      </div>
    </el-dialog>

    <!-- Diet Plan Detail -->
    <el-dialog v-model="dietDetailVisible" title="饮食方案详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="营养师">{{ dietDetail.staffName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ dietDetail.planDate }}</el-descriptions-item>
        <el-descriptions-item label="早餐">{{ dietDetail.breakfast || '无' }}</el-descriptions-item>
        <el-descriptions-item label="午餐">{{ dietDetail.lunch || '无' }}</el-descriptions-item>
        <el-descriptions-item label="晚餐">{{ dietDetail.dinner || '无' }}</el-descriptions-item>
        <el-descriptions-item label="加餐">{{ dietDetail.snack || '无' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ dietDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { View } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { getCareLifeList, getCareMedicalList, getDietPlanList, getInfantsByOrder, getOrderList } from '@/api/index.js'

const store = useStore()
const user = computed(() => store.getters.user)

const activeTab = ref('life')
const selectedInfantId = ref(null)
const infantList = ref([])

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
const lifeRecords = ref([])
const lifeTotal = ref(0)
const lifeParams = reactive({ current: 1, size: 10 })
const lifeDetailVisible = ref(false)
const lifeDetail = ref({})

// Medical care
const medicalLoading = ref(false)
const medicalRecords = ref([])
const medicalTotal = ref(0)
const medicalParams = reactive({ current: 1, size: 10 })
const medicalDetailVisible = ref(false)
const medicalDetail = ref({})
const medicalMaternalParsed = ref(null)
const medicalInfantParsed = ref(null)

// Diet plan
const dietLoading = ref(false)
const dietRecords = ref([])
const dietTotal = ref(0)
const dietParams = reactive({ current: 1, size: 10 })
const dietDetailVisible = ref(false)
const dietDetail = ref({})

const loadInfants = async () => {
  try {
    const orderRes = await getOrderList({ current: 1, size: 100, customerId: user.value.id })
    const orders = orderRes.data.records || []
    const allInfants = []
    for (const order of orders) {
      try {
        const res = await getInfantsByOrder(order.id)
        if (res.data) allInfants.push(...res.data)
      } catch { /* ignore */ }
    }
    infantList.value = allInfants
  } catch { /* ignore */ }
}

const loadLifeRecords = async () => {
  lifeLoading.value = true
  try {
    const params = { ...lifeParams, customerId: user.value.id }
    if (selectedInfantId.value) params.infantId = selectedInfantId.value
    const res = await getCareLifeList(params)
    lifeRecords.value = res.data.records
    lifeTotal.value = res.data.total
  } finally {
    lifeLoading.value = false
  }
}

const loadMedicalRecords = async () => {
  medicalLoading.value = true
  try {
    const params = { ...medicalParams, customerId: user.value.id }
    if (selectedInfantId.value) params.infantId = selectedInfantId.value
    const res = await getCareMedicalList(params)
    medicalRecords.value = res.data.records
    medicalTotal.value = res.data.total
  } finally {
    medicalLoading.value = false
  }
}

const loadDietRecords = async () => {
  dietLoading.value = true
  try {
    const res = await getDietPlanList({ ...dietParams, customerId: user.value.id })
    dietRecords.value = res.data.records
    dietTotal.value = res.data.total
  } finally {
    dietLoading.value = false
  }
}

const showLifeDetail = (row) => { lifeDetail.value = { ...row }; lifeDetailVisible.value = true }
const showMedicalDetail = (row) => {
  medicalDetail.value = { ...row }
  medicalMaternalParsed.value = parseJsonSafe(row.maternalContent)
  medicalInfantParsed.value = parseJsonSafe(row.infantContent)
  medicalDetailVisible.value = true
}
const showDietDetail = (row) => { dietDetail.value = { ...row }; dietDetailVisible.value = true }

const handleTabChange = (tab) => {
  if (tab === 'life') loadLifeRecords()
  else if (tab === 'medical') loadMedicalRecords()
  else if (tab === 'diet') loadDietRecords()
}

const handleInfantChange = () => {
  if (activeTab.value === 'life') loadLifeRecords()
  else if (activeTab.value === 'medical') loadMedicalRecords()
}

onMounted(() => {
  loadInfants()
  loadLifeRecords()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.card-header { display: flex; align-items: center; }
.header-title { font-size: 18px; font-weight: 600; color: #303133; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
.infant-filter { margin-bottom: 16px; display: flex; align-items: center; gap: 12px; }
.filter-label { font-size: 14px; font-weight: 600; color: #303133; }
.text-muted { color: #909399; font-size: 13px; }
.detail-section { margin-top: 20px; }
.detail-section h4 { margin-bottom: 8px; color: #303133; font-size: 15px; border-left: 3px solid #409eff; padding-left: 10px; }
.detail-content { background: #f5f7fa; padding: 12px 16px; border-radius: 4px; white-space: pre-wrap; line-height: 1.8; color: #606266; font-size: 14px; }
</style>
