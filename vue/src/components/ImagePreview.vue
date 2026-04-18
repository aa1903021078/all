<template>
  <el-dialog
      v-model="visible"
      :show-close="false"
      :modal="true"
      :close-on-click-modal="false"
      class="image-preview-dialog"
      width="80%"
      top="5vh"
  >
    <div class="preview-container">
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-button-group>
          <el-button @click="handleZoomOut" title="缩小">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <el-button @click="handleZoomIn" title="放大">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
          <el-button @click="handleRotateLeft" title="向左旋转">
            <el-icon><RefreshLeft /></el-icon>
          </el-button>
          <el-button @click="handleRotateRight" title="向右旋转">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
          <el-button @click="handleReset" title="重置">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </el-button-group>

        <span class="image-index">{{ currentIndex + 1 }} / {{ urlList.length }}</span>

        <el-button @click="handleClose" circle>
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <!-- 图片区域 -->
      <div class="image-wrapper" @wheel.prevent="handleWheel">
        <div
            class="image-box"
            :style="imageStyle"
            @mousedown.prevent="handleMouseDown"
        >
          <img
              :src="currentUrl"
              :alt="`图片-${currentIndex + 1}`"
              draggable="false"
          />
        </div>

        <!-- 切换按钮 -->
        <div
            class="nav-arrow prev"
            v-show="urlList.length > 1"
            @click="handlePrev"
        >
          <el-icon><ArrowLeft /></el-icon>
        </div>
        <div
            class="nav-arrow next"
            v-show="urlList.length > 1"
            @click="handleNext"
        >
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 缩略图列表 -->
      <div class="thumbnail-list" v-if="urlList.length > 1">
        <div
            v-for="(url, index) in urlList"
            :key="index"
            class="thumbnail-item"
            :class="{ active: index === currentIndex }"
            @click="currentIndex = index"
        >
          <img :src="url" />
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  ZoomOut, ZoomIn, RefreshLeft, RefreshRight,
  Refresh, Close, ArrowLeft, ArrowRight
} from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  urlList: {
    type: Array,
    default: () => []
  },
  initialIndex: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:visible'])

const currentIndex = ref(props.initialIndex)
const scale = ref(1)
const rotate = ref(0)
const translateX = ref(0)
const translateY = ref(0)
const isDragging = ref(false)

// 当前显示的图片URL
const currentUrl = computed(() => {
  return props.urlList[currentIndex.value] || ''
})

// 图片样式
const imageStyle = computed(() => ({
  transform: `scale(${scale.value}) rotate(${rotate.value}deg) translate(${translateX.value}px, ${translateY.value}px)`,
  transition: isDragging.value ? 'none' : 'transform 0.3s ease',
  cursor: isDragging.value ? 'grabbing' : 'grab'
}))

// 监听初始索引变化
watch(() => props.initialIndex, (val) => {
  currentIndex.value = val
})

// 监听显示状态
watch(() => props.visible, (val) => {
  if (val) {
    resetTransform()
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleZoomIn = () => {
  scale.value = Math.min(scale.value + 0.2, 3)
}

const handleZoomOut = () => {
  scale.value = Math.max(scale.value - 0.2, 0.3)
}

const handleRotateLeft = () => {
  rotate.value -= 90
}

const handleRotateRight = () => {
  rotate.value += 90
}

const handleReset = () => {
  resetTransform()
}

const resetTransform = () => {
  scale.value = 1
  rotate.value = 0
  translateX.value = 0
  translateY.value = 0
}

const handleWheel = (e) => {
  if (e.deltaY < 0) {
    handleZoomIn()
  } else {
    handleZoomOut()
  }
}

const handleMouseDown = (e) => {
  if (scale.value <= 1) return

  isDragging.value = true
  const startX = e.clientX - translateX.value
  const startY = e.clientY - translateY.value

  const handleMouseMove = (e) => {
    translateX.value = e.clientX - startX
    translateY.value = e.clientY - startY
  }

  const handleMouseUp = () => {
    isDragging.value = false
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

const handlePrev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    resetTransform()
  }
}

const handleNext = () => {
  if (currentIndex.value < props.urlList.length - 1) {
    currentIndex.value++
    resetTransform()
  }
}

// 键盘事件
const handleKeydown = (e) => {
  if (!props.visible) return

  switch(e.key) {
    case 'Escape':
      handleClose()
      break
    case 'ArrowLeft':
      handlePrev()
      break
    case 'ArrowRight':
      handleNext()
      break
    case '+':
    case '=':
      handleZoomIn()
      break
    case '-':
      handleZoomOut()
      break
  }
}

// 监听键盘事件
if (typeof window !== 'undefined') {
  window.addEventListener('keydown', handleKeydown)
}
</script>

<style scoped lang="scss">
.image-preview-dialog {
  :deep(.el-dialog) {
    background: rgba(0, 0, 0, 0.9);
    border-radius: 0;

    &__header {
      display: none;
    }

    &__body {
      padding: 0;
      color: #fff;
    }
  }
}

.preview-container {
  display: flex;
  flex-direction: column;
  height: 85vh;

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: rgba(0, 0, 0, 0.5);

    .image-index {
      color: #fff;
      font-size: 14px;
    }

    .el-button {
      background: rgba(255, 255, 255, 0.1);
      border: none;
      color: #fff;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }

  .image-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;

    .image-box {
      max-width: 90%;
      max-height: 90%;

      img {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
        user-select: none;
      }
    }

    .nav-arrow {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      width: 50px;
      height: 50px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s;
      color: #fff;
      font-size: 24px;

      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }

      &.prev {
        left: 20px;
      }

      &.next {
        right: 20px;
      }
    }
  }

  .thumbnail-list {
    display: flex;
    gap: 10px;
    padding: 16px 24px;
    background: rgba(0, 0, 0, 0.5);
    overflow-x: auto;

    .thumbnail-item {
      width: 60px;
      height: 60px;
      border-radius: 4px;
      overflow: hidden;
      cursor: pointer;
      border: 2px solid transparent;
      opacity: 0.6;
      transition: all 0.3s;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      &:hover, &.active {
        opacity: 1;
        border-color: #fff;
      }
    }
  }
}
</style>