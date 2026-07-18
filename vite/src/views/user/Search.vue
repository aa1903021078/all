<template>
  <div class="page-wrap">
    <div class="search-hero card">
      <el-input
        v-model="keyword"
        size="large"
        placeholder="搜索店铺名 / 菜谱名 / 食材(如:番茄)"
        @keyup.enter="doSearch"
        clearable
      >
        <template #prepend><el-icon><Search /></el-icon></template>
        <template #append>
          <el-button type="primary" @click="doSearch">搜索</el-button>
        </template>
      </el-input>
      <div class="hot mt-12" v-if="hotKeywords.length">
        <span class="muted text-sm">🔥 热门搜索:</span>
        <el-tag
          v-for="k in hotKeywords"
          :key="k.keyword"
          class="pointer"
          effect="plain"
          @click="quickSearch(k.keyword)"
        >{{ k.keyword }}</el-tag>
      </div>
    </div>

    <div v-loading="loading" class="mt-16">
      <template v-if="searched">
        <div class="section-title">
          🏪 相关店铺 <span class="muted text-sm">({{ result.shops.length }})</span>
        </div>
        <div class="grid-cards" v-if="result.shops.length">
          <ShopCard v-for="s in result.shops" :key="s.id" :shop="s" />
        </div>
        <el-empty v-else description="没有匹配的店铺" :image-size="80" />

        <div class="section-title mt-16">
          🍲 相关菜谱 <span class="muted text-sm">({{ result.recipes.length }})</span>
        </div>
        <div class="grid-cards" v-if="result.recipes.length">
          <RecipeCard v-for="r in result.recipes" :key="r.id" :recipe="r" />
        </div>
        <el-empty v-else description="没有匹配的菜谱" :image-size="80" />
      </template>
      <el-empty v-else description="输入关键词开始搜索美食吧" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { searchApi } from '@/api'
import ShopCard from '@/components/ShopCard.vue'
import RecipeCard from '@/components/RecipeCard.vue'

const route = useRoute()
const keyword = ref('')
const hotKeywords = ref([])
const result = ref({ shops: [], recipes: [] })
const loading = ref(false)
const searched = ref(false)

async function doSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  searched.value = true
  try {
    const res = await searchApi.search(keyword.value.trim())
    result.value = { shops: (res.data && res.data.shops) || [], recipes: (res.data && res.data.recipes) || [] }
  } finally {
    loading.value = false
  }
}

function quickSearch(k) {
  keyword.value = k
  doSearch()
}

onMounted(() => {
  searchApi.hot(10).then((res) => (hotKeywords.value = res.data || []))
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    doSearch()
  }
})
</script>

<style scoped>
.search-hero {
  padding: 24px;
}
.hot {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
