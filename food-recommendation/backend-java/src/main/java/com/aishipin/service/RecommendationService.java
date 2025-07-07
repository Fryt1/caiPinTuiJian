package com.aishipin.service;

import com.aishipin.dto.FoodResponse;
import com.aishipin.dto.RecommendationRequest;
import com.aishipin.entity.Food;
import com.aishipin.entity.UserHealthInfo;
import com.aishipin.repository.FoodRepository;
import com.aishipin.repository.UserHealthInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    
    private final FoodRepository foodRepository;
    private final UserHealthInfoRepository userHealthInfoRepository;
    private final AiService aiService;
    
    /**
     * 获取食物推荐
     */
    public List<FoodResponse> getRecommendations(RecommendationRequest request) {
        String userId = request.getUserId();
        RecommendationRequest.RecommendationFilters filters = request.getFilters();
        
        log.info("开始处理推荐请求 - 用户ID: {}", userId);
        
        try {
            // 1. 提取数字用户ID
            Long numericUserId = extractNumericUserId(userId);
            
            // 2. 获取用户健康信息
            UserHealthInfo userHealth = getUserHealthInfo(numericUserId);
            
            // 3. 获取所有食物数据
            List<Food> allFoods = foodRepository.findAll();
            
            // 4. 准备AI输入数据 - 支持多选
            List<String> healthGoals = getHealthGoals(filters);
            List<String> dietPreferences = getDietPreferences(filters);
            String allergies = getAllergies(filters, userHealth);
            
            log.info("推荐参数 - 健康目标: {}, 饮食偏好: {}, 过敏信息: {}", 
                    healthGoals, dietPreferences, allergies);
            
            // 5. 调用AI服务获取推荐
            List<String> aiRecommendedNames = aiService.getRecommendations(
                    healthGoals, dietPreferences, allergies, allFoods, userId);
            
            log.info("AI推荐的食物名称: {}", aiRecommendedNames);
            
            // 6. 根据食物名称查找对应的完整信息
            List<Food> recommendedFoods = new ArrayList<>();
            if (!aiRecommendedNames.isEmpty()) {
                log.info("查找AI推荐的食物: {}", aiRecommendedNames);
                
                // 首先尝试精确匹配
                recommendedFoods = foodRepository.findByNameIn(aiRecommendedNames);
                
                List<String> foundNames = recommendedFoods.stream()
                        .map(Food::getName)
                        .collect(Collectors.toList());
                List<String> notFoundNames = aiRecommendedNames.stream()
                        .filter(name -> !foundNames.contains(name))
                        .collect(Collectors.toList());
                
                log.info("精确匹配找到: {}", foundNames);
                log.info("未找到的食物: {}", notFoundNames);
                
                // 对于未找到的食物，尝试模糊匹配
                if (!notFoundNames.isEmpty()) {
                    for (String searchName : notFoundNames) {
                        List<Food> fuzzyMatches = foodRepository.findByNameContaining(searchName);
                        if (!fuzzyMatches.isEmpty()) {
                            Food matched = fuzzyMatches.get(0);
                            recommendedFoods.add(matched);
                            log.info("模糊匹配: \"{}\" -> \"{}\"", searchName, matched.getName());
                        } else {
                            log.info("❌ 未找到匹配食物: \"{}\"", searchName);
                        }
                    }
                }
            }
            
            // 7. 如果AI推荐的食物不足，补充默认推荐
            if (recommendedFoods.size() < 8) {
                List<Long> defaultIds = getDefaultRecommendationsBalanced(filters, recommendedFoods);
                List<Long> existingIds = recommendedFoods.stream()
                        .map(Food::getId)
                        .collect(Collectors.toList());
                
                List<Long> supplementIds = defaultIds.stream()
                        .filter(id -> !existingIds.contains(id))
                        .limit(8 - recommendedFoods.size())
                        .collect(Collectors.toList());
                
                if (!supplementIds.isEmpty()) {
                    List<Food> supplementFoods = foodRepository.findByIdIn(supplementIds);
                    recommendedFoods.addAll(supplementFoods);
                }
            }
            
            // 8. 转换为响应格式
            List<FoodResponse> result = recommendedFoods.stream()
                    .map(this::formatFoodResponse)
                    .collect(Collectors.toList());
            
            // 为了确保真正的随机性，对最终结果也进行随机排序
            Random finalRandom = new Random(System.currentTimeMillis() + userId.hashCode());
            Collections.shuffle(result, finalRandom);
            
            // 输出最终推荐结果用于调试 - 与Node.js后端保持一致
            log.info("\n=== 最终推荐结果 ===");
            log.info("总共推荐食物数量: {}", result.size());
            log.info("AI成功推荐数量: {}", aiRecommendedNames.size());
            log.info("默认补充数量: {}", result.size() - aiRecommendedNames.size());
            log.info("推荐食物列表:");
            for (int i = 0; i < result.size(); i++) {
                FoodResponse food = result.get(i);
                String source = aiRecommendedNames.contains(food.getName()) ? "🤖AI推荐" : "🔧默认补充";
                log.info("{}. {} ({}kcal, 蛋白质{}g) - {}", 
                        i + 1, food.getName(), food.getCalories(), food.getProtein(), source);
            }
            log.info("=== 推荐结果结束 ===\n");
            
            return result;
            
        } catch (Exception e) {
            log.error("获取推荐失败", e);
            log.info("\n❌ AI响应解析失败: {}", e.getMessage());
            log.info("AI响应解析失败，使用默认推荐");
            
            // 如果AI解析失败，根据filters提供默认推荐
            List<Long> defaultIds = getDefaultRecommendationsBalanced(filters, new ArrayList<>());
            List<Food> defaultFoods = foodRepository.findByIdIn(defaultIds);
            return defaultFoods.stream()
                    .map(this::formatFoodResponse)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 提取数字用户ID
     */
    private Long extractNumericUserId(String userId) {
        if (userId == null) return null;
        
        try {
            // 如果是 user-123 格式，提取数字部分
            String numericPart = userId.replaceAll("\\D+", "");
            return numericPart.isEmpty() ? null : Long.parseLong(numericPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 获取用户健康信息
     */
    private UserHealthInfo getUserHealthInfo(Long numericUserId) {
        if (numericUserId == null || numericUserId.equals(123L)) {
            return null;
        }
        
        return userHealthInfoRepository.findByUserId(numericUserId).orElse(null);
    }
    
    /**
     * 获取健康目标列表
     */
    private List<String> getHealthGoals(RecommendationRequest.RecommendationFilters filters) {
        if (filters.getHealthGoals() != null && !filters.getHealthGoals().isEmpty()) {
            return filters.getHealthGoals();
        }
        
        if (filters.getHealthGoal() != null) {
            return Collections.singletonList(filters.getHealthGoal());
        }
        
        return Collections.singletonList("维持");
    }
    
    /**
     * 获取饮食偏好列表
     */
    private List<String> getDietPreferences(RecommendationRequest.RecommendationFilters filters) {
        if (filters.getDietPreferences() != null && !filters.getDietPreferences().isEmpty()) {
            return filters.getDietPreferences();
        }
        
        if (filters.getDietPreference() != null) {
            return Collections.singletonList(filters.getDietPreference());
        }
        
        return Collections.singletonList("均衡");
    }
    
    /**
     * 获取过敏信息
     */
    private String getAllergies(RecommendationRequest.RecommendationFilters filters, UserHealthInfo userHealth) {
        if (filters.getAllergies() != null) {
            return filters.getAllergies();
        }
        
        if (userHealth != null && userHealth.getAllergies() != null) {
            return userHealth.getAllergies();
        }
        
        return "无";
    }
    
    /**
     * 格式化食物响应
     */
    private FoodResponse formatFoodResponse(Food food) {
        // 生成标签
        List<String> tags = new ArrayList<>();
        tags.add(food.getCategory());
        
        // 根据营养成分添加标签
        if (food.getCalories() != null && food.getCalories().compareTo(BigDecimal.valueOf(100)) <= 0) {
            tags.add("low-calorie");
        }
        if (food.getFat() != null && food.getFat().compareTo(BigDecimal.valueOf(5)) <= 0) {
            tags.add("low-fat");
        }
        if (food.getCarbohydrate() != null && food.getCarbohydrate().compareTo(BigDecimal.valueOf(20)) <= 0) {
            tags.add("low-sugar");
        }
        if (food.getProtein() != null && food.getProtein().compareTo(BigDecimal.valueOf(10)) >= 0) {
            tags.add("high-protein");
        }
        
        // 判断是否为素食
        if (isVegetarianFood(food)) {
            tags.add("vegetarian");
        }
        
        // 构建图片URL
        String imageUrl = food.getImageUrl();
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = "http://localhost:3001" + imageUrl;
        }
        
        FoodResponse response = new FoodResponse();
        response.setId(food.getId());
        response.setName(food.getName());
        response.setCalories(food.getCalories());
        response.setProtein(food.getProtein());
        response.setCarbs(food.getCarbohydrate());
        response.setFat(food.getFat());
        response.setImage(imageUrl);
        response.setTags(tags);
        response.setDescription(food.getDescription());
        
        return response;
    }
    
    /**
     * 判断是否为素食
     */
    private boolean isVegetarianFood(Food food) {
        String category = food.getCategory();
        String name = food.getName();
        
        // 素食类别
        List<String> vegetarianCategories = Arrays.asList(
                "十字花科", "绿叶菜", "根茎类", "茄果类", "瓜类", "葱蒜类",
                "茎叶类", "菌菇类", "芽菜类", "野菜", "藻类", "叶菜类",
                "仁果类", "核果类", "浆果类", "热带水果", "水果类",
                "高蛋白", "嫩豆腐", "低脂", "豆制品", "发酵豆制品", "杂豆", "鲜豆类",
                "低GI主食", "粗粮", "全谷物", "杂粮", "有色谷物", "药食同源", "高原谷物", "全蛋白谷物"
        );
        
        return vegetarianCategories.contains(category) ||
               (category != null && (category.contains("素") || category.contains("蔬菜") || 
                                   category.contains("水果") || category.contains("豆"))) ||
               (name != null && (name.contains("豆") || name.contains("菜") || name.contains("果")));
    }
    
    /**
     * 获取默认推荐
     */
    private List<Long> getDefaultRecommendations(RecommendationRequest.RecommendationFilters filters) {
        Set<Long> recommendedIds = new HashSet<>();
        
        List<String> healthGoals = getHealthGoals(filters);
        List<String> dietPreferences = getDietPreferences(filters);
        
        // 根据健康目标添加推荐
        for (String goal : healthGoals) {
            switch (goal) {
                case "gain-muscle":
                case "增肌":
                    // 增肌：高蛋白食物
                    recommendedIds.addAll(Arrays.asList(201L, 202L, 203L, 204L, 205L, 21L, 22L, 24L, 25L, 4L, 16L));
                    break;
                case "lose-weight":
                case "减脂":
                    // 减脂：低热量食物
                    recommendedIds.addAll(Arrays.asList(32L, 33L, 34L, 35L, 36L, 52L, 53L, 54L, 22L, 23L));
                    break;
                case "improve-immunity":
                case "增强免疫力":
                    // 增强免疫：富含维生素食物
                    recommendedIds.addAll(Arrays.asList(52L, 53L, 54L, 55L, 56L, 32L, 33L, 36L, 37L));
                    break;
                case "improve-digestion":
                case "改善消化":
                    // 改善消化：高纤维和发酵食物
                    recommendedIds.addAll(Arrays.asList(4L, 16L, 32L, 33L, 40L, 41L, 28L, 29L));
                    break;
            }
        }
        
        // 根据饮食偏好添加推荐
        for (String pref : dietPreferences) {
            switch (pref) {
                case "vegetarian":
                case "素食":
                    // 素食：植物性食物
                    recommendedIds.addAll(Arrays.asList(21L, 22L, 23L, 32L, 33L, 34L, 52L, 53L, 1L, 2L, 4L, 16L));
                    break;
                case "low-fat":
                case "低脂":
                    // 低脂：脂肪含量低的食物
                    recommendedIds.addAll(Arrays.asList(203L, 204L, 32L, 33L, 34L, 52L, 53L, 22L, 23L, 2L, 16L));
                    break;
                case "low-sugar":
                case "低糖":
                    // 低糖：低碳水食物
                    recommendedIds.addAll(Arrays.asList(201L, 202L, 203L, 21L, 22L, 32L, 33L, 34L, 35L));
                    break;
                case "high-protein":
                case "高蛋白":
                    // 高蛋白：蛋白质含量高的食物
                    recommendedIds.addAll(Arrays.asList(201L, 202L, 203L, 204L, 21L, 22L, 24L, 25L, 4L));
                    break;
            }
        }
        
        // 如果没有选择任何条件，提供基础均衡推荐
        if (recommendedIds.isEmpty()) {
            recommendedIds.addAll(Arrays.asList(1L, 2L, 21L, 32L, 52L, 201L, 4L, 16L, 33L, 53L));
        }
        
        List<Long> result = new ArrayList<>(recommendedIds);
        
        // 创建一个基于当前时间的随机数生成器
        Random random = new Random(System.currentTimeMillis());
        
        // 如果推荐不足，添加一些基础营养食物，并随机化
        if (result.size() < 8) {
            List<Long> basicFoods = Arrays.asList(1L, 2L, 3L, 4L, 21L, 22L, 32L, 33L, 52L, 53L, 201L, 202L, 5L, 6L, 23L, 24L, 34L, 35L, 54L, 55L, 203L, 204L);
            Collections.shuffle(basicFoods, random); // 随机打乱基础食物
            for (Long food : basicFoods) {
                if (!result.contains(food) && result.size() < 12) {
                    result.add(food);
                }
            }
        }
        
        // 最后随机打乱整个结果列表
        Collections.shuffle(result, random);
        
        return result.stream().limit(15).collect(Collectors.toList());
    }
    
    /**
     * 默认推荐回退方案
     */
    private List<FoodResponse> getDefaultRecommendationsFallback(RecommendationRequest.RecommendationFilters filters) {
        try {
            List<Long> defaultIds = getDefaultRecommendations(filters);
            List<Food> defaultFoods = foodRepository.findByIdIn(defaultIds);
            return defaultFoods.stream()
                    .map(this::formatFoodResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取默认推荐失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取均衡的默认推荐（考虑已有推荐的类别分布）
     */
    private List<Long> getDefaultRecommendationsBalanced(RecommendationRequest.RecommendationFilters filters, List<Food> existingFoods) {
        List<String> healthGoals = getHealthGoals(filters);
        List<String> dietPreferences = getDietPreferences(filters);
        
        // 分析用户是否要求多种类别
        boolean hasMultipleCategories = checkMultipleFoodCategories(dietPreferences);
        
        if (hasMultipleCategories) {
            return getBalancedRecommendationsByCategory(dietPreferences, existingFoods);
        } else {
            return getDefaultRecommendations(filters);
        }
    }
    
    /**
     * 检测用户是否要求多种食物类别
     */
    private boolean checkMultipleFoodCategories(List<String> dietPreferences) {
        Set<String> categoryKeywords = new HashSet<>();
        
        // 定义关键词到类别的映射
        Map<String, String> keywordToCategory = new HashMap<>();
        keywordToCategory.put("水果", "水果类");
        keywordToCategory.put("果", "水果类");
        keywordToCategory.put("肉", "肉类");
        keywordToCategory.put("鱼", "海鲜类");
        keywordToCategory.put("海鲜", "海鲜类");
        keywordToCategory.put("谷", "谷类");
        keywordToCategory.put("米", "谷类");
        keywordToCategory.put("面", "谷类");
        keywordToCategory.put("蔬菜", "蔬菜类");
        keywordToCategory.put("菜", "蔬菜类");
        keywordToCategory.put("豆", "豆类");
        keywordToCategory.put("坚果", "坚果类");
        
        for (String pref : dietPreferences) {
            if (pref == null) continue;
            for (Map.Entry<String, String> entry : keywordToCategory.entrySet()) {
                if (pref.contains(entry.getKey())) {
                    categoryKeywords.add(entry.getValue());
                }
            }
        }
        
        return categoryKeywords.size() >= 2;
    }
    
    /**
     * 按类别均衡推荐食物
     */
    private List<Long> getBalancedRecommendationsByCategory(List<String> dietPreferences, List<Food> existingFoods) {
        Set<Long> recommendedIds = new HashSet<>();
        
        // 创建一个基于当前时间的随机数生成器，确保每次调用都有不同的随机顺序
        Random random = new Random(System.currentTimeMillis());
        
        // 分析已有食物的类别分布
        Map<String, Integer> existingCategoryCount = new HashMap<>();
        for (Food food : existingFoods) {
            String category = food.getCategory();
            existingCategoryCount.put(category, existingCategoryCount.getOrDefault(category, 0) + 1);
        }
        
        // 根据用户要求的类别，均衡补充食物
        // 水果类推荐 - 扩展ID范围确保更多选择
        if (containsKeyword(dietPreferences, Arrays.asList("水果", "果"))) {
            List<Long> fruitIds = Arrays.asList(52L, 53L, 54L, 55L, 56L, 57L, 58L, 59L, 60L, 61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L, 71L, 72L, 73L, 74L, 75L);
            Collections.shuffle(fruitIds, random); // 使用同一个随机种子
            int fruitCount = existingCategoryCount.getOrDefault("水果类", 0);
            recommendedIds.addAll(fruitIds.stream().limit(Math.max(0, 4 - fruitCount)).collect(Collectors.toList()));
        }
        
        // 肉类/海鲜推荐 - 扩展ID范围并确保真正的随机性
        if (containsKeyword(dietPreferences, Arrays.asList("肉", "鱼", "海鲜"))) {
            List<Long> meatIds = Arrays.asList(201L, 202L, 203L, 204L, 205L, 206L, 207L, 208L, 209L, 210L, 211L, 212L, 213L, 214L, 215L, 216L, 217L, 218L, 219L, 220L, 221L, 222L, 223L, 224L, 225L, 226L, 227L, 228L, 229L, 230L);
            Collections.shuffle(meatIds, random); // 使用同一个随机种子
            int meatCount = existingCategoryCount.getOrDefault("海鲜类", 0) + existingCategoryCount.getOrDefault("肉类", 0);
            recommendedIds.addAll(meatIds.stream().limit(Math.max(0, 4 - meatCount)).collect(Collectors.toList()));
        }
        
        // 谷类推荐 - 扩展ID范围
        if (containsKeyword(dietPreferences, Arrays.asList("谷", "米", "面"))) {
            List<Long> grainIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L);
            Collections.shuffle(grainIds, random); // 使用同一个随机种子
            int grainCount = existingCategoryCount.getOrDefault("谷类及制品", 0);
            recommendedIds.addAll(grainIds.stream().limit(Math.max(0, 4 - grainCount)).collect(Collectors.toList()));
        }
        
        // 蔬菜类推荐（如果有要求）
        if (containsKeyword(dietPreferences, Arrays.asList("蔬菜", "菜"))) {
            List<Long> vegetableIds = Arrays.asList(32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L, 51L, 72L, 73L, 74L, 75L, 76L, 77L, 78L, 79L, 80L);
            Collections.shuffle(vegetableIds, random); // 使用同一个随机种子
            int vegetableCount = existingCategoryCount.getOrDefault("蔬菜类", 0);
            recommendedIds.addAll(vegetableIds.stream().limit(Math.max(0, 4 - vegetableCount)).collect(Collectors.toList()));
        }
        
        // 豆类推荐（如果有要求）
        if (containsKeyword(dietPreferences, Arrays.asList("豆"))) {
            List<Long> beanIds = Arrays.asList(21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 81L, 82L, 83L, 84L, 85L);
            Collections.shuffle(beanIds, random); // 使用同一个随机种子
            int beanCount = existingCategoryCount.getOrDefault("豆类及制品", 0);
            recommendedIds.addAll(beanIds.stream().limit(Math.max(0, 3 - beanCount)).collect(Collectors.toList()));
        }
        
        List<Long> result = new ArrayList<>(recommendedIds);
        
        // 如果推荐不足，添加一些基础营养食物，也要随机化
        if (result.size() < 8) {
            List<Long> basicFoods = Arrays.asList(1L, 21L, 32L, 52L, 201L, 2L, 22L, 33L, 53L, 202L, 3L, 23L, 34L, 54L, 203L, 4L, 24L, 35L, 55L, 204L, 5L, 25L, 36L, 56L, 205L);
            Collections.shuffle(basicFoods, random); // 使用同一个随机种子
            for (Long food : basicFoods) {
                if (!result.contains(food) && result.size() < 15) {
                    result.add(food);
                }
            }
        }
        
        // 最后再次随机打乱结果，确保没有固定模式
        Collections.shuffle(result, random);
        
        return result;
    }
    
    /**
     * 检查饮食偏好中是否包含特定关键词
     */
    private boolean containsKeyword(List<String> dietPreferences, List<String> keywords) {
        for (String pref : dietPreferences) {
            if (pref == null) continue;
            for (String keyword : keywords) {
                if (pref.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }
}
