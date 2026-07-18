<template>
  <div class="page-wrap" v-loading="loading">
    <template v-if="shop.id">
      <!-- 头部 -->
      <div class="detail-head card">
        <div class="gallery">
          <img :src="activeImage" class="main-img" />
          <div class="thumbs">
            <img
              v-for="(img, i) in gallery"
              :key="i"
              :src="img"
              :class="{ active: activeImage === img }"
              @click="activeImage = img"
            />
          </div>
        </div>
        <div class="info">
          <div class="flex items-center justify-between">
            <h1 class="shop-name">{{ shop.name }}</h1>
            <span v-if="shop.recommend === 1" class="pill">🏆 平台推荐</span>
          </div>
          <div class="flex items-center gap-12 mt-8">
            <StarRating :model-value="Number(shop.rating)" :size="18" readonly show-score />
            <span class="muted text-sm">{{ shop.ratingCount || 0 }} 条评分</span>
          </div>
          <div class="meta mt-12">
            <div><el-icon><PriceTag /></el-icon> 人均 <span class="price">¥{{ shop.avgPrice }}</span></div>
            <div><el-icon><Dish /></el-icon> {{ shop.categoryName || '美食' }}</div>
            <div><el-icon><Clock /></el-icon> {{ shop.businessHours || '营业中' }}</div>
            <div><el-icon><Location /></el-icon> {{ shop.address }}</div>
            <div v-if="shop.phone"><el-icon><Phone /></el-icon> {{ shop.phone }}</div>
          </div>
          <p class="muted mt-12">{{ shop.description }}</p>
          <div class="detail-stats">
            <span>👀 {{ shop.viewCount || 0 }} 浏览</span>
            <span>🔥 {{ shop.checkinCount || 0 }} 点亮</span>
          </div>
          <div class="action-bar mt-16">
            <el-button :type="shop.lit ? 'warning' : 'primary'" @click="doCheckin">
              🔥 {{ shop.lit ? '已点亮' : '点亮打卡' }}
            </el-button>
            <el-button :type="shop.favorited ? 'danger' : 'default'" plain @click="doFavorite">
              {{ shop.favorited ? '❤️ 已收藏' : '🤍 收藏' }}
            </el-button>
            <el-button @click="reserveVisible = true"><el-icon><Calendar /></el-icon>&nbsp;预约到店</el-button>
            <el-button @click="contactMerchant"><el-icon><ChatDotRound /></el-icon>&nbsp;咨询商家</el-button>
            <el-button type="success" plain @click="goWriteNote"><el-icon><EditPen /></el-icon>&nbsp;写探店笔记</el-button>
          </div>
        </div>
      </div>

      <!-- 推荐菜品 -->
      <div class="section-title mt-16">🍽️ 推荐菜品</div>
      <div class="dish-list" v-if="dishes.length">
        <div v-for="d in dishes" :key="d.id" class="dish-card card">
          <img :src="d.image" class="dish-img" />
          <div class="dish-body">
            <div class="bold line-1">{{ d.name }}</div>
            <div class="price">¥{{ d.price }}</div>
            <div class="muted text-sm line-2">{{ d.description }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else description="商家暂未上传推荐菜品" :image-size="80" />

      <!-- 探店笔记 -->
      <div class="section-title mt-16">📝 探店笔记 ({{ notes.length }})</div>
      <div class="grid-cards" v-if="notes.length">
        <NoteCard v-for="n in notes" :key="n.id" :note="n" />
      </div>
      <el-empty v-else description="还没有探店笔记,快来抢沙发" :image-size="80" />
    </template>

    <!-- 预约弹窗 -->
    <el-dialog v-model="reserveVisible" title="预约到店" width="420px">
      <el-form :model="reserveForm" label-width="80px">
        <el-form-item label="到店时间">
          <el-date-picker v-model="reserveForm.reserveTime" type="datetime" placeholder="选择时间" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="用餐人数">
          <el-input-number v-model="reserveForm.peopleCount" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="reserveForm.contactName" placeholder="您的称呼" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="reserveForm.contactPhone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reserveForm.remark" type="textarea" placeholder="口味/包间等需求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reserveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReserve">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { shopApi, noteApi, favoriteApi, reservationApi, chatApi } from '@/api'
import { useAuthStore } from '@/store/auth'
import StarRating from '@/components/StarRating.vue'
import NoteCard from '@/components/NoteCard.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const shop = ref({})
const dishes = ref([])
const notes = ref([])
const loading = ref(false)
const activeImage = ref('')
const reserveVisible = ref(false)
const reserveForm = reactive({
  reserveTime: '', peopleCount: 2, contactName: '', contactPhone: '', remark: '',
})

const gallery = computed(() => {
  const arr = []
  if (shop.value.cover) arr.push(shop.value.cover)
  try {
    const imgs = JSON.parse(shop.value.images || '[]')
    imgs.forEach((i) => { if (i && arr.indexOf(i) === -1) arr.push(i) })
  } catch (e) { /* ignore */ }
  return arr
})

function requireLogin() {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function load() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await shopApi.detail(id)
    shop.value = res.data || {}
    activeImage.value = gallery.value[0] || ''
    shopApi.dishes(id).then((r) => (dishes.value = r.data || []))
    noteApi.byShop(id).then((r) => (notes.value = r.data || []))
  } finally {
    loading.value = false
  }
}

async function doCheckin() {
  if (!requireLogin()) return
  const res = await shopApi.checkin(shop.value.id)
  shop.value.lit = res.data
  shop.value.checkinCount = (shop.value.checkinCount || 0) + (res.data ? 1 : -1)
  ElMessage.success(res.data ? '点亮成功 🔥' : '已取消点亮')
}

async function doFavorite() {
  if (!requireLogin()) return
  const res = await favoriteApi.toggle('SHOP', shop.value.id)
  shop.value.favorited = res.data
  ElMessage.success(res.data ? '已收藏' : '已取消收藏')
}

function goWriteNote() {
  if (!requireLogin()) return
  router.push({ path: '/publish', query: { type: 'note', shopId: shop.value.id } })
}

async function contactMerchant() {
  if (!requireLogin()) return
  const res = await chatApi.session(shop.value.id)
  router.push({ path: '/chat', query: { sessionId: res.data.id } })
}

async function submitReserve() {
  if (!reserveForm.reserveTime) return ElMessage.warning('请选择到店时间')
  if (!reserveForm.contactPhone) return ElMessage.warning('请填写手机号')
  await reservationApi.create({ shopId: shop.value.id, ...reserveForm })
  reserveVisible.value = false
  ElMessage.success('预约已提交,等待商家确认')
}

onMounted(load)
</script>

<style scoped>
.detail-head {
  display: flex;
  gap: 24px;
  padding: 20px;
}
.gallery {
  width: 400px;
  flex-shrink: 0;
}
.main-img {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 10px;
}
.thumbs {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  overflow-x: auto;
}
.thumbs img {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
}
.thumbs img.active {
  border-color: var(--brand);
}
.info {
  flex: 1;
  min-width: 0;
}
.shop-name {
  margin: 0;
  font-size: 26px;
}
.meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  color: #606266;
}
.meta > div {
  display: flex;
  align-items: center;
  gap: 6px;
}
.detail-stats {
  margin-top: 12px;
  display: flex;
  gap: 16px;
  color: var(--text-muted);
  font-size: 13px;
}
.action-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.dish-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}
.dish-card {
  display: flex;
  overflow: hidden;
}
.dish-img {
  width: 90px;
  height: 90px;
  object-fit: cover;
}
.dish-body {
  padding: 8px 10px;
  flex: 1;
  min-width: 0;
}
@media (max-width: 800px) {
  .detail-head {
    flex-direction: column;
  }
  .gallery {
    width: 100%;
  }
}
</style>
