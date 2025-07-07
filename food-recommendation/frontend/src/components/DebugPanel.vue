<template>
  <div class="debug-panel">
    <h3>调试面板</h3>
    <div class="debug-section">
      <h4>当前推荐食物数据：</h4>
      <div v-if="foods.length === 0" class="no-data">
        暂无推荐数据
      </div>
      <div v-else>
        <p>共 {{ foods.length }} 个推荐食物</p>
        <div class="food-list">
          <div 
            v-for="(food, index) in foods" 
            :key="food.id" 
            class="food-item"
          >
            <div class="food-index">{{ index + 1 }}</div>
            <div class="food-details">
              <div><strong>名称：</strong>{{ food.name }}</div>
              <div><strong>ID：</strong>{{ food.id }}</div>
              <div><strong>热量：</strong>{{ food.calories }}kcal</div>
              <div><strong>蛋白质：</strong>{{ food.protein }}g</div>
              <div><strong>碳水：</strong>{{ food.carbs }}g</div>
              <div><strong>脂肪：</strong>{{ food.fat }}g</div>
              <div><strong>图片：</strong>{{ food.image }}</div>
              <div><strong>标签：</strong>{{ food.tags?.join(', ') || '无' }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="debug-section">
      <h4>测试连接：</h4>
      <button @click="testConnection" :disabled="testing">
        {{ testing ? '测试中...' : '测试后端连接' }}
      </button>
      <div v-if="connectionResult" class="connection-result">
        {{ connectionResult }}
      </div>
    </div>
  </div>
</template>

<script>
import recommendationService from '../services/recommendationService'

export default {
  props: {
    foods: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      testing: false,
      connectionResult: ''
    }
  },
  methods: {
    async testConnection() {
      this.testing = true;
      this.connectionResult = '';
      
      try {
        // 测试简单的推荐请求
        const testFilters = {
          healthGoals: ['maintain'],
          dietPreferences: ['high-protein'],
          allergies: '无'
        };
        
        const result = await recommendationService.getRecommendations('test-user', testFilters);
        this.connectionResult = `✅ 连接成功！获取到 ${result.length} 个推荐食物`;
      } catch (error) {
        this.connectionResult = `❌ 连接失败：${error.message}`;
      } finally {
        this.testing = false;
      }
    }
  }
}
</script>

<style scoped>
.debug-panel {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  font-family: monospace;
  font-size: 14px;
}

.debug-section {
  margin-bottom: 20px;
}

.debug-section h4 {
  margin: 0 0 10px 0;
  color: #495057;
}

.no-data {
  color: #6c757d;
  font-style: italic;
}

.food-list {
  max-height: 400px;
  overflow-y: auto;
}

.food-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  margin-bottom: 8px;
  background: white;
}

.food-index {
  background: #007bff;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.food-details {
  flex: 1;
}

.food-details > div {
  margin-bottom: 4px;
}

.connection-result {
  margin-top: 10px;
  padding: 8px;
  border-radius: 4px;
  background: #e9ecef;
}

button {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background: #218838;
}
</style>
