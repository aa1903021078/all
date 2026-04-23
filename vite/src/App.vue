<script setup>
import IngredientRecommender from './components/IngredientRecommender.vue'
import CookingFlowchart from './components/CookingFlowchart.vue'
</script>

<template>
  <main class="page">
    <IngredientRecommender />
    <CookingFlowchart />
  </main>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 24px 16px;
}
</style>
