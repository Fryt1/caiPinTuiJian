<template>
  <div class="food-card">
    <img :src="food.image" :alt="food.name" class="food-image">
    <div class="food-info">
      <h3>{{ food.name }}</h3>
      <div class="nutrition">
        <span>热量: {{ food.calories }}kcal</span>
        <span>蛋白质: {{ food.protein }}g</span>
        <span>碳水: {{ food.carbs }}g</span>
      </div>
      <div class="tags">
        <span v-for="tag in food.tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    food: {
      type: Object,
      required: true
    }
  }
}
</script>

<style scoped>
.food-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}

.food-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.food-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.food-info {
  padding: 15px;
}

.nutrition {
  display: flex;
  gap: 10px;
  margin: 10px 0;
  font-size: 0.9em;
  color: #666;
}

.tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.tag {
  background: #f0f0f0;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.8em;
}
</style>
