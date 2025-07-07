const express = require('express');
const mysql = require('mysql2/promise');
const axios = require('axios');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');

const app = express();
app.use(cors());
app.use(bodyParser.json());

// 配置静态文件服务 - 提供图片访问
app.use('/images', express.static(path.join(__dirname, '../../images')));

// MySQL数据库配置
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '10086123',
  database: 'food_recommendation'
};

// 健康检查端点
app.get('/api/health', async (req, res) => {
  try {
    const connection = await mysql.createConnection(dbConfig);
    await connection.ping();
    await connection.end();
    res.json({ status: 'ok', database: 'connected' });
  } catch (error) {
    res.status(500).json({ status: 'error', database: 'disconnected' });
  }
});

// 推荐API
app.post('/api/recommendations', async (req, res) => {
  const { userId, filters } = req.body;
  let connection;

  try {
    connection = await mysql.createConnection(dbConfig);

    // 提取数字 ID（如果是 user-123 格式）
    const numericUserId = userId.toString().replace('user-', '');

    // 1. 获取用户健康信息（如果有的话）
    let userHealth = null;
    if (numericUserId && numericUserId !== '123') {
      const [userHealthData] = await connection.execute(
        `SELECT allergies, health_goal, diet_preference
         FROM user_health_info
         WHERE user_id = ?`,
        [numericUserId]
      );
      userHealth = userHealthData[0] || null;
    }

    // 2. 获取数据库中所有食物信息，供AI分析
    const [allFoods] = await connection.execute(
      `SELECT id, name, category, calories, protein, fat, carbohydrate, ingredients, description
       FROM foods`
    );

    // 3. 准备AI输入数据 - 支持多选
    const healthGoals = filters.healthGoals || [filters.healthGoal] || ['维持'];
    const dietPreferences = filters.dietPreferences || [filters.dietPreference] || ['均衡'];
    const allergies = filters.allergies || userHealth?.allergies || '无';

    const aiInputs = {
      health_goals: healthGoals,
      diet_preferences: dietPreferences, 
      allergies: allergies,
      foods_database: allFoods // 传递食物数据库供AI参考
    };

    // 4. 构建智能推荐提示词 - 支持多目标
    const buildPrompt = () => {
      const healthGoalsText = healthGoals.map(goal => {
        const goalMap = {
          'lose-weight': '减脂',
          'gain-muscle': '增肌',
          'maintain': '维持体重',
          'improve-immunity': '增强免疫力',
          'improve-digestion': '改善消化'
        };
        return goalMap[goal] || goal;
      }).join('、');

      const dietPreferencesText = dietPreferences.map(pref => {
        const prefMap = {
          'vegetarian': '素食',
          'low-fat': '低脂',
          'low-sugar': '低糖',
          'high-protein': '高蛋白',
          'low-sodium': '低盐',
          'gluten-free': '无麸质'
        };
        return prefMap[pref] || pref;
      }).join('、');

      let prompt = `作为专业营养师，请从提供的食物数据库中推荐12-18种最适合的食物。

用户需求：
- 健康目标：${healthGoalsText}
- 饮食偏好：${dietPreferencesText}
- 过敏信息：${allergies}

推荐原则：
`;

      // 根据健康目标添加具体指导
      if (healthGoals.includes('gain-muscle') || healthGoals.includes('增肌')) {
        prompt += `- 优先推荐高蛋白食物（蛋白质≥10g/100g）
- 包含优质蛋白源：鱼类、瘦肉、豆制品、蛋类
- 适量碳水化合物支持训练
- 富含支链氨基酸的食物
`;
      }
      
      if (healthGoals.includes('lose-weight') || healthGoals.includes('减脂')) {
        prompt += `- 优先推荐低热量高饱腹感食物（热量<150kcal/100g）
- 高纤维蔬菜和水果
- 优质蛋白维持肌肉量
- 避免高糖高脂食物
`;
      }

      if (healthGoals.includes('improve-immunity') || healthGoals.includes('增强免疫力')) {
        prompt += `- 富含维生素C、D、锌的食物
- 益生菌和益生元食物
- 抗氧化食物如浆果类
`;
      }

      if (healthGoals.includes('improve-digestion') || healthGoals.includes('改善消化')) {
        prompt += `- 高纤维食物促进肠道蠕动
- 发酵食品改善肠道菌群
- 易消化的食物
`;
      }

      // 根据饮食偏好添加约束
      if (dietPreferences.includes('low-fat') || dietPreferences.includes('低脂')) {
        prompt += `- 脂肪含量<5g/100g的食物为主
- 选择蒸煮烹饪方式的食物
`;
      }
      
      if (dietPreferences.includes('low-sugar') || dietPreferences.includes('低糖')) {
        prompt += `- 碳水化合物<20g/100g的食物为主
- 避免高GI值食物
- 选择复合碳水化合物
`;
      }
      
      if (dietPreferences.includes('vegetarian') || dietPreferences.includes('素食')) {
        prompt += `- 只推荐植物性食物
- 豆制品、坚果、蔬菜、水果、谷物
- 确保蛋白质互补
`;
      }

      if (dietPreferences.includes('high-protein') || dietPreferences.includes('高蛋白')) {
        prompt += `- 蛋白质含量≥15g/100g的食物
- 动植物蛋白搭配
`;
      }

      if (dietPreferences.includes('low-sodium') || dietPreferences.includes('低盐')) {
        prompt += `- 天然低钠食物
- 避免加工腌制食品
`;
      }

      if (dietPreferences.includes('gluten-free') || dietPreferences.includes('无麸质')) {
        prompt += `- 避免含麸质的谷物
- 推荐米类、玉米、藜麦等
`;
      }

      // 精确食物包含/排除处理
      const includeKeywords = ['只要', '仅要', '只吃', '专门要', '特别要'];
      const excludeKeywords = ['不要', '避免', '拒绝', '不吃', '禁止', '排除'];
      
      let specificInstructions = '';
      let hasSpecificRequirements = false;
      
      // 检查是否有具体的食物要求
      dietPreferences.forEach(pref => {
        if (!pref || typeof pref !== 'string') return;
        const prefLower = pref.toLowerCase();
        
        // 检查包含特定食物的指令
        includeKeywords.forEach(keyword => {
          if (pref.includes(keyword)) {
            hasSpecificRequirements = true;
            specificInstructions += `- 【重要】用户明确要求：${pref}，请严格按此要求推荐\n`;
          }
        });
        
        // 检查排除特定食物的指令
        excludeKeywords.forEach(keyword => {
          if (pref.includes(keyword)) {
            hasSpecificRequirements = true;
            specificInstructions += `- 【严格禁止】用户明确拒绝：${pref}，绝对不能推荐相关食物\n`;
          }
        });
        
        // 智能食物偏好解析 - 动态识别任何食物类型
        // 方案1：基于数据库食物类别的动态匹配
        const foodCategories = [
          '十字花科', '绿叶菜', '根茎类', '茄果类', '瓜类', '葱蒜类', '茎叶类', '菌菇类', 
          '芽菜类', '野菜', '藻类', '叶菜类', '仁果类', '核果类', '浆果类', '热带水果', 
          '水果类', '高蛋白', '嫩豆腐', '低脂', '豆制品', '发酵豆制品', '杂豆', '鲜豆类',
          '低GI主食', '粗粮', '全谷物', '杂粮', '有色谷物', '药食同源', '高原谷物', '全蛋白谷物',
          '海鲜', '淡水鱼', '深海鱼', '贝类', '禽肉', '畜肉', '内脏', '蛋类', '乳制品'
        ];
        
        // 方案2：通用关键词提取和智能匹配
        const extractFoodKeywords = (text) => {
          // 移除关键词后提取食物名称
          let foodText = text;
          [...includeKeywords, ...excludeKeywords].forEach(keyword => {
            foodText = foodText.replace(keyword, '');
          });
          return foodText.trim();
        };
        
        // 检查包含类指令 - 智能匹配
        includeKeywords.forEach(keyword => {
          if (pref.includes(keyword)) {
            const foodKeyword = extractFoodKeywords(pref);
            hasSpecificRequirements = true;
            
            // 检查是否匹配已知类别
            const matchedCategory = foodCategories.find(cat => 
              foodKeyword.includes(cat) || cat.includes(foodKeyword)
            );
            
            if (matchedCategory) {
              specificInstructions += `- 【重要】只推荐${matchedCategory}类食物，忽略其他所有推荐原则\n`;
            } else {
              // 通用处理：直接传递用户的具体要求给AI
              specificInstructions += `- 【重要】只推荐包含"${foodKeyword}"的食物，请在数据库中查找所有相关食物，忽略其他推荐原则\n`;
            }
          }
        });
        
        // 检查排除类指令 - 智能匹配  
        excludeKeywords.forEach(keyword => {
          if (pref.includes(keyword)) {
            const foodKeyword = extractFoodKeywords(pref);
            hasSpecificRequirements = true;
            
            // 检查是否匹配已知类别
            const matchedCategory = foodCategories.find(cat => 
              foodKeyword.includes(cat) || cat.includes(foodKeyword)
            );
            
            if (matchedCategory) {
              specificInstructions += `- 【严格禁止】绝对不能推荐${matchedCategory}类食物\n`;
            } else {
              // 通用处理：直接传递用户的具体要求给AI
              specificInstructions += `- 【严格禁止】绝对不能推荐任何包含"${foodKeyword}"的食物\n`;
            }
          }
        });
      });
      
      // 如果有具体要求，添加到提示词中
      if (hasSpecificRequirements) {
        prompt += `
【用户明确要求 - 最高优先级】：
${specificInstructions}
- 注意：上述要求优先级最高，如与其他推荐原则冲突，以用户明确要求为准
`;
      }

      prompt += `
注意事项：
- 如果用户有过敏信息，严格避免相关食物
- 营养搭配要均衡，包含多种食物类别
- 考虑食物的协同作用和营养互补

请只返回JSON格式，不要有其他文字。请推荐数据库中真实存在的食物：
{
  "recommendations": [
    {
      "name": "食物名称（必须与数据库中name字段完全一致）",
      "reason": "推荐理由（说明为什么适合用户的多重需求）"
    }
  ]
}

可选择的食物包括：${allFoods.map(f => f.name).join('、')}`;

      return prompt;
    };

    // 构建最终的AI提示词
    const finalPrompt = buildPrompt();
    
    // 输出最终提示词用于调试优化
    console.log('\n=== 最终输入AI的完整Prompt ===');
    console.log('用户ID:', userId);
    console.log('用户选择的filters:', JSON.stringify(filters, null, 2));
    console.log('\n--- AI输入数据 ---');
    console.log('健康目标:', healthGoals);
    console.log('饮食偏好:', dietPreferences);
    console.log('过敏信息:', allergies);
    console.log('\n--- 完整Prompt内容 ---');
    console.log(finalPrompt);
    console.log('\n=== Prompt结束 ===\n');

    // 调用AI服务获取推荐
    const aiResponse = await axios.post('http://localhost:8082/v1/chat-messages', {
      inputs: aiInputs,
      query: finalPrompt,
      response_mode: "blocking",
      user: userId.toString()
    }, {
      headers: {
        'Authorization': 'Bearer app-uAGYYnfpXdB5t1EdYdNP7wgP',
        'Content-Type': 'application/json'
      }
    });

    // 5. 解析AI响应
    let recommendedNames = [];
    try {
      const aiAnswer = aiResponse.data.answer;
      
      // 输出AI响应用于调试
      console.log('\n=== AI响应分析 ===');
      console.log('AI原始响应:', aiAnswer);
      
      // 尝试提取JSON部分
      const jsonMatch = aiAnswer.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        console.log('提取的JSON:', jsonMatch[0]);
        const recommendations = JSON.parse(jsonMatch[0]);
        recommendedNames = recommendations.recommendations?.map(r => r.name) || [];
        console.log('推荐的食物名称列表:', recommendedNames);
        console.log('推荐详情:', recommendations.recommendations);
      } else {
        console.log('❌ 未找到有效的JSON格式');
      }
      console.log('=== AI响应分析结束 ===\n');
    } catch (parseError) {
      console.log('\n❌ AI响应解析失败:', parseError.message);
      console.log('AI响应解析失败，使用默认推荐:', parseError);
      // 如果AI解析失败，根据filters提供默认推荐
      const defaultIds = getDefaultRecommendations(filters);
      const placeholders = defaultIds.map(() => '?').join(',');
      const [defaultFoods] = await connection.execute(
        `SELECT * FROM foods WHERE id IN (${placeholders})`,
        defaultIds
      );
      
      res.json(defaultFoods.map(food => formatFoodResponse(food)));
      return;
    }

    console.log('AI推荐的食物名称:', recommendedNames);

    // 6. 根据食物名称查找对应的完整信息
    let foods = [];
    if (recommendedNames.length > 0) {
      const namePlaceholders = recommendedNames.map(() => '?').join(',');
      const [aiRecommendedFoods] = await connection.execute(
        `SELECT * FROM foods WHERE name IN (${namePlaceholders})`,
        recommendedNames
      );
      foods = aiRecommendedFoods;
    }

    // 7. 如果AI推荐的食物不足，补充默认推荐
    if (foods.length < 8) {
      const defaultIds = getDefaultRecommendations(filters);
      const existingIds = foods.map(f => f.id);
      const supplementIds = defaultIds.filter(id => !existingIds.includes(id)).slice(0, 8 - foods.length);
      
      if (supplementIds.length > 0) {
        const placeholders = supplementIds.map(() => '?').join(',');
        const [supplementFoods] = await connection.execute(
          `SELECT * FROM foods WHERE id IN (${placeholders})`,
          supplementIds
        );
        foods = [...foods, ...supplementFoods];
      }
    }

    // 8. 记录推荐历史 (暂时注释掉以测试核心功能)
    /*
    if (foods.length > 0) {
      const foodIds = foods.map(f => f.id).join(',');
      // 使用数据库中已存在的用户ID (1 或 2)
      const existingUserId = parseInt(numericUserId) || 1;
      await connection.execute(
        `INSERT INTO recommendation_history
         (user_id, recommended_food_ids, filter_info)
         VALUES (?, ?, ?)`,
        [existingUserId, foodIds, JSON.stringify(filters)]
      );
    }
    */

    // 9. 返回格式化的食物信息
    const finalResult = foods.map(food => formatFoodResponse(food));
    
    // 输出最终推荐结果用于调试
    console.log('\n=== 最终推荐结果 ===');
    console.log('总共推荐食物数量:', finalResult.length);
    console.log('AI成功推荐数量:', recommendedNames.length);
    console.log('默认补充数量:', finalResult.length - recommendedNames.length);
    console.log('推荐食物列表:');
    finalResult.forEach((food, index) => {
      const source = recommendedNames.includes(food.name) ? '🤖AI推荐' : '🔧默认补充';
      console.log(`${index + 1}. ${food.name} (${food.calories}kcal, 蛋白质${food.protein}g) - ${source}`);
    });
    console.log('=== 推荐结果结束 ===\n');

    res.json(finalResult);

  } catch (error) {
    console.error('获取推荐失败:', error);
    res.status(500).json({ error: '获取推荐失败', details: error.message });
  } finally {
    if (connection) await connection.end();
  }
});

// 格式化食物响应的函数
function formatFoodResponse(food) {
  // 根据食物属性生成标签
  const tags = [food.category];
  
  // 根据营养成分添加标签
  if (food.calories <= 100) tags.push('low-calorie');
  if (food.fat <= 5) tags.push('low-fat');
  if (food.carbohydrate <= 20) tags.push('low-sugar');
  if (food.protein >= 10) tags.push('high-protein');
  
  // 扩展素食判断条件，包括所有植物性食物
  const vegetarianCategories = [
    '十字花科', '绿叶菜', '根茎类', '茄果类', '瓜类', '葱蒜类', 
    '茎叶类', '菌菇类', '芽菜类', '野菜', '藻类', '叶菜类',
    '仁果类', '核果类', '浆果类', '热带水果', '水果类',
    '高蛋白', '嫩豆腐', '低脂', '豆制品', '发酵豆制品', '杂豆', '鲜豆类',
    '低GI主食', '粗粮', '全谷物', '杂粮', '有色谷物', '药食同源', '高原谷物', '全蛋白谷物'
  ];
  
  if (vegetarianCategories.includes(food.category) || 
      food.category.includes('素') || 
      food.category.includes('蔬菜') || 
      food.category.includes('水果') ||
      food.category.includes('豆') ||
      food.name.includes('豆') ||
      food.name.includes('菜') ||
      food.name.includes('果')) {
    tags.push('vegetarian');
  }
  
  // 构建完整的图片URL
  let imageUrl = food.image_url;
  if (imageUrl && !imageUrl.startsWith('http')) {
    // 如果是相对路径，构建完整的服务器URL
    imageUrl = `http://localhost:3000${imageUrl}`;
  }
  
  return {
    id: food.id,
    name: food.name,
    calories: food.calories,
    protein: food.protein,
    carbs: food.carbohydrate,
    fat: food.fat,
    image: imageUrl,
    tags: tags,
    description: food.description
  };
}

// 默认推荐函数 - 支持多选条件
function getDefaultRecommendations(filters) {
  // 兼容旧格式和新格式
  const healthGoals = filters.healthGoals || [filters.healthGoal] || [];
  const dietPreferences = filters.dietPreferences || [filters.dietPreference] || [];
  
  let recommendedIds = new Set();
  
  // 根据健康目标添加推荐
  if (healthGoals.includes('gain-muscle') || healthGoals.includes('增肌')) {
    // 增肌：高蛋白食物
    const muscleFoods = [201, 202, 203, 204, 205, 21, 22, 24, 25, 4, 16]; // 海鲜、豆制品、谷物
    muscleFoods.forEach(id => recommendedIds.add(id));
  }
  
  if (healthGoals.includes('lose-weight') || healthGoals.includes('减脂')) {
    // 减脂：低热量食物
    const weightLossFoods = [32, 33, 34, 35, 36, 52, 53, 54, 22, 23]; // 蔬菜、水果、豆腐
    weightLossFoods.forEach(id => recommendedIds.add(id));
  }
  
  if (healthGoals.includes('improve-immunity') || healthGoals.includes('增强免疫力')) {
    // 增强免疫：富含维生素食物
    const immunityFoods = [52, 53, 54, 55, 56, 32, 33, 36, 37]; // 水果、蔬菜
    immunityFoods.forEach(id => recommendedIds.add(id));
  }
  
  if (healthGoals.includes('improve-digestion') || healthGoals.includes('改善消化')) {
    // 改善消化：高纤维和发酵食物
    const digestionFoods = [4, 16, 32, 33, 40, 41, 28, 29]; // 谷物、蔬菜、发酵食品
    digestionFoods.forEach(id => recommendedIds.add(id));
  }
  
  // 根据饮食偏好添加推荐
  if (dietPreferences.includes('vegetarian') || dietPreferences.includes('素食')) {
    // 素食：植物性食物
    const vegetarianFoods = [21, 22, 23, 32, 33, 34, 52, 53, 1, 2, 4, 16]; // 豆制品、蔬果、谷物
    vegetarianFoods.forEach(id => recommendedIds.add(id));
  }
  
  if (dietPreferences.includes('low-fat') || dietPreferences.includes('低脂')) {
    // 低脂：脂肪含量低的食物
    const lowFatFoods = [203, 204, 32, 33, 34, 52, 53, 22, 23, 2, 16]; // 白肉鱼、蔬果、谷物
    lowFatFoods.forEach(id => recommendedIds.add(id));
  }
  
  if (dietPreferences.includes('low-sugar') || dietPreferences.includes('低糖')) {
    // 低糖：低碳水食物
    const lowSugarFoods = [201, 202, 203, 21, 22, 32, 33, 34, 35]; // 蛋白质、绿叶蔬菜
    lowSugarFoods.forEach(id => recommendedIds.add(id));
  }
  
  if (dietPreferences.includes('high-protein') || dietPreferences.includes('高蛋白')) {
    // 高蛋白：蛋白质含量高的食物
    const highProteinFoods = [201, 202, 203, 204, 21, 22, 24, 25, 4]; // 海鲜、豆制品
    highProteinFoods.forEach(id => recommendedIds.add(id));
  }
  
  // 如果没有选择任何条件，提供基础均衡推荐
  if (recommendedIds.size === 0) {
    const balancedFoods = [1, 2, 21, 32, 52, 201, 4, 16, 33, 53];
    balancedFoods.forEach(id => recommendedIds.add(id));
  }
  
  // 转换为数组并确保有足够的推荐
  let result = Array.from(recommendedIds);
  
  // 如果推荐不足，添加一些基础营养食物
  if (result.length < 8) {
    const basicFoods = [1, 2, 3, 4, 21, 22, 32, 33, 52, 53, 201, 202];
    for (let food of basicFoods) {
      if (!result.includes(food) && result.length < 12) {
        result.push(food);
      }
    }
  }
  
  return result.slice(0, 15); // 最多返回15个推荐
}

// 启动服务器
const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server running on port ${PORT}`);
});
