<template>
  <div class="points-manage">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 积分商品管理 -->
      <el-tab-pane label="积分商品" name="goods">
        <el-card>
          <template #header>
            <div class="header">
              <span>积分商品管理</span>
              <el-button type="primary" @click="handleAdd">+ 新增商品</el-button>
            </div>
          </template>

          <el-table :data="goodsList" v-loading="loading" stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
            <el-table-column label="商品图片" width="120" align="center">
              <template #default="{ row }">
                <el-image
                    :src="row.image"
                    style="width: 60px; height: 60px; border-radius: 4px;"
                    fit="cover"
                    :preview-src-list="[row.image]"
                    preview-teleported
                    hide-on-click-modal
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </template>
            </el-table-column>
            <el-table-column prop="pointsRequired" label="所需积分" width="100" align="center">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: bold;">{{ row.pointsRequired }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="80" align="center" />
            <el-table-column prop="totalExchanged" label="已兑换" width="80" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-switch
                    v-model="row.status"
                    :active-value="1"
                    :inactive-value="0"
                    active-text="上架"
                    inactive-text="下架"
                    inline-prompt
                    @change="(val) => handleStatusChange(row, val)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 兑换记录管理 -->
      <el-tab-pane label="兑换记录" name="records">
        <el-card>
          <template #header>
            <div class="header">
              <span>兑换记录管理</span>
              <el-radio-group v-model="recordStatus" size="small" @change="loadRecords">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button :label="0">待发放</el-radio-button>
                <el-radio-button :label="1">已发放</el-radio-button>
                <el-radio-button :label="2">已取消</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <el-table :data="exchangeRecords" v-loading="recordLoading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="goodsName" label="商品名称" min-width="150" />
            <el-table-column prop="userName" label="用户" width="120" />
            <el-table-column prop="pointsSpent" label="消耗积分" width="100" align="center">
              <template #default="{ row }">
                <span style="color: #f56c6c;">-{{ row.pointsSpent }}</span>
              </template>
            </el-table-column>
            <el-table-column label="收货信息" min-width="200">
              <template #default="{ row }">
                <div v-if="row.contactName" class="address-info">
                  <div>{{ row.contactName }} {{ row.contactPhone }}</div>
                  <div class="address-text">{{ row.address }}</div>
                </div>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" effect="light">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="兑换时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button
                    v-if="row.status === 0"
                    type="success"
                    link
                    @click="handleShip(row)"
                >
                  确认发放
                </el-button>
                <el-button
                    v-if="row.status === 0"
                    type="danger"
                    link
                    @click="handleCancel(row)"
                >
                  取消
                </el-button>
                <span v-if="row.status !== 0" style="color: #909399; font-size: 12px;">
                  已完成
                </span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
                v-model:current-page="pageNum"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadRecords"
                @current-change="loadRecords"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 商品新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑商品' : '新增商品'"
        width="600px"
        destroy-on-close
    >
      <el-form
          ref="formRef"
          :model="goodsForm"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="goodsForm.name" placeholder="请输入商品名称" />
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item label="商品图片" prop="image">
          <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept=".jpg,.jpeg,.png,.gif,.webp"
              name="file"
          >
            <img v-if="goodsForm.image" :src="goodsForm.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">
            支持 JPG、PNG、GIF、WEBP 格式，建议尺寸 800x800px，大小不超过 5MB
          </div>
          <div v-if="goodsForm.image" class="image-url">
            <el-link type="info" :href="goodsForm.image" target="_blank" :underline="false">
              {{ goodsForm.image }}
            </el-link>
          </div>
        </el-form-item>

        <el-form-item label="所需积分" prop="pointsRequired">
          <el-input-number
              v-model="goodsForm.pointsRequired"
              :min="1"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="库存数量" prop="stock">
          <el-input-number
              v-model="goodsForm.stock"
              :min="0"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="商品描述">
          <el-input
              v-model="goodsForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入商品描述"
          />
        </el-form-item>

        <el-form-item label="上架状态">
          <el-radio-group v-model="goodsForm.status">
            <el-radio :label="1">立即上架</el-radio>
            <el-radio :label="0">暂不上架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

const activeTab = ref('goods')
const loading = ref(false)
const recordLoading = ref(false)
const goodsList = ref([])
const exchangeRecords = ref([])

// 商品弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 分页相关
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const recordStatus = ref('')

// 图片上传配置
const uploadUrl = ref('http://localhost:9090/files/upload')
const uploadHeaders = ref({
  Authorization: 'Bearer ' + (localStorage.getItem('token') || '')
})

const goodsForm = ref({
  id: null,
  name: '',
  image: '',
  pointsRequired: 100,
  stock: 10,
  description: '',
  status: 1
})

const rules = {
  name: [{required: true, message: '请输入商品名称', trigger: 'blur'}],
  image: [{required: true, message: '请上传商品图片', trigger: 'change'}],
  pointsRequired: [{required: true, message: '请输入所需积分', trigger: 'blur'}],
  stock: [{required: true, message: '请输入库存数量', trigger: 'blur'}]
}

const getStatusType = (status) => {
  const types = {0: 'warning', 1: 'success', 2: 'info'}
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {0: '待发放', 1: '已发放', 2: '已取消'}
  return texts[status] || '未知'
}

// ==================== 商品管理（修复数据获取）====================

const loadGoods = async () => {
  loading.value = true
  try {
    const res = await request.get('/pointsGoods/selectAll')
    // 关键修复：使用 res.data 而不是 res
    goodsList.value = res?.data || []
  } catch (error) {
    console.error('加载商品失败', error)
    goodsList.value = []
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  goodsForm.value = {
    id: null,
    name: '',
    image: '',
    pointsRequired: 100,
    stock: 10,
    description: '',
    status: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  goodsForm.value = {...row}
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/pointsGoods/update', goodsForm.value)
      ElMessage.success('修改成功')
    } else {
      await request.post('/pointsGoods/add', goodsForm.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadGoods()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该商品吗？此操作不可恢复', '警告', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await request.delete(`/pointsGoods/delete/${row.id}`)
      ElMessage.success('删除成功')
      loadGoods()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
  })
}

const handleStatusChange = async (row, newStatus) => {
  try {
    await request.put('/pointsGoods/update', {
      id: row.id,
      status: newStatus
    })
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.status = newStatus === 1 ? 0 : 1
  }
}

// ==================== 图片上传处理 ====================

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('上传图片只能是 JPG、PNG、GIF、WEBP 格式!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  console.log('上传响应:', response)
  // 后端返回：{code: '200', data: 'http://...', msg: '...'}
  if (response.code === '200' || response.code === 200) {
    goodsForm.value.image = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请检查网络或联系管理员')
}

// ==================== 兑换记录管理（修复数据获取）====================

const loadRecords = async () => {
  recordLoading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (recordStatus.value !== '') {
      params.status = recordStatus.value
    }

    const res = await request.get('/pointsExchange/selectPage', {params})
    // 关键修复：使用 res.data.list 或 res.data
    exchangeRecords.value = res?.data?.list || res?.data || []
    total.value = res?.data?.total || 0
  } catch (error) {
    console.error('加载兑换记录失败', error)
    exchangeRecords.value = []
    total.value = 0
  } finally {
    recordLoading.value = false
  }
}

const handleShip = async (row) => {
  try {
    await ElMessageBox.confirm(`确认发放商品 "${row.goodsName}" 吗？`, '确认发放', {
      type: 'warning'
    })
    await request.put(`/pointsExchange/ship/${row.id}`)
    ElMessage.success('发放成功')
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发放失败')
    }
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该兑换吗？积分将退回给用户', '警告', {
      type: 'danger'
    })
    await request.post(`/pointsExchange/cancel/${row.id}`)
    ElMessage.success('取消成功，积分已退回')
    loadRecords()
    loadGoods()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

onMounted(() => {
  loadGoods()
  loadRecords()
})
</script>

<style scoped>
.points-manage {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 178px;
  height: 178px;
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.image-url {
  margin-top: 8px;
  font-size: 12px;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-error {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.address-info {
  font-size: 13px;
  line-height: 1.5;
}

.address-text {
  color: #909399;
  margin-top: 4px;
  font-size: 12px;
}

:deep(.el-table__cell) {
  .cell {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>