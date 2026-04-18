<template>
  <div class="file-upload">
    <el-upload
        :action="uploadUrl"
        :headers="headers"
        :before-upload="beforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :on-remove="handleRemove"
        :on-preview="handlePreview"
        :file-list="fileList"
        :limit="limit"
        :multiple="multiple"
        :accept="accept"
        :disabled="disabled"
        list-type="picture-card"
        :class="{ 'hide-upload': fileList.length >= limit }"
    >
      <el-icon><Plus /></el-icon>
      <template #tip>
        <div class="upload-tip" v-if="tip">
          {{ tip }}
        </div>
      </template>
    </el-upload>

    <!-- 图片预览弹窗 -->
    <ImagePreview
        v-model:visible="previewVisible"
        :url-list="previewUrls"
        :initial-index="previewIndex"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ImagePreview from './ImagePreview.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  limit: {
    type: Number,
    default: 3
  },
  multiple: {
    type: Boolean,
    default: true
  },
  accept: {
    type: String,
    default: 'image/*'
  },
  maxSize: {
    type: Number,
    default: 5 // MB
  },
  tip: {
    type: String,
    default: '支持 JPG、PNG 格式，单张不超过 5MB'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'error'])

const uploadUrl = import.meta.env.VITE_API_BASE_URL + '/files/upload'
const headers = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('token')
}))

const fileList = ref([])
const previewVisible = ref(false)
const previewIndex = ref(0)

// 计算预览URL列表
const previewUrls = computed(() => {
  return fileList.value.map(file => file.url || file.response)
})

// 初始化回显
watch(() => props.modelValue, (val) => {
  if (val && fileList.value.length === 0) {
    const urls = val.split(',').filter(Boolean)
    fileList.value = urls.map((url, index) => ({
      name: `image-${index}`,
      url: url,
      response: url,
      uid: Date.now() + index
    }))
  }
}, { immediate: true })

// 上传前校验
const beforeUpload = (file) => {
  // 文件类型校验
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }

  // 文件大小校验
  const isLtSize = file.size / 1024 / 1024 < props.maxSize
  if (!isLtSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB!`)
    return false
  }

  return true
}

// 上传成功
const handleSuccess = (response, file) => {
  file.url = response
  updateValue()
  emit('success', response, file)
  ElMessage.success('上传成功')
}

// 上传失败
const handleError = (error, file) => {
  console.error('上传失败:', error)
  emit('error', error, file)
  ElMessage.error('上传失败，请重试')
}

// 删除文件
const handleRemove = (file, fileList) => {
  fileList.value = fileList
  updateValue()
}

// 预览图片
const handlePreview = (file) => {
  const index = fileList.value.findIndex(item => item.uid === file.uid)
  previewIndex.value = index >= 0 ? index : 0
  previewVisible.value = true
}

// 更新v-model
const updateValue = () => {
  const urls = fileList.value
      .filter(file => file.response || file.url)
      .map(file => file.response || file.url)
      .join(',')
  emit('update:modelValue', urls)
}
</script>

<style scoped lang="scss">
.file-upload {
  :deep(.el-upload--picture-card) {
    width: 100px;
    height: 100px;
    line-height: 100px;
    border-radius: 8px;
    border: 1px dashed #d9d9d9;
    background: #fafafa;
    transition: all 0.3s;

    &:hover {
      border-color: #52c41a;
      color: #52c41a;
    }
  }

  :deep(.el-upload-list__item) {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      .el-upload-list__item-status-label {
        display: none;
      }
    }
  }

  .hide-upload {
    :deep(.el-upload--picture-card) {
      display: none;
    }
  }

  .upload-tip {
    margin-top: 8px;
    color: #8c8c8c;
    font-size: 12px;
    line-height: 1.5;
  }
}
</style>