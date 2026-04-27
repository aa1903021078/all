<template>
  <el-card class="card-shadow !border-0">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-medium">操作日志与异常审计</span>
        <div class="space-x-2">
          <el-input v-model="keyword" placeholder="搜索操作 / 用户 / 目标" clearable style="width:280px" @keyup.enter="load" />
          <el-button :icon="Search" @click="load">查询</el-button>
        </div>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="action" label="操作" width="160" />
      <el-table-column prop="target" label="目标" width="120" />
      <el-table-column label="结果" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.success ? 'success' : 'danger'">{{ row.success ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="durationMs" label="耗时" width="80">
        <template #default="{ row }">{{ row.durationMs }}ms</template>
      </el-table-column>
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
      <el-table-column prop="params" label="参数" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="160" />
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'
import { Search } from '@element-plus/icons-vue'

const list = ref([]); const keyword = ref('')
async function load() { list.value = (await adminApi.logs({ keyword: keyword.value, limit: 200 })).data }
onMounted(load)
</script>
