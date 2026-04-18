<template>
  <div class="notice-manage">
    <el-card>
      <template #header>
        <div class="header">
          <span>公告管理</span>
          <el-button type="primary" @click="showAdd = true">+ 发布公告</el-button>
        </div>
      </template>

      <el-table :data="noticeList" v-loading="loading">
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeColor(row.type)">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger">置顶</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读量" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="updateStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="edit(row)">编辑</el-button>
            <el-button type="warning" link @click="toggleTop(row)">
              {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发布公告弹窗 -->
    <el-dialog v-model="showAdd" title="发布公告" width="700px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio-button :label="1">活动通知</el-radio-button>
            <el-radio-button :label="2">政策解读</el-radio-button>
            <el-radio-button :label="3">环保知识</el-radio-button>
            <el-radio-button :label="4">系统公告</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const noticeList = ref([])
const showAdd = ref(false)
const submitting = ref(false)
const form = ref({
  title: '',
  type: 1,
  coverImage: '',
  content: '',
  isTop: 0
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/notice/selectAll', {})
    noticeList.value = res
  } catch (e) {}
  loading.value = false
}

const typeText = (type) => {
  const map = { 1: '活动', 2: '政策', 3: '知识', 4: '公告' }
  return map[type] || '其他'
}

const typeColor = (type) => {
  const map = { 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }
  return map[type] || 'info'
}

const updateStatus = async (row) => {
  try {
    await request.put('/notice/update', { id: row.id, status: row.status })
    ElMessage.success('状态更新成功')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

const toggleTop = async (row) => {
  try {
    await request.put(`/notice/setTop/${row.id}`, null, { params: { isTop: row.isTop === 1 ? 0 : 1 } })
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const submit = async () => {
  submitting.value = true
  try {
    await request.post('/notice/add', form.value)
    ElMessage.success('发布成功')
    showAdd.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  } finally {
    submitting.value = false
  }
}

const edit = (row) => {
  // 编辑公告
}
</script>