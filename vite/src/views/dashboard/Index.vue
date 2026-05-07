<template>
  <div>
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>用户总数</template>
          <div class="stat-num">{{ stats.userCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>图书总数</template>
          <div class="stat-num">{{ stats.bookCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>置换订单</template>
          <div class="stat-num">{{ stats.exchangeCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>置换成功率</template>
          <div class="stat-num">{{ (stats.successRate || 0).toFixed(1) }}%</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card>
      <template #header>系统概况</template>
      <p>成功置换数量：{{ stats.successCount || 0 }}</p>
      <p>总置换订单数：{{ stats.exchangeCount || 0 }}</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStatsOverview } from '../../api'

const stats = ref({})

onMounted(async () => {
  const res = await getStatsOverview()
  if (res.code === 200) {
    stats.value = res.data
  }
})
</script>

<style scoped>
.stat-num {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  text-align: center;
}
</style>
