<template>
  <div class="page-wrap">
    <div class="section-title">🍲 菜谱广场</div>
    <div class="toolbar card">
      <div class="filter-row">
        <span class="muted text-sm">菜系:</span>
        <el-radio-group v-model="query.categoryId" size="small" @change="reload">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-row">
        <span class="muted text-sm">难度:</span>
        <el-radio-group v-model="query.difficulty" size="small" @change="reload">
          <el-radio-button :value="null">不限</el-radio-button>
          <el-radio-button :value="1">简单</el-radio-button>
          <el-radio-button :value="2">中等</el-radio-button>
          <el-radio-button :value="3">困难</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-row">
        <span class="muted text-sm">排序:</span>
        <el-radio-group v-model="query.sort" size="small" @change="reload">
          <el-radio-button value="new">最新</el-radio-button>
          <el-radio-button value="hot">最热</el-radio-button>
          <el-radio-button value="quick">最快手</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="query.keyword"
          placeholder="搜索菜谱"
          size="small"
          style="width: 180px; margin-left: auto"
          clearable
          @keyup.enter="reload"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
    </div>

    <div v-loading="loading" class="grid-cards mt-16">
      <RecipeCard v-for="r in list" :key="r.id" :recipe="r" />
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无菜谱" />

    <div class="flex justify-center mt-16" v-if="total > query.size">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="query.size"
        :current-page="query.current"
        @current-change="pageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { recipeApi, categoryApi } from '@/api'
import RecipeCard from '@/components/RecipeCard.vue'

const list = ref([])
const categories = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({
  current: 1, size: 12, keyword: '', categoryId: null, difficulty: null, sort: 'new',
})

async function load() {
  loading.value = true
  try {
    const res = await recipeApi.page({ ...query })
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

onMounted(() => {
  load()
  categoryApi.list().then((res) => (categories.value = res.data || []))
})
</script>

<style scoped>
.toolbar {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
