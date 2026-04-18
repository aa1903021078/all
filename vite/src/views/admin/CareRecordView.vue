<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 生活护理记录 -->
        <el-tab-pane label="生活护理记录" name="life">
          <el-table v-loading="lifeLoading" :data="lifeData" border stripe @row-click="(row) => showDetail('life', row)">
            <el-table-column prop="customerName" label="客户" min-width="100" />
            <el-table-column prop="staffName" label="月嫂" min-width="100" />
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
        <el-tab-pane label="医疗护理记录" name="medical">
          <el-table v-loading="medicalLoading" :data="medicalData" border stripe @row-click="(row) => showDetail('medical', row)">
            <el-table-column prop="customerName" label="客户" min-width="100" />
            <el-table-column prop="staffName" label="护理人员" min-width="100" />
            <el-table-column prop="recordDate" label="记录日期" width="120" />
            <el-table-column prop="content" label="护理内容" min-width="240" show-overflow-tooltip />
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

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="记录详情" width="600px">
      <el-descriptions :column="2" border>
        <template v-if="detailType === 'life'">
          <el-descriptions-item label="客户">{{ detailData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="月嫂">{{ detailData.staffName }}</el-descriptions-item>
          <el-descriptions-item label="记录日期" :span="2">{{ detailData.recordDate }}</el-descriptions-item>
          <el-descriptions-item label="产妇护理内容" :span="2">{{ detailData.maternalContent || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="婴儿护理内容" :span="2">{{ detailData.infantContent || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '暂无' }}</el-descriptions-item>
        </template>
        <template v-else-if="detailType === 'medical'">
          <el-descriptions-item label="客户">{{ detailData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="护理人员">{{ detailData.staffName }}</el-descriptions-item>
          <el-descriptions-item label="记录日期" :span="2">{{ detailData.recordDate }}</el-descriptions-item>
          <el-descriptions-item label="护理内容" :span="2">{{ detailData.content || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '暂无' }}</el-descriptions-item>
        </template>
        <template v-else>
          <el-descriptions-item label="客户">{{ detailData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="营养师">{{ detailData.staffName }}</el-descriptions-item>
          <el-descriptions-item label="日期" :span="2">{{ detailData.planDate }}</el-descriptions-item>
          <el-descriptions-item label="早餐" :span="2">{{ detailData.breakfast || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="午餐" :span="2">{{ detailData.lunch || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="晚餐" :span="2">{{ detailData.dinner || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="加餐" :span="2">{{ detailData.snack || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '暂无' }}</el-descriptions-item>
        </template>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCareLifeList, getCareMedicalList, getDietPlanList } from '@/api/index.js'

const activeTab = ref('life')

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
  detailVisible.value = true
}

onMounted(() => getLifeList())
</script>

<style scoped>
.app-container { padding: 20px; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
