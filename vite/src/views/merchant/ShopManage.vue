<template>
  <div>
    <div class="flex justify-between items-center mb-12">
      <span class="bold text-lg">我的店铺</span>
      <el-button type="primary" @click="openShop()"><el-icon><Plus /></el-icon>&nbsp;入驻新店铺</el-button>
    </div>

    <div v-loading="loading" class="shop-grid">
      <div v-for="s in shops" :key="s.id" class="card shop-mgr-card">
        <img :src="s.cover" class="mgr-cover" />
        <div class="mgr-body">
          <div class="flex justify-between items-center">
            <span class="bold">{{ s.name }}</span>
            <el-tag :type="statusTag(s.status)" size="small">{{ statusText(s.status) }}</el-tag>
          </div>
          <div class="muted text-sm mt-8 line-1">{{ s.address }}</div>
          <div class="muted text-sm mt-8">人均 ¥{{ s.avgPrice }} · 评分 {{ s.rating }} · 点亮 {{ s.checkinCount }}</div>
          <div class="flex gap-8 mt-12">
            <el-button size="small" @click="openShop(s)">编辑</el-button>
            <el-button size="small" type="primary" plain @click="openDishes(s)">菜品管理</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && !shops.length" description="还没有店铺,点击右上角入驻" />
    </div>

    <!-- 店铺表单 -->
    <el-dialog v-model="shopVisible" :title="shopForm.id ? '编辑店铺' : '入驻新店铺'" width="620px">
      <el-form :model="shopForm" label-width="90px">
        <el-form-item label="店铺名称"><el-input v-model="shopForm.name" /></el-form-item>
        <el-form-item label="菜系">
          <el-select v-model="shopForm.categoryId" placeholder="选择菜系" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址"><el-input v-model="shopForm.address" /></el-form-item>
        <el-form-item label="经纬度">
          <div class="flex gap-8">
            <el-input v-model="shopForm.longitude" placeholder="经度 如 116.40" />
            <el-input v-model="shopForm.latitude" placeholder="纬度 如 39.90" />
          </div>
        </el-form-item>
        <el-form-item label="人均消费"><el-input-number v-model="shopForm.avgPrice" :min="0" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="shopForm.businessHours" placeholder="如 10:00-22:00" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="shopForm.phone" /></el-form-item>
        <el-form-item label="封面图"><ImageUpload v-model="shopForm.cover" /></el-form-item>
        <el-form-item label="实拍图集"><ImageUpload v-model="shopForm.imageList" multiple :limit="6" /></el-form-item>
        <el-form-item label="店铺简介"><el-input v-model="shopForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shopVisible = false">取消</el-button>
        <el-button type="primary" @click="saveShop">保存</el-button>
      </template>
    </el-dialog>

    <!-- 菜品管理 -->
    <el-dialog v-model="dishVisible" :title="dishShop.name + ' · 推荐菜品'" width="640px">
      <div class="flex justify-end mb-12">
        <el-button type="primary" size="small" @click="openDishForm()"><el-icon><Plus /></el-icon>&nbsp;添加菜品</el-button>
      </div>
      <el-table :data="dishes" size="small">
        <el-table-column label="图片" width="70">
          <template #default="{ row }"><img :src="row.image" class="dish-thumb" /></template>
        </el-table-column>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="price" label="价格" width="90">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" text @click="openDishForm(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="delDish(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-divider v-if="dishFormVisible" />
      <el-form v-if="dishFormVisible" :model="dishForm" label-width="70px" class="mt-12">
        <el-form-item label="名称"><el-input v-model="dishForm.name" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="dishForm.price" :min="0" /></el-form-item>
        <el-form-item label="图片"><ImageUpload v-model="dishForm.image" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="dishForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveDish">保存菜品</el-button>
          <el-button @click="dishFormVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shopApi, categoryApi } from '@/api'
import ImageUpload from '@/components/ImageUpload.vue'

const loading = ref(false)
const shops = ref([])
const categories = ref([])

const shopVisible = ref(false)
const shopForm = reactive({})

const dishVisible = ref(false)
const dishShop = ref({})
const dishes = ref([])
const dishFormVisible = ref(false)
const dishForm = reactive({})

const statusTextMap = { 0: '待审核', 1: '营业中', 2: '已下架', 3: '已拒绝' }
const statusTagMap = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
function statusText(s) {
  return statusTextMap[s] || '-'
}
function statusTag(s) {
  return statusTagMap[s] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await shopApi.mine()
    shops.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openShop(s) {
  if (s) {
    let imgs = []
    try { imgs = JSON.parse(s.images || '[]') } catch (e) { imgs = [] }
    Object.assign(shopForm, { ...s, imageList: imgs })
  } else {
    Object.assign(shopForm, {
      id: null, name: '', categoryId: null, address: '', longitude: '', latitude: '',
      avgPrice: 60, businessHours: '', phone: '', cover: '', imageList: [], description: '',
    })
  }
  shopVisible.value = true
}

async function saveShop() {
  if (!shopForm.name) return ElMessage.warning('请填写店铺名称')
  const payload = { ...shopForm, images: JSON.stringify(shopForm.imageList || []) }
  delete payload.imageList
  if (shopForm.id) {
    await shopApi.merchantUpdate(shopForm.id, payload)
    ElMessage.success('已保存')
  } else {
    await shopApi.merchantCreate(payload)
    ElMessage.success('提交成功,等待平台审核')
  }
  shopVisible.value = false
  load()
}

async function openDishes(s) {
  dishShop.value = s
  dishVisible.value = true
  dishFormVisible.value = false
  const res = await shopApi.dishes(s.id)
  dishes.value = res.data || []
}

function openDishForm(d) {
  if (d) {
    Object.assign(dishForm, { ...d })
  } else {
    Object.assign(dishForm, { id: null, shopId: dishShop.value.id, name: '', price: 0, image: '', description: '' })
  }
  dishFormVisible.value = true
}

async function saveDish() {
  if (!dishForm.name) return ElMessage.warning('请填写菜品名称')
  dishForm.shopId = dishShop.value.id
  if (dishForm.id) {
    await shopApi.updateDish(dishForm.id, { ...dishForm })
  } else {
    await shopApi.saveDish({ ...dishForm })
  }
  ElMessage.success('已保存')
  dishFormVisible.value = false
  openDishes(dishShop.value)
}

async function delDish(d) {
  await ElMessageBox.confirm('确定删除该菜品?', '提示', { type: 'warning' })
  await shopApi.deleteDish(d.id)
  openDishes(dishShop.value)
}

onMounted(() => {
  load()
  categoryApi.list().then((res) => (categories.value = res.data || []))
})
</script>

<style scoped>
.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.mgr-cover {
  width: 100%;
  height: 150px;
  object-fit: cover;
}
.mgr-body {
  padding: 12px;
}
.dish-thumb {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  object-fit: cover;
}
</style>
