<template>
  <div class="yq-page max-w-[1100px] mx-auto">
    <div class="yq-card">
      <div class="flex items-center justify-between mb-3">
        <div class="text-lg font-bold">我的订单</div>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
      <el-table :data="list" border>
        <el-table-column prop="orderId" label="订单号" width="300" />
        <el-table-column prop="itemId" label="商品ID" width="90" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.status === '1'" type="success">已支付</el-tag>
            <el-tag v-else-if="row.status === '2'" type="info">已关闭</el-tag>
            <el-tag v-else type="warning">未支付</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '0'">
              <el-button size="small" type="primary" @click="pay(row)">支付宝支付</el-button>
              <el-button size="small" @click="mockPayOne(row)">模拟支付</el-button>
              <el-button size="small" type="danger" plain @click="cancel(row)">取消</el-button>
            </template>
            <span v-else class="text-gray-400 text-xs">—</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-center mt-4">
        <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                       layout="prev, pager, next" @current-change="load" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myOrders } from '@/api/biz'
import { cancelOrder, mockPay, payUrl } from '@/api/comm'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
let timer = null

async function load() {
  const d = await myOrders({ page: page.value, size })
  list.value = d.records
  total.value = d.total
}

async function pay(row) {
  try {
    // 带 Authorization 头拿到表单 HTML，再写入新窗口
    const html = await request.get(payUrl(row.orderId), { responseType: 'text' })
    const w = window.open('', '_blank')
    if (!w) {
      ElMessage.warning('请允许弹出新窗口')
      return
    }
    w.document.open()
    w.document.write(html)
    w.document.close()
    // 给用户 3 秒后开始轮询订单状态
    startPolling(row.orderId)
  } catch (e) { /* error toast handled by interceptor */ }
}

async function mockPayOne(row) {
  await ElMessageBox.confirm('跳过沙箱直接把该订单标记为已支付？仅开发 / 联调可用。', '模拟支付', {
    type: 'warning'
  })
  await mockPay(row.orderId)
  ElMessage.success('已标记支付')
  load()
}

async function cancel(row) {
  await ElMessageBox.confirm('取消订单后不可恢复，确认继续？', '提示', { type: 'warning' })
  await cancelOrder(row.orderId)
  ElMessage.success('已取消')
  load()
}

function startPolling(orderId) {
  let n = 0
  const t = setInterval(async () => {
    n++
    await load()
    const o = list.value.find(x => x.orderId === orderId)
    if ((o && o.status !== '0') || n >= 30) clearInterval(t)
  }, 3000)
}

onMounted(() => {
  load()
  // 每 15 秒自动刷新一次，感知超时关闭
  timer = setInterval(load, 15000)
})
onBeforeUnmount(() => timer && clearInterval(timer))
</script>
