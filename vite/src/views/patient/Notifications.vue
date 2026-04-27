<template>
  <el-card class="card-shadow !border-0" header="站内消息">
    <el-empty v-if="list.length === 0" description="暂无消息" />
    <ul v-else class="divide-y">
      <li v-for="n in list" :key="n.id" class="py-3 flex justify-between items-start">
        <div class="flex-1">
          <div class="flex items-center gap-2">
            <el-badge v-if="!n.readFlag" is-dot type="danger" />
            <span class="font-medium">{{ n.title }}</span>
            <el-tag size="small" type="info" effect="plain">{{ n.type }}</el-tag>
          </div>
          <div class="text-sm text-slate-600 mt-1">{{ n.content }}</div>
          <div class="text-xs text-slate-400 mt-1">{{ n.createdAt }}</div>
        </div>
        <el-button v-if="!n.readFlag" link type="primary" @click="markRead(n)">标为已读</el-button>
      </li>
    </ul>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { patientApi } from '@/api'

const list = ref([])
async function load() { list.value = (await patientApi.notifications()).data }
async function markRead(n) { await patientApi.markRead(n.id); n.readFlag = 1 }
onMounted(load)
</script>
