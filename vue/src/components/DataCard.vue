<template>
  <div class="data-card" :class="[type, { 'hoverable': hoverable }]">
    <div class="card-icon" :style="{ background: iconBg }">
      <el-icon :size="iconSize" :color="iconColor">
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="card-content">
      <div class="label">{{ label }}</div>
      <div class="value" :style="{ fontSize: valueSize }">
        <span v-if="prefix" class="prefix">{{ prefix }}</span>
        {{ formattedValue }}
        <span v-if="suffix" class="suffix">{{ suffix }}</span>
      </div>
      <div class="trend" v-if="trend !== undefined">
        <el-icon :size="14" :class="trend >= 0 ? 'up' : 'down'">
          <CaretTop v-if="trend >= 0" />
          <CaretBottom v-else />
        </el-icon>
        <span :class="trend >= 0 ? 'up' : 'down'">{{ Math.abs(trend) }}%</span>
        <span class="trend-label">较{{ trendLabel }}</span>
      </div>
    </div>
    <div class="card-footer" v-if="$slots.footer">
      <slot name="footer" />
    </div>
    <div class="card-action" v-if="actionText" @click="handleAction">
      {{ actionText }}
      <el-icon><ArrowRight /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { CaretTop, CaretBottom, ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  icon: {
    type: [String, Object],
    required: true
  },
  label: {
    type: String,
    default: ''
  },
  value: {
    type: [Number, String],
    default: 0
  },
  type: {
    type: String,
    default: 'primary', // primary, success, warning, danger, info
    validator: (val) => ['primary', 'success', 'warning', 'danger', 'info'].includes(val)
  },
  prefix: {
    type: String,
    default: ''
  },
  suffix: {
    type: String,
    default: ''
  },
  decimals: {
    type: Number,
    default: 0
  },
  trend: {
    type: Number,
    default: undefined
  },
  trendLabel: {
    type: String,
    default: '上期'
  },
  iconSize: {
    type: Number,
    default: 32
  },
  valueSize: {
    type: String,
    default: '28px'
  },
  hoverable: {
    type: Boolean,
    default: true
  },
  actionText: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['action'])

// 颜色映射
const colorMap = {
  primary: { bg: '#e6f7ff', color: '#1890ff', main: '#1890ff' },
  success: { bg: '#f6ffed', color: '#52c41a', main: '#52c41a' },
  warning: { bg: '#fffbe6', color: '#faad14', main: '#faad14' },
  danger: { bg: '#fff1f0', color: '#f5222d', main: '#f5222d' },
  info: { bg: '#f4f4f5', color: '#909399', main: '#909399' }
}

const iconBg = computed(() => colorMap[props.type].bg)
const iconColor = computed(() => colorMap[props.type].color)

// 格式化数值
const formattedValue = computed(() => {
  if (typeof props.value === 'string') return props.value

  if (props.decimals > 0) {
    return props.value.toFixed(props.decimals)
  }

  // 大数值格式化
  if (props.value >= 10000) {
    return (props.value / 10000).toFixed(1) + '万'
  }

  return props.value.toLocaleString()
})

const handleAction = () => {
  emit('action')
}
</script>

<style scoped lang="scss">
.data-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &.hoverable:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }

  .card-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .card-content {
    flex: 1;
    min-width: 0;

    .label {
      color: #8c8c8c;
      font-size: 14px;
      margin-bottom: 8px;
    }

    .value {
      color: #262626;
      font-weight: 600;
      line-height: 1.2;
      margin-bottom: 8px;

      .prefix, .suffix {
        font-size: 0.6em;
        color: #8c8c8c;
        margin: 0 4px;
      }
    }

    .trend {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;

      .up {
        color: #52c41a;
      }

      .down {
        color: #f5222d;
      }

      .trend-label {
        color: #bfbfbf;
        margin-left: 4px;
      }
    }
  }

  .card-footer {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
    width: 100%;
  }

  .card-action {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px 24px;
    background: rgba(0, 0, 0, 0.02);
    color: #595959;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: rgba(0, 0, 0, 0.04);
      color: #1890ff;
    }
  }

  // 不同类型左边框
  &.primary { border-left: 4px solid #1890ff; }
  &.success { border-left: 4px solid #52c41a; }
  &.warning { border-left: 4px solid #faad14; }
  &.danger { border-left: 4px solid #f5222d; }
  &.info { border-left: 4px solid #909399; }
}
</style>