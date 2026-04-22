<script setup>
import { ref, computed } from 'vue'

// 菜品数据库（含鱼类菜品，用于验证推荐）
const dishes = [
  { name: '红烧肉', ingredients: ['五花肉', '葱', '姜', '蒜', '生抽', '老抽', '八角'] },
  { name: '清蒸鲈鱼', ingredients: ['鲈鱼', '葱', '姜', '蒸鱼豉油'] },
  { name: '糖醋鱼', ingredients: ['草鱼', '糖', '醋', '番茄酱', '姜'] },
  { name: '酸菜鱼', ingredients: ['黑鱼', '酸菜', '泡椒', '蒜', '花椒'] },
  { name: '水煮鱼', ingredients: ['草鱼', '豆芽', '花椒', '辣椒', '蒜'] },
  { name: '番茄炒蛋', ingredients: ['番茄', '鸡蛋', '葱', '糖'] },
  { name: '青椒土豆丝', ingredients: ['土豆', '青椒', '蒜', '醋'] },
  { name: '宫保鸡丁', ingredients: ['鸡胸肉', '花生', '干辣椒', '葱', '蒜'] },
  { name: '麻婆豆腐', ingredients: ['豆腐', '牛肉末', '豆瓣酱', '花椒', '蒜'] }
]

const input = ref('鱼')

/**
 * 智能匹配：用户输入的关键词只要是菜品名或任一食材的子串即视为匹配。
 * 之前的 bug：使用 `ingredients.includes(keyword)` 做完全相等匹配，
 * 导致用户输入"鱼"时无法匹配到食材为"鲈鱼"、"草鱼"、"黑鱼"的菜品。
 * 这里改为 indexOf/includes 做子串匹配，并兼容多关键词（以空格或逗号分隔）。
 */
const keywords = computed(() =>
  input.value
    .split(/[\s,，、]+/)
    .map((k) => k.trim())
    .filter(Boolean)
)

const recommended = computed(() => {
  if (keywords.value.length === 0) return dishes
  return dishes.filter((dish) =>
    keywords.value.every((kw) =>
      dish.name.includes(kw) ||
      dish.ingredients.some((ing) => ing.includes(kw))
    )
  )
})
</script>

<template>
  <section class="card">
    <h2 class="card-title">🍳 食材智能推荐</h2>
    <div class="search-row">
      <input
        v-model="input"
        type="text"
        class="search-input"
        placeholder="输入食材，如：鱼、土豆、鸡蛋"
      />
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
            :class="{ matched: keywords.some((k) => ing.includes(k)) }"
          >{{ ing }}</span>
        </div>
      </li>
    </ul>
    <p v-else class="empty">没有匹配的菜品，换个食材试试～</p>
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
  margin: 0 0 14px;
  font-size: 18px;
  color: #333;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
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
.dish-item:last-child {
  border-bottom: none;
}
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
