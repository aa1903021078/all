<template>
  <div class="image-upload">
    <div class="preview-list">
      <div
        v-for="(url, idx) in list"
        :key="idx"
        class="preview-item"
        :class="{ dragging: dragIndex === idx, sortable: multiple }"
        :draggable="multiple"
        @dragstart="onDragStart(idx)"
        @dragover.prevent
        @drop="onDrop(idx)"
        @dragend="dragIndex = -1"
      >
        <img :src="url" />
        <el-icon class="del" @click="remove(idx)"><CircleCloseFilled /></el-icon>
        <span v-if="multiple" class="order-badge">{{ idx + 1 }}</span>
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
    <div class="text-sm muted mt-8">
      图片将自动压缩优化,最多 {{ limit }} 张<template v-if="multiple && list.length > 1">,可拖拽调整顺序</template>
    </div>
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
const dragIndex = ref(-1)

function onDragStart(idx) {
  if (props.multiple) dragIndex.value = idx
}
function onDrop(idx) {
  if (!props.multiple || dragIndex.value === -1 || dragIndex.value === idx) {
    dragIndex.value = -1
    return
  }
  const arr = Array.isArray(props.modelValue) ? [...props.modelValue] : []
  const moved = arr.splice(dragIndex.value, 1)[0]
  arr.splice(idx, 0, moved)
  emit('update:modelValue', arr)
  dragIndex.value = -1
}

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
.preview-item.sortable {
  cursor: move;
}
.preview-item.dragging {
  opacity: 0.4;
}
.order-badge {
  position: absolute;
  left: 2px;
  bottom: 2px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 11px;
  border-radius: 9px;
  padding: 0 4px;
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
