<template>
  <div>
    <div class="card filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索店铺名/地址" style="width: 200px" clearable @keyup.enter="reload" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px" @change="reload">
        <el-option label="待审核" :value="0" />
        <el-option label="营业中" :value="1" />
        <el-option label="已下架" :value="2" />
        <el-option label="已拒绝" :value="3" />
      </el-select>
      <el-button type="primary" @click="reload"><el-icon><Search /></el-icon>&nbsp;查询</el-button>
      <div class="flex-1"></div>
      <el-button type="success" @click="openForm()"><el-icon><Plus /></el-icon>&nbsp;新增店铺</el-button>
    </div>

    <div class="card mt-16" style="padding: 16px" v-loading="loading">
      <el-table :data="list" style="width: 100%">
        <el-table-column label="店铺" min-width="220">
          <template #default="{ row }">
            <div class="flex items-center gap-8">
              <img :src="row.cover" class="mini-cover" />
              <div>
                <div class="bold">{{ row.name }}</div>
                <div class="muted text-sm line-1">{{ row.address }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="菜系" width="90" />
        <el-table-column label="人均" width="90">
          <template #default="{ row }">¥{{ row.avgPrice }}</template>
        </el-table-column>
        <el-table-column label="评分" width="80">
          <template #default="{ row }">{{ row.rating }}</template>
        </el-table-column>
        <el-table-column label="推荐" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.recommend === 1" @change="(v) => setRecommend(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="changeStatus(row, 1)">通过</el-button>
              <el-button size="small" type="danger" plain @click="changeStatus(row, 3)">拒绝</el-button>
            </template>
            <el-button v-if="row.status === 1" size="small" type="warning" plain @click="changeStatus(row, 2)">下架</el-button>
            <el-button v-if="row.status === 2" size="small" type="success" plain @click="changeStatus(row, 1)">上架</el-button>
            <el-button size="small" @click="openForm(row)">编辑</el-button>
            <el-button size="small" type="danger" text @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-16">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.current"
          @current-change="pageChange"
        />
      </div>
    </div>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑店铺' : '新增店铺'" width="620px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="店铺名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="菜系">
          <el-select v-model="form.categoryId" placeholder="选择菜系" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="经纬度">
          <div class="flex gap-8">
            <el-input v-model="form.longitude" placeholder="经度" />
            <el-input v-model="form.latitude" placeholder="纬度" />
          </div>
        </el-form-item>
        <el-form-item label="人均消费"><el-input-number v-model="form.avgPrice" :min="0" /></el-form-item>
        <el-form-item label="封面图"><ImageUpload v-model="form.cover" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shopApi, categoryApi } from '@/api'
import ImageUpload from '@/components/ImageUpload.vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])
const query = reactive({ current: 1, size: 10, keyword: '', status: null })

const formVisible = ref(false)
const form = reactive({})

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
    const res = await shopApi.adminPage({ ...query })
    list.value = (res.data && res.data.records) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}
function reload() {
  query.current = 1
  load()
}
function pageChange(p) {
  query.current = p
  load()
}

async function changeStatus(row, status) {
  await shopApi.changeStatus(row.id, status)
  row.status = status
  ElMessage.success('操作成功')
}
async function setRecommend(row, v) {
  await shopApi.setRecommend(row.id, v ? 1 : 0)
  row.recommend = v ? 1 : 0
  ElMessage.success('已更新推荐状态')
}
async function remove(row) {
  await ElMessageBox.confirm('确定删除该店铺?此操作不可恢复', '警告', { type: 'warning' })
  await shopApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

function openForm(row) {
  if (row) {
    Object.assign(form, { ...row })
  } else {
    Object.assign(form, {
      id: null, name: '', categoryId: null, address: '', longitude: '', latitude: '',
      avgPrice: 60, cover: '', description: '',
    })
  }
  formVisible.value = true
}
async function save() {
  if (!form.name) return ElMessage.warning('请填写店铺名称')
  if (form.id) {
    await shopApi.adminUpdate(form.id, { ...form })
  } else {
    await shopApi.adminCreate({ ...form })
  }
  ElMessage.success('已保存')
  formVisible.value = false
  load()
}

onMounted(() => {
  load()
  categoryApi.list().then((res) => (categories.value = res.data || []))
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}
.mini-cover {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  object-fit: cover;
}
</style>
