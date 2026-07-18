<template>
  <div class="star-rating" :class="{ readonly }">
    <span
      v-for="i in max"
      :key="i"
      class="star"
      :style="{ fontSize: size + 'px' }"
      @mousemove="onHover(i)"
      @mouseleave="onLeave"
      @click="onClick(i)"
    >
      <span class="star-bg">★</span>
      <span class="star-fill" :style="{ width: fillWidth(i) }">★</span>
    </span>
    <span v-if="showScore" class="score">{{ displayScore }}</span>
    <span v-if="showCount && count != null" class="count">({{ count }}条)</span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: { type: [Number, String], default: 0 },
  max: { type: Number, default: 5 },
  size: { type: Number, default: 20 },
  readonly: { type: Boolean, default: false },
  showScore: { type: Boolean, default: false },
  showCount: { type: Boolean, default: false },
  count: { type: Number, default: null },
})
const emit = defineEmits(['update:modelValue', 'change'])

const hoverValue = ref(0)

const current = computed(() => Number(props.modelValue) || 0)
const displayScore = computed(() => current.value.toFixed(1))

function fillWidth(i) {
  const val = hoverValue.value || current.value
  if (val >= i) return '100%'
  if (val > i - 1) return (val - (i - 1)) * 100 + '%'
  return '0%'
}
function onHover(i) {
  if (props.readonly) return
  hoverValue.value = i
}
function onLeave() {
  if (props.readonly) return
  hoverValue.value = 0
}
function onClick(i) {
  if (props.readonly) return
  emit('update:modelValue', i)
  emit('change', i)
}
</script>

<style scoped>
.star-rating {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.star {
  position: relative;
  display: inline-block;
  line-height: 1;
  cursor: pointer;
  color: #dcdfe6;
}
.readonly .star {
  cursor: default;
}
.star-bg {
  color: #e4e7ed;
}
.star-fill {
  position: absolute;
  left: 0;
  top: 0;
  overflow: hidden;
  color: #ff9900;
  white-space: nowrap;
}
.score {
  margin-left: 6px;
  color: #ff9900;
  font-weight: 700;
}
.count {
  margin-left: 4px;
  color: #909399;
  font-size: 12px;
}
</style>
