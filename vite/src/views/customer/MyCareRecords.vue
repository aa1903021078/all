<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">护理记录查看</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 生活护理记录 -->
        <el-tab-pane label="生活护理记录" name="life">
          <el-table v-loading="lifeLoading" :data="lifeRecords" border stripe>
            <el-table-column prop="staffName" label="月嫂" min-width="100" />
            <el-table-column prop="careDate" label="日期" width="120" />
            <el-table-column prop="motherCare" label="产妇护理内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="babyCare" label="婴儿护理内容" min-width="200" show-overflow-tooltip />
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
        <el-tab-pane label="医疗护理记录" name="medical">
          <el-table v-loading="medicalLoading" :data="medicalRecords" border stripe>
            <el-table-column prop="staffName" label="护理人员" min-width="100" />
            <el-table-column prop="careDate" label="日期" width="120" />
            <el-table-column prop="careContent" label="护理内容" min-width="300" show-overflow-tooltip />
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
    <el-dialog v-model="lifeDetailVisible" title="生活护理记录详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="月嫂">{{ lifeDetail.staffName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ lifeDetail.careDate }}</el-descriptions-item>
        <el-descriptions-item label="产妇护理内容">{{ lifeDetail.motherCare || '无' }}</el-descriptions-item>
        <el-descriptions-item label="婴儿护理内容">{{ lifeDetail.babyCare || '无' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ lifeDetail.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="记录时间">{{ lifeDetail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- Medical Care Detail -->
    <el-dialog v-model="medicalDetailVisible" title="医疗护理记录详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="护理人员">{{ medicalDetail.staffName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ medicalDetail.careDate }}</el-descriptions-item>
        <el-descriptions-item label="护理内容">{{ medicalDetail.careContent || '无' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ medicalDetail.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="记录时间">{{ medicalDetail.createTime }}</el-descriptions-item>
      </el-descriptions>
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
        <el-descriptions-item label="记录时间">{{ dietDetail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { View } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { getCareLifeList, getCareMedicalList, getDietPlanList } from '@/api/index.js'

const store = useStore()
const user = computed(() => store.getters.user)

const activeTab = ref('life')

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

// Diet plan
const dietLoading = ref(false)
const dietRecords = ref([])
const dietTotal = ref(0)
const dietParams = reactive({ current: 1, size: 10 })
const dietDetailVisible = ref(false)
const dietDetail = ref({})

const loadLifeRecords = async () => {
  lifeLoading.value = true
  try {
    const res = await getCareLifeList({ ...lifeParams, customerId: user.value.id })
    lifeRecords.value = res.data.records
    lifeTotal.value = res.data.total
  } finally {
    lifeLoading.value = false
  }
}

const loadMedicalRecords = async () => {
  medicalLoading.value = true
  try {
    const res = await getCareMedicalList({ ...medicalParams, customerId: user.value.id })
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
const showMedicalDetail = (row) => { medicalDetail.value = { ...row }; medicalDetailVisible.value = true }
const showDietDetail = (row) => { dietDetail.value = { ...row }; dietDetailVisible.value = true }

const handleTabChange = (tab) => {
  if (tab === 'life') loadLifeRecords()
  else if (tab === 'medical') loadMedicalRecords()
  else if (tab === 'diet') loadDietRecords()
}

onMounted(() => loadLifeRecords())
</script>

<style scoped>
.app-container { padding: 20px; }
.card-header { display: flex; align-items: center; }
.header-title { font-size: 18px; font-weight: 600; color: #303133; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
