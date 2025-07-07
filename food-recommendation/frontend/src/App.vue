<script setup>
import RecommendationPanel from './components/RecommendationPanel.vue'
</script>

<template>
  <div class="app">
    <h1>个性化食物推荐平台</h1>
    <RecommendationPanel />
  </div>
</template>

<style scoped>
.app {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 2rem;
}

@media (max-width: 768px) {
  .app {
    padding: 1rem;
  }
}
</style>
