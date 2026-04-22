<script setup>
import { ref, computed } from 'vue'
import { dishes, allIngredients } from './dishes.js'

// 用户输入（实时编辑，但不直接触发推荐）
const input = ref('')
// 已提交用于推荐的关键词（仅在点击"推荐"按钮后更新）
const submittedKeywords = ref([])
// 是否已点击过推荐（控制初始展示文案）
const hasSearched = ref(false)

// 常用快捷食材（可点击一键推荐）——从数据库里挑选一组代表性食材
const quickPicks = ['鱼', '虾', '鸡蛋', '牛肉', '鸡肉', '猪肉', '豆腐', '土豆', '茄子', '番茄', '青椒', '香菇', '米饭', '面条']

function parseKeywords(text) {
  return text
    .split(/[\s,，、]+/)
    .map((k) => k.trim())
    .filter(Boolean)
}

/**
 * 点击"推荐"按钮触发：
 * 1. 按分隔符拆分用户输入为多个关键词
 * 2. 对每个关键词，与菜品名 / 任一食材做子串匹配（只要数据库中存在即可命中）
 * 3. 多关键词之间取"与"关系（每个关键词都要匹配到，才算命中该菜品）
 */
function recommend(textOverride) {
  // 若提供了 textOverride（如点击快捷食材按钮），则同步更新输入框，便于用户看到当前推荐所用的关键词
  const text = textOverride ?? input.value
  if (textOverride !== undefined) input.value = text
  submittedKeywords.value = parseKeywords(text)
  hasSearched.value = true
}

function clearSearch() {
  input.value = ''
  submittedKeywords.value = []
  hasSearched.value = false
}

// 当前输入的关键词是否在数据库里存在（任一食材子串包含即算存在）
const knownStatus = computed(() =>
  parseKeywords(input.value).map((kw) => ({
    kw,
    exists: allIngredients.some((ing) => ing.includes(kw))
  }))
)

const recommended = computed(() => {
  if (submittedKeywords.value.length === 0) return []
  return dishes.filter((dish) =>
    submittedKeywords.value.every((kw) =>
      dish.name.includes(kw) ||
      dish.ingredients.some((ing) => ing.includes(kw))
    )
  )
})

function onEnter() {
  recommend()
}
</script>

<template>
  <section class="card">
    <h2 class="card-title">🍳 食材智能推荐</h2>

    <div class="search-row">
      <input
        v-model="input"
        type="text"
        class="search-input"
        placeholder="输入食材，如：鱼、牛肉、番茄、香菇…（多个食材用空格或逗号分隔）"
        @keyup.enter="onEnter"
      />
      <button type="button" class="btn btn-primary" @click="recommend()">推荐</button>
      <button type="button" class="btn btn-plain" @click="clearSearch">清空</button>
    </div>

    <!-- 快捷点选：点击即触发推荐 -->
    <div class="quick-row">
      <span class="quick-label">快捷食材：</span>
      <button
        v-for="ing in quickPicks"
        :key="ing"
        type="button"
        class="quick-tag"
        @click="recommend(ing)"
      >{{ ing }}</button>
    </div>

    <!-- 输入中的食材是否存在于数据库 -->
    <div v-if="knownStatus.length" class="known-row">
      <span
        v-for="item in knownStatus"
        :key="item.kw"
        class="known-tag"
        :class="{ yes: item.exists, no: !item.exists }"
      >
        {{ item.kw }}
        <small>{{ item.exists ? '数据库中有' : '暂无此食材' }}</small>
      </span>
    </div>

    <div v-if="hasSearched" class="result-head">
      <span>
        关键词：
        <strong v-for="kw in submittedKeywords" :key="kw" class="kw">{{ kw }}</strong>
        <em v-if="!submittedKeywords.length">（空）</em>
      </span>
      <span class="hint">共 {{ recommended.length }} 道推荐菜品</span>
    </div>

    <ul v-if="recommended.length" class="dish-list">
      <li v-for="dish in recommended" :key="dish.name" class="dish-item">
        <div class="dish-name">{{ dish.name }}</div>
        <div class="dish-ings">
          <span
            v-for="ing in dish.ingredients"
            :key="ing"
            class="tag"
            :class="{ matched: submittedKeywords.some((k) => ing.includes(k)) }"
          >{{ ing }}</span>
        </div>
      </li>
    </ul>
    <p v-else-if="hasSearched" class="empty">没有匹配的菜品，换个食材试试～</p>
    <p v-else class="empty hint">输入食材或点击上方快捷食材，再点"推荐"即可查看菜品</p>
  </section>
</template>

<style scoped>
.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  max-width: 560px;
  margin: 16px auto;
}
.card-title {
  margin: 0 0 14px;
  font-size: 18px;
  color: #333;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.search-input:focus {
  border-color: #409eff;
}
.btn {
  flex: 0 0 auto;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.15s;
}
.btn:hover { opacity: 0.88; }
.btn-primary { background: #409eff; color: #fff; }
.btn-plain { background: #f4f4f5; color: #606266; }

.quick-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}
.quick-label {
  font-size: 12px;
  color: #999;
  margin-right: 4px;
}
.quick-tag {
  font-size: 12px;
  padding: 3px 10px;
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.15s;
}
.quick-tag:hover { background: #e1f3d8; }

.known-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.known-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.known-tag small { font-size: 11px; opacity: 0.85; }
.known-tag.yes { background: #ecf5ff; color: #409eff; }
.known-tag.no { background: #fef0f0; color: #f56c6c; }

.result-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
  margin: 10px 0;
}
.kw {
  color: #409eff;
  margin: 0 2px;
}
.hint {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}
.dish-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.dish-item {
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}
.dish-item:last-child { border-bottom: none; }
.dish-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}
.dish-ings {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag {
  font-size: 12px;
  padding: 2px 8px;
  background: #f4f4f5;
  color: #666;
  border-radius: 10px;
}
.tag.matched {
  background: #ecf5ff;
  color: #409eff;
}
.empty {
  text-align: center;
  color: #999;
  padding: 16px 0;
}
</style>
