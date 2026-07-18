<template>
  <div class="image-upload">
    <div class="preview-list">
      <div v-for="(url, idx) in list" :key="idx" class="preview-item">
        <img :src="url" />
        <el-icon class="del" @click="remove(idx)"><CircleCloseFilled /></el-icon>
      </div>
      <el-upload
        v-if="list.length < limit"
        :show-file-list="false"
        :http-request="doUpload"
        accept="image/*"
        :disabled="uploading"
      >
        <div class="upload-btn">
          <el-icon v-if="!uploading"><Plus /></el-icon>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
          <div class="text-sm muted">{{ uploading ? '上传中' : '上传图片' }}</div>
        </div>
      </el-upload>
    </div>
    <div class="text-sm muted mt-8">图片将自动压缩为 WebP 格式,最多 {{ limit }} 张</div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fileApi } from '@/api'

const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  multiple: { type: Boolean, default: false },
  limit: { type: Number, default: 9 },
})
const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)

const list = computed(() => {
  if (props.multiple) {
    return Array.isArray(props.modelValue) ? props.modelValue : []
  }
  return props.modelValue ? [props.modelValue] : []
})

async function doUpload(option) {
  uploading.value = true
  try {
    const res = await fileApi.uploadImage(option.file)
    const url = res.data
    if (props.multiple) {
      const arr = Array.isArray(props.modelValue) ? [...props.modelValue] : []
      arr.push(url)
      emit('update:modelValue', arr)
    } else {
      emit('update:modelValue', url)
    }
    ElMessage.success('上传成功')
  } catch (e) {
    // 拦截器已提示
  } finally {
    uploading.value = false
  }
}

function remove(idx) {
  if (props.multiple) {
    const arr = [...props.modelValue]
    arr.splice(idx, 1)
    emit('update:modelValue', arr)
  } else {
    emit('update:modelValue', '')
  }
}
</script>

<style scoped>
.preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}
.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.del {
  position: absolute;
  right: 2px;
  top: 2px;
  color: #f56c6c;
  background: #fff;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
}
.upload-btn {
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  transition: border-color 0.2s;
}
.upload-btn:hover {
  border-color: var(--brand);
  color: var(--brand);
}
</style>
