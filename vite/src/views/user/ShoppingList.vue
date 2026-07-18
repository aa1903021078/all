<template>
  <div class="page-wrap">
    <div class="section-title">🛒 我的采购清单</div>

    <div class="card add-bar">
      <el-input v-model="newItem.name" placeholder="添加食材,如:西红柿" style="width: 220px" @keyup.enter="addItem" />
      <el-input v-model="newItem.amount" placeholder="用量,如:3个" style="width: 160px" @keyup.enter="addItem" />
      <el-button type="primary" @click="addItem"><el-icon><Plus /></el-icon>&nbsp;添加</el-button>
      <div class="flex-1"></div>
      <el-button type="danger" plain :disabled="!doneCount" @click="clearDone">清除已采购({{ doneCount }})</el-button>
    </div>

    <div class="card list-card mt-16" v-loading="loading">
      <div class="progress-head">
        <span>采购进度</span>
        <el-progress :percentage="progress" :stroke-width="14" style="flex: 1; margin: 0 16px" />
        <span class="bold">{{ doneCount }}/{{ items.length }}</span>
      </div>

      <div v-for="item in items" :key="item.id" class="shop-item" :class="{ done: item.status === 2 }">
        <el-checkbox
          :model-value="item.status === 2"
          @change="(v) => toggleDone(item, v)"
        />
        <div class="item-name">
          {{ item.name }}
          <span class="muted text-sm">{{ item.amount }}</span>
        </div>
        <el-tag v-if="item.recipeId" size="small" effect="plain" type="warning">菜谱</el-tag>
        <el-tag
          size="small"
          :type="statusTag(item.status)"
          class="pointer"
          @click="cycleStatus(item)"
        >{{ statusText(item.status) }}</el-tag>
        <el-icon class="pointer del" @click="removeItem(item)"><Delete /></el-icon>
      </div>
      <el-empty v-if="!loading && !items.length" description="清单是空的,去菜谱页一键添加食材吧" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shoppingApi } from '@/api'

const items = ref([])
const loading = ref(false)
const newItem = reactive({ name: '', amount: '' })

const doneCount = computed(() => items.value.filter((i) => i.status === 2).length)
const progress = computed(() => (items.value.length ? Math.round((doneCount.value / items.value.length) * 100) : 0))

const statusMap = { 0: '待采购', 1: '家中已有', 2: '已采购' }
const tagMap = { 0: 'info', 1: 'warning', 2: 'success' }
function statusText(s) {
  return statusMap[s] || '待采购'
}
function statusTag(s) {
  return tagMap[s] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await shoppingApi.list()
    items.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function addItem() {
  if (!newItem.name.trim()) return ElMessage.warning('请输入食材名称')
  await shoppingApi.add({ name: newItem.name.trim(), amount: newItem.amount.trim(), status: 0 })
  newItem.name = ''
  newItem.amount = ''
  load()
}

async function toggleDone(item, checked) {
  const status = checked ? 2 : 0
  await shoppingApi.updateStatus(item.id, status)
  item.status = status
}

async function cycleStatus(item) {
  const next = (item.status + 1) % 3
  await shoppingApi.updateStatus(item.id, next)
  item.status = next
}

async function removeItem(item) {
  await shoppingApi.remove(item.id)
  load()
}

async function clearDone() {
  await ElMessageBox.confirm('确定清除所有已采购的食材?', '提示', { type: 'warning' })
  await shoppingApi.clearDone()
  ElMessage.success('已清除')
  load()
}

onMounted(load)
</script>

<style scoped>
.add-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  flex-wrap: wrap;
}
.list-card {
  padding: 8px 16px 16px;
}
.progress-head {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
}
.shop-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 4px;
  border-bottom: 1px solid #f5f5f5;
}
.item-name {
  flex: 1;
  font-size: 15px;
}
.shop-item.done .item-name {
  text-decoration: line-through;
  color: var(--text-muted);
}
.del {
  color: #c0c4cc;
  cursor: pointer;
}
.del:hover {
  color: #f56c6c;
}
</style>
