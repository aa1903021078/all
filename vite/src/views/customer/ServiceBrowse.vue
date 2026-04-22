<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="queryParams.name" placeholder="搜索服务名称" clearable prefix-icon="Search" style="width: 280px" @keyup.enter="handleQuery" />
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>

      <el-row v-loading="loading" :gutter="20">
        <el-col v-for="item in serviceList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom: 20px">
          <el-card shadow="hover" class="service-card" @click="handleDetail(item)">
            <div class="service-image">
              <el-image v-if="item.image" :src="item.image" fit="cover" class="card-img" />
              <div v-else class="card-img-placeholder">
                <el-icon :size="40" color="#fff"><GoodsFilled /></el-icon>
              </div>
            </div>
            <div class="service-info">
              <div class="service-name">{{ item.name }}</div>
              <el-tag size="small" type="info" class="service-category">{{ item.category || '通用' }}</el-tag>
              <div class="service-price">¥{{ Number(item.price).toFixed(2) }}</div>
              <div class="service-desc">{{ item.description || '暂无描述' }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col v-if="!loading && serviceList.length === 0" :span="24">
          <el-empty description="暂无可用服务" />
        </el-col>
      </el-row>

      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :page-sizes="[8, 12, 24]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList" @current-change="getList" />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="服务详情" width="600px" destroy-on-close>
      <div v-if="detailData" class="detail-content">
        <div class="detail-image-wrap">
          <el-image v-if="detailData.image" :src="detailData.image" fit="contain" class="detail-img" :preview-src-list="[detailData.image]" />
          <div v-else class="detail-img-placeholder">
            <el-icon :size="60" color="#fff"><GoodsFilled /></el-icon>
          </div>
        </div>
        <el-descriptions :column="1" border style="margin-top: 20px">
          <el-descriptions-item label="服务名称">{{ detailData.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ detailData.category || '通用' }}</el-descriptions-item>
          <el-descriptions-item label="价格">
            <span class="price-text">¥{{ Number(detailData.price || 0).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="描述">{{ detailData.description || '暂无描述' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, GoodsFilled } from '@element-plus/icons-vue'
import { getServiceList } from '@/api/index.js'

const loading = ref(false)
const serviceList = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 12, name: '', status: 1 })

const detailVisible = ref(false)
const detailData = ref({})

const getList = async () => {
  loading.value = true
  try {
    const res = await getServiceList(queryParams)
    serviceList.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.current = 1; getList() }
const handleReset = () => { queryParams.name = ''; queryParams.current = 1; getList() }

const handleDetail = (item) => {
  detailData.value = { ...item }
  detailVisible.value = true
}

onMounted(() => getList())
</script>

<style scoped>
.app-container { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }

.service-card { cursor: pointer; transition: transform 0.3s, box-shadow 0.3s; border-radius: 8px; overflow: hidden; }
.service-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.service-card :deep(.el-card__body) { padding: 0; }

.service-image { width: 100%; height: 180px; overflow: hidden; }
.card-img { width: 100%; height: 180px; }
.card-img-placeholder {
  width: 100%; height: 180px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.service-info { padding: 16px; }
.service-name { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.service-category { margin-bottom: 8px; }
.service-price { font-size: 20px; font-weight: 700; color: #e6a23c; margin-bottom: 8px; }
.service-desc { font-size: 13px; color: #909399; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.detail-content { text-align: center; }
.detail-img { max-width: 100%; max-height: 300px; border-radius: 8px; }
.detail-img-placeholder {
  width: 100%; height: 200px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 8px;
}
.price-text { font-size: 18px; font-weight: 700; color: #e6a23c; }
</style>
