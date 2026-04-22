<template>
  <div class="yq-page max-w-[1200px] mx-auto">
    <div class="yq-card mb-4 flex items-center gap-3">
      <el-input v-model="keyword" placeholder="搜索书名/简介" clearable class="max-w-[300px]" @keyup.enter="reload" />
      <el-select v-model="type" placeholder="全部分类" clearable class="max-w-[160px]" @change="reload">
        <el-option v-for="t in types" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" @click="reload">搜索</el-button>
    </div>

    <div class="grid grid-cols-4 gap-4">
      <div v-for="b in list" :key="b.id"
           class="yq-card cursor-pointer hover:shadow-lg transition"
           @click="$router.push('/books/' + b.id)">
        <div class="aspect-[3/4] overflow-hidden rounded bg-gray-100">
          <img :src="b.coverUrl" class="w-full h-full object-cover"
               @error="(e) => e.target.style.display='none'" />
        </div>
        <div class="mt-2 font-medium truncate">{{ b.name }}</div>
        <div class="text-xs text-gray-500 truncate">{{ b.description }}</div>
        <div class="text-xs text-gray-400 mt-1 flex justify-between">
          <span>{{ b.type }}</span>
          <span v-if="b.rating" class="text-red-600">★ {{ b.rating }}</span>
        </div>
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
import { pageBooks } from '@/api/book'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 12
const keyword = ref('')
const type = ref('')
const types = ['编程', '数据库', '前端', '网络', '操作系统', '人工智能', '古典']

function reload() {
  page.value = 1
  load()
}
async function load() {
  const data = await pageBooks({ page: page.value, size, keyword: keyword.value, type: type.value })
  list.value = data.records
  total.value = data.total
}

onMounted(load)
</script>
