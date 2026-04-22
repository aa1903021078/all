<template>
  <div class="yq-page max-w-[1200px] mx-auto">
    <div class="yq-card mb-4 flex gap-3">
      <el-input v-model="keyword" placeholder="搜索商品" clearable class="max-w-[300px]" @keyup.enter="reload" />
      <el-button type="primary" @click="reload">搜索</el-button>
    </div>
    <div class="grid grid-cols-4 gap-4">
      <div v-for="i in list" :key="i.itemId" class="yq-card">
        <div class="aspect-square overflow-hidden rounded bg-gray-100 mb-2">
          <img :src="i.photo" class="w-full h-full object-cover"
               @error="(e) => e.target.style.display='none'" />
        </div>
        <div class="font-medium truncate">{{ i.itemName }}</div>
        <div class="flex justify-between items-center mt-2">
          <span class="text-red-600 text-lg font-bold">¥{{ i.price }}</span>
          <el-button size="small" type="primary" @click="buy(i)">购买</el-button>
        </div>
        <div class="text-xs text-gray-400 mt-1">库存 {{ i.stock }}</div>
      </div>
    </div>
    <div class="flex justify-center mt-6">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageItems, createOrder } from '@/api/biz'

const list = ref([]); const total = ref(0); const page = ref(1); const size = 12
const keyword = ref('')
function reload() { page.value = 1; load() }
async function load() {
  const data = await pageItems({ page: page.value, size, keyword: keyword.value })
  list.value = data.records
  total.value = data.total
}
async function buy(item) {
  await ElMessageBox.confirm(`购买「${item.itemName}」，金额 ¥${item.price}？`, '确认下单', { type: 'warning' })
  await createOrder({ itemId: item.itemId })
  ElMessage.success('订单已创建，请到"我的订单"完成支付（支付宝沙箱将在第 2 期接入）')
}
onMounted(load)
</script>
