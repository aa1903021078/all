<template>
  <div class="collector-audit">
    <div class="page-header">
      <h2>资质审核</h2>
      <p>审核回收员注册申请</p>
    </div>

    <el-table :data="collectors" stripe v-loading="loading" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="头像" width="80">
        <template #default="{ row }">
          <el-avatar :size="40" :src="row.avatar || ''" />
        </template>
      </el-table-column>
      <!-- 关键修复：后端返回的是 name 不是 realName -->
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="idCard" label="身份证号" width="180" />
      <!-- 关键修复：后端返回的是 serviceArea 不是 workArea -->
      <el-table-column prop="serviceArea" label="工作区域" show-overflow-tooltip min-width="150" />
      <el-table-column prop="auditStatus" label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.auditStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="row.auditStatus === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="row.auditStatus === 2" type="danger">已拒绝</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="viewDetail(row)">查看</el-button>
          <el-button type="success" size="small" @click="audit(row, 1)">通过</el-button>
          <el-button type="danger" size="small" @click="audit(row, 2)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && collectors.length === 0" description="暂无待审核数据" />

    <el-dialog v-model="detailDialogVisible" title="审核详情" width="600px">
      <div v-if="currentCollector" class="audit-detail">
        <div class="detail-item">
          <span class="label">姓名：</span>
          <span class="value">{{ currentCollector.name }}</span>
        </div>
        <div class="detail-item">
          <span class="label">手机号：</span>
          <span class="value">{{ currentCollector.phone }}</span>
        </div>
        <div class="detail-item">
          <span class="label">身份证号：</span>
          <span class="value">{{ currentCollector.idCard }}</span>
        </div>
        <div class="detail-item">
          <span class="label">工作区域：</span>
          <span class="value">{{ currentCollector.serviceArea }}</span>
        </div>
        <div class="detail-item">
          <span class="label">身份证正面：</span>
          <div class="image-box">
            <img v-if="currentCollector.idCardFront" :src="currentCollector.idCardFront" class="id-card-img" />
            <span v-else class="no-image">未上传</span>
          </div>
        </div>
        <div class="detail-item">
          <span class="label">身份证反面：</span>
          <div class="image-box">
            <img v-if="currentCollector.idCardBack" :src="currentCollector.idCardBack" class="id-card-img" />
            <span v-else class="no-image">未上传</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="audit(currentCollector, 1)">通过</el-button>
        <el-button type="danger" @click="audit(currentCollector, 2)">拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {selectPendingAudit, auditPass, auditReject} from '@/api/collector'

const collectors = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentCollector = ref(null)

const fetchCollectors = async () => {
  loading.value = true
  try {
    const res = await selectPendingAudit()
    console.log('API响应:', res) // 调试用

    // 关键修复：使用 == 代替 ===，兼容字符串 '200' 和数字 200
    if (res.code == 200) {
      collectors.value = res.data || []
      console.log('数据赋值成功:', collectors.value)
    } else {
      ElMessage.warning(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('获取待审核回收员失败:', error)
    ElMessage.error('网络请求失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  currentCollector.value = row
  detailDialogVisible.value = true
}

const audit = async (row, status) => {
  if (!row) return

  try {
    await ElMessageBox.confirm(
        `确定要${status === 1 ? '通过' : '拒绝'}该回收员的申请吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: status === 1 ? 'success' : 'warning'
        }
    )

    let res
    if (status === 1) {
      res = await auditPass(row.id)
    } else {
      res = await auditReject(row.id, '不符合审核要求')
    }

    // 同样修复：使用 == 判断
    if (res.code == 200) {
      ElMessage.success(status === 1 ? '审核通过' : '已拒绝')
      detailDialogVisible.value = false
      fetchCollectors()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchCollectors()
})
</script>

<style scoped lang="scss">
.collector-audit {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-header {
  margin-bottom: 20px;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
  }

  p {
    color: #6b7280;
  }
}

.audit-detail {
  .detail-item {
    display: flex;
    padding: 12px 0;
    border-bottom: 1px solid #f3f4f6;
    align-items: flex-start;

    &:last-child {
      border-bottom: none;
    }

    .label {
      width: 100px;
      color: #6b7280;
      flex-shrink: 0;
    }

    .value {
      flex: 1;
      color: #1f2937;
      font-weight: 500;
    }

    .image-box {
      flex: 1;

      .id-card-img {
        max-width: 100%;
        max-height: 150px;
        border-radius: 4px;
        border: 1px solid #e5e7eb;
      }

      .no-image {
        color: #9ca3af;
        font-style: italic;
      }
    }
  }
}
</style>