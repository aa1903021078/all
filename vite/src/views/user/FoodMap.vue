<template>
  <div class="page-wrap">
    <div class="section-title">🗺️ 美食地图</div>
    <div class="map-toolbar card">
      <div class="tool-row">
        <span class="muted text-sm tool-label">菜系:</span>
        <el-radio-group v-model="categoryId" size="small" @change="load">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button v-for="c in categories" :key="c.id" :value="c.id">
            {{ c.name }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="tool-row">
        <span class="muted text-sm tool-label">周边雷达:</span>
        <el-radio-group v-model="radius" size="small">
          <el-radio-button :value="0">不限</el-radio-button>
          <el-radio-button :value="1">1km</el-radio-button>
          <el-radio-button :value="3">3km</el-radio-button>
          <el-radio-button :value="5">5km</el-radio-button>
        </el-radio-group>
        <span class="muted text-sm tool-label">人均:</span>
        <el-select v-model="priceMax" size="small" style="width: 110px">
          <el-option label="不限" :value="0" />
          <el-option label="≤50" :value="50" />
          <el-option label="≤100" :value="100" />
          <el-option label="≤200" :value="200" />
        </el-select>
        <span class="muted text-sm tool-label">星级:</span>
        <el-select v-model="minRating" size="small" style="width: 110px">
          <el-option label="不限" :value="0" />
          <el-option label="4分以上" :value="4" />
          <el-option label="4.5分以上" :value="4.5" />
        </el-select>
        <el-checkbox v-model="onlyLit" @change="load">只看我点亮的 🔥</el-checkbox>
      </div>
    </div>

    <div class="map-layout mt-16">
      <div class="map-main" v-loading="loading">
        <SimpleMap
          :shops="filtered"
          @open="goDetail"
          @checkin="doCheckin"
          @reserve="openReserve"
          @consult="doConsult"
        />
      </div>
      <div class="map-side card">
        <div class="side-head">
          共 {{ filtered.length }} 家店铺
          <span v-if="radius" class="muted text-sm">(中心 {{ radius }}km 内)</span>
        </div>
        <div class="side-list">
          <div v-for="s in filtered" :key="s.id" class="side-item" @click="goDetail(s)">
            <img :src="s.cover" class="side-cover" />
            <div class="side-info">
              <div class="bold line-1">{{ s.name }} <span v-if="s.lit">🔥</span></div>
              <StarRating :model-value="Number(s.rating)" :size="12" readonly show-score />
              <div class="muted text-sm line-1">{{ s.categoryName }} · ¥{{ s.avgPrice }}/人</div>
            </div>
          </div>
          <el-empty v-if="!loading && !filtered.length" description="该范围暂无店铺" :image-size="80" />
        </div>
      </div>
    </div>

    <!-- 预约弹窗 -->
    <el-dialog v-model="reserveVisible" :title="'预约 · ' + (reserveShop.name || '')" width="440px">
      <el-form :model="reserveForm" label-width="80px">
        <el-form-item label="到店时间">
          <el-date-picker
            v-model="reserveForm.reserveTime"
            type="datetime"
            placeholder="选择到店时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="人数">
          <el-input-number v-model="reserveForm.peopleCount" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="reserveForm.contactName" placeholder="您的称呼" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="reserveForm.contactPhone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reserveForm.remark" type="textarea" :rows="2" placeholder="靠窗/包间等需求(选填)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reserveVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReserve">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { shopApi, categoryApi, reservationApi, chatApi } from '@/api'
import { useAuthStore } from '@/store/auth'
import SimpleMap from '@/components/SimpleMap.vue'
import StarRating from '@/components/StarRating.vue'

const router = useRouter()
const auth = useAuthStore()

const rawShops = ref([])
const categories = ref([])
const categoryId = ref(null)
const onlyLit = ref(false)
const loading = ref(false)

// 周边雷达筛选
const radius = ref(0)
const priceMax = ref(0)
const minRating = ref(0)

// 地图中心(取当前店铺质心, 作为"我的位置"参考点)
const center = computed(() => {
  const list = rawShops.value.filter((s) => s.longitude != null && s.latitude != null)
  if (!list.length) return null
  let lng = 0
  let lat = 0
  list.forEach((s) => {
    lng += Number(s.longitude)
    lat += Number(s.latitude)
  })
  return { lng: lng / list.length, lat: lat / list.length }
})

// 两点球面距离(km)
function distanceKm(lng1, lat1, lng2, lat2) {
  const R = 6371
  const dLat = ((lat2 - lat1) * Math.PI) / 180
  const dLng = ((lng2 - lng1) * Math.PI) / 180
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLng / 2) * Math.sin(dLng / 2)
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

const filtered = computed(() => {
  let list = rawShops.value.slice()
  if (priceMax.value) {
    list = list.filter((s) => Number(s.avgPrice) <= priceMax.value)
  }
  if (minRating.value) {
    list = list.filter((s) => Number(s.rating) >= minRating.value)
  }
  if (radius.value && center.value) {
    list = list.filter((s) => {
      if (s.longitude == null || s.latitude == null) return false
      const d = distanceKm(center.value.lng, center.value.lat, Number(s.longitude), Number(s.latitude))
      return d <= radius.value
    })
  }
  return list
})

async function load() {
  loading.value = true
  try {
    const res = await shopApi.map({ categoryId: categoryId.value, onlyLit: onlyLit.value })
    rawShops.value = res.data || []
  } finally {
    loading.value = false
  }
}

function goDetail(s) {
  router.push('/shops/' + s.id)
}

function requireLogin() {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/map' } })
    return false
  }
  return true
}

async function doCheckin(s) {
  if (!requireLogin()) return
  if (s.lit) return ElMessage.info('你已点亮过该店铺')
  await shopApi.checkin(s.id)
  const target = rawShops.value.find((x) => x.id === s.id)
  if (target) target.lit = true
  ElMessage.success('已点亮 ' + s.name + ' 🔥')
}

// ---- 预约 ----
const reserveVisible = ref(false)
const submitting = ref(false)
const reserveShop = ref({})
const reserveForm = reactive({
  reserveTime: '', peopleCount: 2, contactName: '', contactPhone: '', remark: '',
})

function openReserve(s) {
  if (!requireLogin()) return
  reserveShop.value = s
  reserveForm.reserveTime = ''
  reserveForm.peopleCount = 2
  reserveForm.contactName = auth.user?.nickname || ''
  reserveForm.contactPhone = auth.user?.phone || ''
  reserveForm.remark = ''
  reserveVisible.value = true
}

async function submitReserve() {
  if (!reserveForm.reserveTime) return ElMessage.warning('请选择到店时间')
  if (!reserveForm.contactPhone) return ElMessage.warning('请填写联系电话')
  submitting.value = true
  try {
    await reservationApi.create({ shopId: reserveShop.value.id, ...reserveForm })
    reserveVisible.value = false
    ElMessage.success('预约已提交,等待商家确认')
  } finally {
    submitting.value = false
  }
}

async function doConsult(s) {
  if (!requireLogin()) return
  await chatApi.session(s.id)
  router.push('/chat')
}

onMounted(() => {
  load()
  categoryApi.list().then((res) => (categories.value = res.data || []))
})
</script>

<style scoped>
.map-toolbar {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.tool-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.tool-label {
  flex-shrink: 0;
}
.map-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 16px;
}
.map-side {
  padding: 12px;
  display: flex;
  flex-direction: column;
  max-height: 520px;
}
.side-head {
  font-weight: 700;
  padding: 4px 4px 10px;
  border-bottom: 1px solid var(--border);
}
.side-list {
  overflow-y: auto;
  flex: 1;
}
.side-item {
  display: flex;
  gap: 10px;
  padding: 10px 4px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;
}
.side-item:hover {
  background: var(--el-color-primary-light-9);
}
.side-cover {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
}
.side-info {
  flex: 1;
  min-width: 0;
}
@media (max-width: 900px) {
  .map-layout {
    grid-template-columns: 1fr;
  }
}
</style>
