<template>
  <div class="simple-map" ref="mapRef">
    <div class="map-grid"></div>
    <div class="map-hint">📍 美食地图 · 基础视图(共 {{ shops.length }} 家)</div>
    <div
      v-for="s in projected"
      :key="s.id"
      class="marker"
      :class="{ lit: s.lit, active: activeId === s.id }"
      :style="{ left: s.x + '%', top: s.y + '%' }"
      @click.stop="select(s)"
    >
      <div class="pin">{{ s.icon }}</div>
      <div class="label" v-if="activeId === s.id || showAllLabels">{{ s.name }}</div>
    </div>

    <div v-if="active" class="map-popup card">
      <img :src="active.cover" class="popup-cover" />
      <div class="popup-body">
        <div class="bold line-1">{{ active.name }}</div>
        <div class="muted text-sm">{{ active.categoryName }} · ¥{{ active.avgPrice }}/人</div>
        <div class="flex items-center gap-4 mt-4">
          <StarRating :model-value="Number(active.rating)" :size="14" readonly show-score />
        </div>
        <div class="muted text-sm line-1 mt-4">📍 {{ active.address }}</div>
        <div class="muted text-sm" v-if="active.businessHours">🕒 {{ active.businessHours }}</div>
        <div class="popup-actions mt-8">
          <el-button type="primary" size="small" @click="$emit('open', active)">详情</el-button>
          <el-button :type="active.lit ? 'danger' : 'default'" size="small" plain @click="$emit('checkin', active)">
            {{ active.lit ? '已点亮' : '🔥点亮' }}
          </el-button>
          <el-button size="small" @click="$emit('reserve', active)">预约</el-button>
          <el-button size="small" @click="$emit('consult', active)">咨询</el-button>
        </div>
      </div>
      <el-icon class="popup-close" @click="activeId = null"><Close /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import StarRating from './StarRating.vue'

const props = defineProps({
  shops: { type: Array, default: () => [] },
  showAllLabels: { type: Boolean, default: false },
})
defineEmits(['open', 'checkin', 'reserve', 'consult'])

const activeId = ref(null)
const mapRef = ref(null)

const iconMap = {
  川菜: '🌶️', 粤菜: '🦐', 火锅: '🍲', 日料: '🍣', 西餐: '🍝',
  烧烤: '🍢', 甜品: '🍰', 小吃: '🍜', 湘菜: '🌶', 咖啡: '☕',
}

const bounds = computed(() => {
  const list = props.shops.filter((s) => s.longitude != null && s.latitude != null)
  if (!list.length) return null
  let minLng = Infinity, maxLng = -Infinity, minLat = Infinity, maxLat = -Infinity
  list.forEach((s) => {
    const lng = Number(s.longitude), lat = Number(s.latitude)
    minLng = Math.min(minLng, lng); maxLng = Math.max(maxLng, lng)
    minLat = Math.min(minLat, lat); maxLat = Math.max(maxLat, lat)
  })
  return { minLng, maxLng, minLat, maxLat }
})

const projected = computed(() => {
  const b = bounds.value
  if (!b) return []
  const spanLng = b.maxLng - b.minLng || 1
  const spanLat = b.maxLat - b.minLat || 1
  return props.shops
    .filter((s) => s.longitude != null && s.latitude != null)
    .map((s) => {
      const lng = Number(s.longitude), lat = Number(s.latitude)
      // 10% ~ 90% 边距
      const x = 10 + ((lng - b.minLng) / spanLng) * 80
      const y = 10 + (1 - (lat - b.minLat) / spanLat) * 80
      return { ...s, x, y, icon: iconMap[s.categoryName] || '🍴' }
    })
})

const active = computed(() => projected.value.find((s) => s.id === activeId.value) || null)

function select(s) {
  activeId.value = activeId.value === s.id ? null : s.id
}
</script>

<style scoped>
.simple-map {
  position: relative;
  width: 100%;
  height: 520px;
  background: linear-gradient(135deg, #eaf3ec 0%, #e4eef5 100%);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: inset 0 0 0 1px #dfe6e9;
}
.map-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(120, 160, 140, 0.12) 1px, transparent 1px),
    linear-gradient(90deg, rgba(120, 160, 140, 0.12) 1px, transparent 1px),
    linear-gradient(45deg, transparent 48%, rgba(255, 255, 255, 0.6) 49% 51%, transparent 52%);
  background-size: 40px 40px, 40px 40px, 220px 220px;
}
.map-hint {
  position: absolute;
  left: 12px;
  top: 12px;
  background: rgba(255, 255, 255, 0.85);
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 12px;
  color: #555;
  z-index: 2;
}
.marker {
  position: absolute;
  transform: translate(-50%, -100%);
  z-index: 3;
  cursor: pointer;
  text-align: center;
}
.pin {
  font-size: 24px;
  filter: drop-shadow(0 3px 3px rgba(0, 0, 0, 0.25));
  transition: transform 0.15s;
}
.marker:hover .pin,
.marker.active .pin {
  transform: scale(1.3);
}
.marker.lit .pin::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  width: 30px;
  height: 30px;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: rgba(255, 106, 61, 0.35);
  animation: pulse 1.6s infinite;
  z-index: -1;
}
@keyframes pulse {
  0% { transform: translate(-50%, -50%) scale(0.6); opacity: 0.8; }
  100% { transform: translate(-50%, -50%) scale(1.8); opacity: 0; }
}
.label {
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 8px;
  white-space: nowrap;
  margin-top: 2px;
}
.map-popup {
  position: absolute;
  right: 16px;
  bottom: 16px;
  width: 300px;
  display: flex;
  z-index: 5;
}
.popup-cover {
  width: 88px;
  height: auto;
  object-fit: cover;
}
.popup-body {
  padding: 8px 10px;
  flex: 1;
  min-width: 0;
}
.popup-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.popup-close {
  position: absolute;
  right: 6px;
  top: 6px;
  cursor: pointer;
  color: #909399;
}
</style>
