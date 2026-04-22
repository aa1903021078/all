<template>
  <div class="yq-page max-w-[1000px] mx-auto">
    <div class="yq-card">
      <div class="text-lg font-bold mb-3">我的订单</div>
      <el-table :data="list" border>
        <el-table-column prop="orderId" label="订单号" width="280" />
        <el-table-column prop="itemId" label="商品ID" width="100" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === '1'" type="success">已支付</el-tag>
            <el-tag v-else-if="row.status === '2'" type="info">已关闭</el-tag>
            <el-tag v-else type="warning">未支付</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" />
      </el-table>
      <div class="flex justify-center mt-4">
        <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                       layout="prev, pager, next" @current-change="load" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myOrders } from '@/api/biz'
const list = ref([]); const total = ref(0); const page = ref(1); const size = 10
async function load() {
  const d = await myOrders({ page: page.value, size }); list.value = d.records; total.value = d.total
}
onMounted(load)
</script>
