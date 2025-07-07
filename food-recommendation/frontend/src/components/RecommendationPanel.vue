<template>
  <div class="recommendation-panel">
    <h2>个性化食物推荐</h2>
    
    <div class="filters">
      <!-- 健康目标 - 多选 -->
      <div class="filter-group">
        <label class="group-label">健康目标（可多选）：</label>
        <div class="checkbox-group">
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="lose-weight" 
              v-model="healthGoals"
            />
            减脂
          </label>
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="gain-muscle" 
              v-model="healthGoals"
            />
            增肌
          </label>
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="maintain" 
              v-model="healthGoals"
            />
            维持体重
          </label>
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="improve-immunity" 
              v-model="healthGoals"
            />
            增强免疫力
          </label>
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="improve-digestion" 
              v-model="healthGoals"
            />
            改善消化
          </label>
        </div>
        
        <!-- 自定义健康目标输入 -->
        <div class="custom-input">
          <input 
            type="text" 
            v-model="customHealthGoal" 
            placeholder="自定义健康目标（如：降血压、改善睡眠等）"
            @keyup.enter="addCustomHealthGoal"
          />
          <button @click="addCustomHealthGoal" v-if="customHealthGoal.trim()">添加</button>
        </div>
      </div>

      <!-- 饮食偏好 - 多选 -->
      <div class="filter-group">
        <label class="group-label">饮食偏好（可多选）：</label>
        <div class="checkbox-group">
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="vegetarian" 
              v-model="dietPreferences"
            />
            素食
          </label>
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="low-fat" 
              v-model="dietPreferences"
            />
            低脂
          </label>
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="low-sugar" 
              v-model="dietPreferences"
            />
            低糖
          </label>
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="high-protein" 
              v-model="dietPreferences"
            />
            高蛋白
          </label>
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="low-sodium" 
              v-model="dietPreferences"
            />
            低盐
          </label>
          <label class="checkbox-item">
            <input 
              type="checkbox" 
              value="gluten-free" 
              v-model="dietPreferences"
            />
            无麸质
          </label>
        </div>
        
        <!-- 自定义饮食偏好输入 -->
        <div class="custom-input">
          <input 
            type="text" 
            v-model="customDietPreference" 
            placeholder="自定义饮食偏好（如：生酮、地中海饮食等）"
            @keyup.enter="addCustomDietPreference"
          />
          <button @click="addCustomDietPreference" v-if="customDietPreference.trim()">添加</button>
        </div>
      </div>

      <!-- 过敏信息 -->
      <div class="filter-group">
        <label class="group-label">过敏信息：</label>
        <input 
          type="text" 
          v-model="allergies" 
          placeholder="请输入过敏食物，用逗号分隔（如：花生,海鲜,乳制品）"
          class="allergy-input"
        />
      </div>

      <!-- 已选择的标签显示 -->
      <div class="selected-tags" v-if="allSelectedTags.length > 0">
        <h4>已选择：</h4>
        <div class="tags">
          <span 
            v-for="tag in allSelectedTags" 
            :key="tag" 
            class="tag"
            @click="removeTag(tag)"
          >
            {{ tag }} ×
          </span>
        </div>
      </div>

      <!-- 获取推荐按钮 -->
      <button 
        @click="fetchRecommendations" 
        class="recommend-btn"
        :disabled="isLoading"
      >
        {{ isLoading ? '获取中...' : '获取推荐' }}
      </button>
    </div>
    
    <div v-if="isLoading" class="loading">
      <div class="spinner"></div>
      正在加载推荐...
    </div>
    
    <div v-if="error" class="error-message">
      {{ error }}
      <button @click="fetchRecommendations">重试</button>
    </div>
    
    <div v-if="!isLoading && !error" class="recommendations">
      <div v-if="emptyMessage" class="empty">
        {{ emptyMessage }}
      </div>
      <div v-else>
        <!-- 推荐结果 -->
        <div class="food-grid">
          <FoodCard 
            v-for="food in filteredFoods" 
            :key="food.id"
            :food="food"
          />
        </div>
        
        <!-- 调试面板 -->
        <DebugPanel :foods="filteredFoods" />
      </div>
    </div>
  </div>
</template>

<script>
import FoodCard from './FoodCard.vue'
import DebugPanel from './DebugPanel.vue'
import recommendationService from '../services/recommendationService'

export default {
  components: { FoodCard, DebugPanel },
  data() {
    return {
      // 多选健康目标
      healthGoals: [], // 开始时不默认选择
      customHealthGoal: '',
      
      // 多选饮食偏好
      dietPreferences: [], // 开始时不默认选择
      customDietPreference: '',
      
      // 过敏信息
      allergies: '',
      
      // 推荐结果
      foods: [],
      isLoading: false,
      error: null
    }
  },
  computed: {
    // 直接显示后端推荐的食物，不再进行前端筛选
    filteredFoods() {
      return this.foods;
    },
    
    // 简化提示信息
    emptyMessage() {
      if (this.isLoading) {
        return 'AI正在分析您的需求，请稍候...';
      }
      if (this.foods.length === 0) {
        return '请选择您的健康目标和饮食偏好，然后点击"获取AI推荐"按钮';
      }
      return '';
    },

    // 所有已选择的标签
    allSelectedTags() {
      const goals = this.healthGoals.map(goal => {
        const goalMap = {
          'lose-weight': '减脂',
          'gain-muscle': '增肌', 
          'maintain': '维持体重',
          'improve-immunity': '增强免疫力',
          'improve-digestion': '改善消化'
        };
        return goalMap[goal] || goal;
      });

      const prefs = this.dietPreferences.map(pref => {
        const prefMap = {
          'vegetarian': '素食',
          'low-fat': '低脂',
          'low-sugar': '低糖',
          'high-protein': '高蛋白',
          'low-sodium': '低盐',
          'gluten-free': '无麸质'
        };
        return prefMap[pref] || pref;
      });

      return [...goals, ...prefs];
    }
  },
  // 移除自动获取，改为手动点击
  methods: {
    // 添加自定义健康目标
    addCustomHealthGoal() {
      const goal = this.customHealthGoal.trim();
      if (goal && !this.healthGoals.includes(goal)) {
        this.healthGoals.push(goal);
        this.customHealthGoal = '';
        // 移除自动调用
      }
    },

    // 添加自定义饮食偏好
    addCustomDietPreference() {
      const pref = this.customDietPreference.trim();
      if (pref && !this.dietPreferences.includes(pref)) {
        this.dietPreferences.push(pref);
        this.customDietPreference = '';
        // 移除自动调用
      }
    },

    // 移除标签
    removeTag(tagText) {
      // 反向映射移除对应的选项
      const goalReverseMap = {
        '减脂': 'lose-weight',
        '增肌': 'gain-muscle',
        '维持体重': 'maintain',
        '增强免疫力': 'improve-immunity',
        '改善消化': 'improve-digestion'
      };

      const prefReverseMap = {
        '素食': 'vegetarian',
        '低脂': 'low-fat',
        '低糖': 'low-sugar',
        '高蛋白': 'high-protein',
        '低盐': 'low-sodium',
        '无麸质': 'gluten-free'
      };

      // 移除健康目标
      const goalKey = goalReverseMap[tagText];
      if (goalKey) {
        const index = this.healthGoals.indexOf(goalKey);
        if (index > -1) {
          this.healthGoals.splice(index, 1);
        }
      } else {
        // 移除自定义健康目标
        const customIndex = this.healthGoals.indexOf(tagText);
        if (customIndex > -1) {
          this.healthGoals.splice(customIndex, 1);
        }
      }

      // 移除饮食偏好
      const prefKey = prefReverseMap[tagText];
      if (prefKey) {
        const index = this.dietPreferences.indexOf(prefKey);
        if (index > -1) {
          this.dietPreferences.splice(index, 1);
        }
      } else {
        // 移除自定义饮食偏好
        const customIndex = this.dietPreferences.indexOf(tagText);
        if (customIndex > -1) {
          this.dietPreferences.splice(customIndex, 1);
        }
      }

      // 移除自动调用
    },

    async fetchRecommendations() {
      // 如果没有选择任何目标，不发送请求
      if (this.healthGoals.length === 0 && this.dietPreferences.length === 0) {
        this.foods = [];
        return;
      }

      this.isLoading = true;
      this.error = null;
      
      try {
        const userId = 'user-123';
        const requestFilters = {
          healthGoals: this.healthGoals, // 数组
          dietPreferences: this.dietPreferences, // 数组
          allergies: this.allergies.trim() || '无'
        };
        
        console.log('🚀 前端发送请求参数:', requestFilters);
        
        const response = await recommendationService.getRecommendations(userId, requestFilters);
        this.foods = response;
        
        console.log('🎯 前端接收到的推荐数据:', this.foods);
        console.log('📊 推荐食物数量:', this.foods.length);
        
        // 详细日志每个食物的信息
        this.foods.forEach((food, index) => {
          console.log(`${index + 1}. ${food.name} - 热量:${food.calories}kcal, 蛋白质:${food.protein}g, 图片:${food.image}`);
        });
        
      } catch (error) {
        this.error = '获取推荐失败，请稍后重试';
        console.error('❌ 前端获取推荐失败:', error);
      } finally {
        this.isLoading = false;
      }
    }
  },
  // 移除watch监听，避免自动触发
}
</script>

<style scoped>
.recommendation-panel {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.filters {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-group {
  margin-bottom: 20px;
}

.group-label {
  font-weight: bold;
  margin-bottom: 10px;
  display: block;
  color: #333;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 10px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  padding: 5px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  transition: all 0.2s;
}

.checkbox-item:hover {
  background: #e9ecef;
}

.checkbox-item input[type="checkbox"] {
  margin: 0;
}

.custom-input {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 10px;
}

.custom-input input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.custom-input button {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.custom-input button:hover {
  background: #0056b3;
}

.allergy-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.selected-tags {
  margin-top: 15px;
  padding: 15px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.selected-tags h4 {
  margin: 0 0 10px 0;
  color: #495057;
  font-size: 14px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: #007bff;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.tag:hover {
  background: #dc3545;
}

.recommend-btn {
  width: 100%;
  padding: 12px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 15px;
}

.recommend-btn:hover:not(:disabled) {
  background: #218838;
}

.recommend-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.recommendations {
  margin-top: 20px;
}

.food-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 40px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #42b883;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  padding: 20px;
  background: #ffebee;
  color: #c62828;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.error-message button {
  padding: 5px 10px;
  background: #c62828;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.empty {
  padding: 40px;
  text-align: center;
  color: #666;
}

@media (max-width: 768px) {
  .checkbox-group {
    flex-direction: column;
  }
  
  .custom-input {
    flex-direction: column;
    align-items: stretch;
  }
  
  .food-grid {
    grid-template-columns: 1fr;
  }
}
</style>
