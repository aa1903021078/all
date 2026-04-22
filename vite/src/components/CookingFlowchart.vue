<script setup>
import { ref } from 'vue'

// 步骤颜色与截图保持一致
const palette = ['#67c23a', '#409eff', '#e6a23c', '#f56c6c', '#909399', '#67c23a']

const steps = ref([
  { text: '五花肉洗净切成3厘米见方的块' },
  { text: '冷水下锅，加料酒、姜片焯水去腥' },
  { text: '锅中放少许油，放入冰糖小火炒糖色' },
  { text: '放入五花肉翻炒上色' },
  { text: '加入葱姜蒜、八角、香叶等香料' },
  { text: '加入生抽、老抽、料酒、清水炖煮 40 分钟' }
])
</script>

<template>
  <section class="card">
    <h2 class="card-title">🥢 做饭流程图</h2>

    <!-- 去掉了“原始顺序 / 自定义顺序”切换与“重置”按钮，固定使用原始顺序 -->

    <ol class="steps">
      <li v-for="(step, idx) in steps" :key="idx" class="step">
        <div class="badge" :style="{ background: palette[idx % palette.length] }">
          {{ idx + 1 }}
        </div>
        <div class="step-text">{{ step.text }}</div>
      </li>
    </ol>
  </section>
</template>

<style scoped>
.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  max-width: 520px;
  margin: 16px auto;
}
.card-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #333;
}
.steps {
  list-style: none;
  padding: 0;
  margin: 0;
  position: relative;
}
/* 时间线竖线 */
.steps::before {
  content: '';
  position: absolute;
  left: 19px;
  top: 20px;
  bottom: 20px;
  width: 2px;
  background: #ebeef5;
  z-index: 0;
}
.step {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
  position: relative;
  z-index: 1;
}
/*
 * 步骤按钮（数字圆圈）的稳定悬停：
 * - 使用固定 width/height + box-sizing: border-box，避免 hover 改变盒子尺寸。
 * - 不在 hover 时修改 font-size / margin / border-width / padding，否则整行会“跳”。
 * - 仅用 transform:scale 做视觉缩放，不影响布局。
 * - 使用 will-change + translateZ(0) 稳定合成层，避免子像素抖动。
 */
.badge {
  flex: 0 0 auto;
  width: 40px;
  height: 40px;
  box-sizing: border-box;
  border-radius: 50%;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  transform: translateZ(0);
  will-change: transform, box-shadow;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.badge:hover {
  transform: scale(1.08) translateZ(0);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.step-text {
  flex: 1;
  color: #333;
  font-size: 14px;
  line-height: 1.5;
}
</style>
