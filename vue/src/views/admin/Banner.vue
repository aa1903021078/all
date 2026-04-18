<template>
  <div class="banner-management">
    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-input
          v-model="searchForm.title"
          placeholder="请输入标题搜索"
          class="search-input"
          clearable
          @clear="loadData"
          @keyup.enter="loadData"
      />
      <el-select
          v-model="searchForm.status"
          placeholder="状态"
          class="search-select"
          clearable
          @clear="loadData"
      >
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 操作按钮 -->
    <div class="operation-bar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增轮播图</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="70" align="center" />

      <el-table-column label="轮播图片" width="160" align="center">
        <template #default="{ row }">
          <div class="image-cell">
            <el-image
                v-if="row.imageUrl"
                :src="row.imageUrl"
                :preview-src-list="[row.imageUrl]"
                fit="cover"
                class="table-image"
                hide-on-click-modal
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
            </el-image>
            <div v-else class="image-placeholder" :style="{ background: row.bgColor || '#ccc' }">
              <span class="placeholder-icon">{{ row.icon || '🖼️' }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />

      <el-table-column prop="subtitle" label="副标题" min-width="180" show-overflow-tooltip />

      <el-table-column prop="sortOrder" label="排序" width="80" align="center" sortable />

      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        :title="dialogTitle"
        v-model="dialogVisible"
        width="700px"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="100px"
          class="banner-form"
      >
        <!-- 图片上传 -->
        <el-form-item label="轮播图片" prop="imageUrl">
          <div class="upload-wrapper">
            <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                accept="image/jpeg,image/png,image/gif"
            >
              <img
                  v-if="formData.imageUrl"
                  :src="formData.imageUrl"
                  class="uploaded-image"
              />
              <el-icon v-else class="uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tips">
              <div>点击上传图片，支持 JPG、PNG、GIF 格式</div>
              <div>建议尺寸：1920 × 400 像素，大小不超过 2MB</div>
              <el-button
                  v-if="formData.imageUrl"
                  link
                  type="danger"
                  size="small"
                  @click.stop="formData.imageUrl = ''"
              >
                删除图片
              </el-button>
            </div>
          </div>
        </el-form-item>

        <!-- 兜底背景色（无图片时显示） -->
        <el-form-item label="兜底背景色" v-if="!formData.imageUrl" prop="bgColor">
          <div class="bg-color-wrapper">
            <el-input
                v-model="formData.bgColor"
                placeholder="请输入CSS渐变色值"
                class="bg-color-input"
            >
              <template #append>
                <el-color-picker v-model="formData.bgColor" show-alpha size="small" />
              </template>
            </el-input>
          </div>
          <div class="form-hint">当不上传图片时显示此背景色（如：linear-gradient(135deg, #667eea 0%, #764ba2 100%)）</div>
        </el-form-item>

        <el-form-item label="图标" prop="icon" v-if="!formData.imageUrl">
          <div class="icon-wrapper">
            <el-input
                v-model="formData.icon"
                placeholder="请输入emoji图标"
                class="icon-input"
                maxlength="10"
            />
            <div class="icon-preview">{{ formData.icon || '无' }}</div>
          </div>
        </el-form-item>

        <el-form-item label="轮播标题" prop="title">
          <el-input
              v-model="formData.title"
              placeholder="请输入轮播标题"
              maxlength="100"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="副标题" prop="subtitle">
          <el-input
              v-model="formData.subtitle"
              type="textarea"
              :rows="2"
              placeholder="请输入副标题描述"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number
              v-model="formData.sortOrder"
              :min="0"
              :max="999"
              controls-position="right"
              style="width: 150px"
          />
          <span class="form-hint-inline">数字越小，排序越靠前</span>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">立即上架</el-radio>
            <el-radio :label="0">暂不发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 文件上传地址：POST /files/upload
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

// 搜索表单
const searchForm = reactive({
  title: '',
  status: null
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增轮播图')
const formRef = ref(null)
const submitLoading = ref(false)
const isEdit = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  title: '',
  subtitle: '',
  bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  icon: '🌍',
  imageUrl: '',      // 后端返回的完整URL：http://localhost:9090/files/download/xxx.jpg
  sortOrder: 0,
  status: 1
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入轮播标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

// 上传前校验
const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isGIF = file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG && !isGIF) {
    ElMessage.error('只支持 JPG、PNG、GIF 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传成功处理
const handleUploadSuccess = (res) => {
  if (res.code === '200' || res.code === 200) {
    // 后端返回完整的下载URL：http://localhost:9090/files/download/xxx.jpg
    formData.imageUrl = res.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

// 上传失败处理
const handleUploadError = (err) => {
  console.error('上传错误:', err)
  ElMessage.error('上传失败，请检查：1.后端是否启动 2.跨域是否配置 3.文件是否过大')
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchForm.title) {
      params.title = searchForm.title
    }
    if (searchForm.status !== null && searchForm.status !== undefined) {
      params.status = searchForm.status
    }

    const res = await request.get('/banner/selectPage', { params })
    if (res.code === '200' || res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载轮播图数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.status = null
  pageNum.value = 1
  loadData()
}

// 分页事件
const handleSizeChange = (val) => {
  pageSize.value = val
  pageNum.value = 1
  loadData()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadData()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增轮播图'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑轮播图'
  Object.assign(formData, {
    id: row.id,
    title: row.title,
    subtitle: row.subtitle,
    bgColor: row.bgColor || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    icon: row.icon || '🌍',
    imageUrl: row.imageUrl || '',
    sortOrder: row.sortOrder,
    status: row.status
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定要删除轮播图 "${row.title}" 吗？删除后不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      const res = await request.delete(`/banner/delete/${row.id}`)
      if (res.code === '200' || res.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 上架/下架切换
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '上架' : '下架'

  try {
    const res = await request.put(`/banner/toggleStatus/${row.id}?status=${newStatus}`)
    if (res.code === '200' || res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      row.status = newStatus
    } else {
      ElMessage.error(res.msg || `${actionText}失败`)
    }
  } catch (error) {
    ElMessage.error(`${actionText}失败`)
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    let res
    if (isEdit.value) {
      res = await request.put('/banner/update', formData)
    } else {
      res = await request.post('/banner/add', formData)
    }

    if (res.code === '200' || res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.msg || (isEdit.value ? '修改失败' : '新增失败'))
    }
  } catch (error) {
    ElMessage.error(isEdit.value ? '修改失败' : '新增失败')
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(formData, {
    id: null,
    title: '',
    subtitle: '',
    bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    icon: '🌍',
    imageUrl: '',
    sortOrder: 0,
    status: 1
  })
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.banner-management {
  padding: 20px;
  background-color: #ffffff;
  min-height: 100%;

  .search-bar {
    margin-bottom: 20px;
    padding: 18px;
    background-color: #f5f7fa;
    border-radius: 6px;
    display: flex;
    align-items: center;
    gap: 10px;

    .search-input {
      width: 220px;
    }

    .search-select {
      width: 120px;
    }
  }

  .operation-bar {
    margin-bottom: 20px;
  }

  .image-cell {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .table-image {
    width: 120px;
    height: 60px;
    border-radius: 4px;
    border: 1px solid #eee;
    cursor: pointer;
  }

  .image-error {
    width: 120px;
    height: 60px;
    border-radius: 4px;
    background-color: #f5f7fa;
    border: 1px solid #dcdfe6;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 12px;
    gap: 4px;
  }

  .image-placeholder {
    width: 120px;
    height: 60px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #eee;

    .placeholder-icon {
      font-size: 28px;
    }
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .banner-form {
    .upload-wrapper {
      display: flex;
      align-items: flex-start;
      gap: 15px;

      .avatar-uploader {
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        transition: border-color 0.3s;
        width: 300px;
        height: 150px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fbfdff;

        &:hover {
          border-color: #409eff;
        }

        .uploaded-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
        }

        .uploader-icon {
          font-size: 28px;
          color: #8c939d;
        }
      }

      .upload-tips {
        color: #606266;
        font-size: 12px;
        line-height: 1.8;
        padding-top: 10px;
      }
    }

    .bg-color-wrapper {
      width: 100%;
    }

    .bg-color-input {
      width: 100%;
    }

    .icon-wrapper {
      display: flex;
      align-items: center;
      gap: 15px;

      .icon-input {
        width: 200px;
      }

      .icon-preview {
        width: 50px;
        height: 32px;
        line-height: 32px;
        text-align: center;
        font-size: 24px;
        background-color: #f5f7fa;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
      }
    }

    .form-hint {
      color: #909399;
      font-size: 12px;
      margin-top: 6px;
      line-height: 1.4;
    }

    .form-hint-inline {
      color: #909399;
      font-size: 12px;
      margin-left: 10px;
    }
  }
}
</style>