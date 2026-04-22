<template>
  <div class="yq-card">
    <el-table :data="list" border>
      <el-table-column prop="orderId" label="订单号" width="300" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="itemId" label="商品ID" width="100" />
      <el-table-column prop="amount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.status === '1'" type="success">已支付</el-tag>
          <el-tag v-else-if="row.status === '2'" type="info">已关闭</el-tag>
          <el-tag v-else type="warning">未支付</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === '0'" size="small" type="success" @click="updateStatus(row, '1')">标记已支付</el-button>
          <el-button v-if="row.status === '0'" size="small" @click="updateStatus(row, '2')">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="flex justify-center mt-4">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                     layout="prev, pager, next" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminPageOrders, setOrderStatus } from '@/api/biz'
const list = ref([]); const total = ref(0); const page = ref(1); const size = 10
async function load() {
  const d = await adminPageOrders({ page: page.value, size }); list.value = d.records; total.value = d.total
}
async function updateStatus(row, status) {
  await setOrderStatus(row.orderId, status); ElMessage.success('已更新'); load()
}
onMounted(load)
</script>
