<template>
  <div class="yq-bigscreen">
    <!-- 顶栏 -->
    <div class="yq-bs-header">
      <div class="yq-bs-title">
        <span class="yq-bs-dot"></span>
        阅趣阁 · 数据大屏
      </div>
      <div class="yq-bs-time">{{ clock }}</div>
      <div class="flex gap-3">
        <el-button size="small" @click="rebuild" plain>重建推荐</el-button>
        <el-button size="small" @click="refresh" plain>刷新</el-button>
        <el-button size="small" @click="$router.push('/admin')" type="danger" plain>退出大屏</el-button>
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="yq-bs-kpis">
      <div class="yq-bs-kpi"><div class="label">用户总数</div><div class="val">{{ ov.userTotal || 0 }}</div></div>
      <div class="yq-bs-kpi"><div class="label">图书总数</div><div class="val">{{ ov.bookTotal || 0 }}</div></div>
      <div class="yq-bs-kpi"><div class="label">订单总数</div><div class="val">{{ ov.orderTotal || 0 }}</div></div>
      <div class="yq-bs-kpi"><div class="label">已支付订单</div><div class="val" style="color:#0ff">{{ ov.orderPaid || 0 }}</div></div>
      <div class="yq-bs-kpi"><div class="label">今日行为数</div><div class="val" style="color:#ffcd3c">{{ ov.actionsToday || 0 }}</div></div>
      <div class="yq-bs-kpi"><div class="label">累计收款</div><div class="val" style="color:#ff6b6b">¥{{ ov.amountPaid || 0 }}</div></div>
    </div>

    <!-- 主体 3 列 -->
    <div class="yq-bs-grid">
      <div class="yq-bs-box">
        <div class="yq-bs-boxtitle">实时热榜 · Top 10 图书</div>
        <div ref="hotRef" class="yq-bs-chart"></div>
      </div>
      <div class="yq-bs-box">
        <div class="yq-bs-boxtitle">近 7 天订单趋势</div>
        <div ref="trendRef" class="yq-bs-chart"></div>
      </div>
      <div class="yq-bs-box">
        <div class="yq-bs-boxtitle">图书类型分布</div>
        <div ref="pieRef" class="yq-bs-chart"></div>
      </div>
      <div class="yq-bs-box col-span-2">
        <div class="yq-bs-boxtitle">收入金额趋势（¥）</div>
        <div ref="amountRef" class="yq-bs-chart"></div>
      </div>
      <div class="yq-bs-box">
        <div class="yq-bs-boxtitle">实时行为流</div>
        <div class="yq-bs-events">
          <div v-for="(e, i) in events" :key="i" class="yq-bs-event">
            <span class="yq-bs-action" :class="actionClass(e.action)">{{ e.action }}</span>
            <span class="text-white/70">user #{{ e.userId }}</span>
            <span class="text-white/50">→ {{ e.type }} #{{ e.id }}</span>
          </div>
          <el-empty v-if="events.length === 0" description="等待事件..." :image-size="60" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  dashOverview, dashHotBooks, dashOrderTrend, dashBookTypeDist, dashRecentEvents,
  rebuildRecommend
} from '@/api/book'

const ov = ref({})
const events = ref([])
const clock = ref('')

const hotRef = ref()
const trendRef = ref()
const pieRef = ref()
const amountRef = ref()
let hotChart, trendChart, pieChart, amountChart
let timer, clockTimer

function fmt(d) {
  const pad = n => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function actionClass(a) {
  return {
    'bg-red-500': a === 'PAY',
    'bg-orange-500': a === 'ORDER',
    'bg-pink-500': a === 'FAVORITE',
    'bg-blue-500': a === 'VIEW' || a === 'READ',
    'bg-gray-500': !['PAY','ORDER','FAVORITE','VIEW','READ'].includes(a)
  }
}

async function refresh() {
  try { ov.value = await dashOverview() } catch (e) {}
  try {
    const hot = await dashHotBooks(10)
    hotChart && hotChart.setOption({
      grid: { left: 150, right: 20, top: 10, bottom: 10 },
      tooltip: {},
      xAxis: { type: 'value', axisLabel: { color: '#9aa3b2' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } } },
      yAxis: {
        type: 'category',
        data: hot.map(x => x.name || ('#' + x.id)).reverse(),
        axisLabel: { color: '#fff', width: 140, overflow: 'truncate' }
      },
      series: [{
        type: 'bar',
        data: hot.map(x => x.score).reverse(),
        barWidth: 14,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#c20c0c' }, { offset: 1, color: '#ff6b6b' }
          ])
        },
        label: { show: true, position: 'right', color: '#fff' }
      }]
    })
  } catch (e) {}
  try {
    const trend = await dashOrderTrend()
    trendChart && trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: trend.map(x => x.date.slice(5)), axisLabel: { color: '#9aa3b2' } },
      yAxis: { type: 'value', axisLabel: { color: '#9aa3b2' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } } },
      series: [{
        type: 'line', smooth: true, symbol: 'circle',
        data: trend.map(x => x.count),
        lineStyle: { color: '#0ff', width: 3 },
        areaStyle: { color: 'rgba(0,255,255,0.15)' },
        itemStyle: { color: '#0ff' }
      }]
    })
    amountChart && amountChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 60, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: trend.map(x => x.date.slice(5)), axisLabel: { color: '#9aa3b2' } },
      yAxis: { type: 'value', axisLabel: { color: '#9aa3b2' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } } },
      series: [{
        type: 'bar',
        data: trend.map(x => x.amount || 0),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ffcd3c' }, { offset: 1, color: '#ff6b6b' }
          ])
        },
        label: { show: true, position: 'top', color: '#fff', formatter: '¥{c}' }
      }]
    })
  } catch (e) {}
  try {
    const pie = await dashBookTypeDist()
    pieChart && pieChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { textStyle: { color: '#9aa3b2' }, bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        data: pie,
        label: { color: '#fff' },
        itemStyle: { borderColor: '#0f1117', borderWidth: 2 }
      }]
    })
  } catch (e) {}
  try {
    const raw = await dashRecentEvents(40)
    events.value = raw.map(line => {
      try { return JSON.parse(line) } catch (e) { return null }
    }).filter(Boolean)
  } catch (e) {}
}

async function rebuild() {
  try {
    const s = await rebuildRecommend()
    ElMessage.success(`推荐矩阵重建完成（users=${s.users}, items=${s.items}）`)
  } catch (e) {}
}

onMounted(async () => {
  await nextTick()
  hotChart = echarts.init(hotRef.value)
  trendChart = echarts.init(trendRef.value)
  pieChart = echarts.init(pieRef.value)
  amountChart = echarts.init(amountRef.value)
  window.addEventListener('resize', resize)
  clock.value = fmt(new Date())
  clockTimer = setInterval(() => { clock.value = fmt(new Date()) }, 1000)
  await refresh()
  timer = setInterval(refresh, 10000)
})

function resize() {
  hotChart?.resize(); trendChart?.resize(); pieChart?.resize(); amountChart?.resize()
}

onBeforeUnmount(() => {
  timer && clearInterval(timer)
  clockTimer && clearInterval(clockTimer)
  window.removeEventListener('resize', resize)
  hotChart?.dispose(); trendChart?.dispose(); pieChart?.dispose(); amountChart?.dispose()
})
</script>

<style scoped>
.yq-bigscreen {
  min-height: 100vh;
  background: #0f1117;
  color: #fff;
  padding: 16px 24px;
  background-image:
    radial-gradient(circle at 20% 20%, rgba(194,12,12,0.15), transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(0,255,255,0.08), transparent 40%);
  background-attachment: fixed;
}
.yq-bs-header {
  display: flex; align-items: center; justify-content: space-between;
  padding-bottom: 10px; border-bottom: 1px solid rgba(255,255,255,0.08);
}
.yq-bs-title { font-size: 22px; font-weight: 700; letter-spacing: 2px; display: flex; align-items: center; gap: 10px; }
.yq-bs-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: #c20c0c; box-shadow: 0 0 10px #c20c0c; animation: pulse 1.2s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.4} }
.yq-bs-time { font-family: 'JetBrains Mono', monospace; color: #0ff; font-size: 16px; }

.yq-bs-kpis { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin: 14px 0; }
.yq-bs-kpi {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  padding: 12px 16px;
}
.yq-bs-kpi .label { font-size: 12px; color: #9aa3b2; }
.yq-bs-kpi .val { font-size: 28px; font-weight: 700; margin-top: 4px; font-family: 'JetBrains Mono', monospace; }

.yq-bs-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; grid-auto-rows: 300px; gap: 14px; }
.yq-bs-grid .col-span-2 { grid-column: span 2; }
.yq-bs-box {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  padding: 12px;
  display: flex; flex-direction: column;
  overflow: hidden;
}
.yq-bs-boxtitle { font-size: 14px; color: #9aa3b2; margin-bottom: 6px; border-left: 3px solid #c20c0c; padding-left: 8px; }
.yq-bs-chart { flex: 1; }

.yq-bs-events { flex: 1; overflow-y: auto; font-size: 12px; font-family: 'JetBrains Mono', monospace; }
.yq-bs-event { display: flex; gap: 8px; align-items: center; padding: 4px 0; border-bottom: 1px dashed rgba(255,255,255,0.04); }
.yq-bs-action { display: inline-block; min-width: 56px; text-align: center; padding: 2px 6px; border-radius: 4px; font-size: 11px; color: #fff; }
</style>
