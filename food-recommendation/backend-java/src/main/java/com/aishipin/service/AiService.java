package com.aishipin.service;

import com.aishipin.dto.AiRequest;
import com.aishipin.dto.AiResponse;
import com.aishipin.dto.AiRecommendation;
import com.aishipin.entity.Food;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {
    
    @Value("${ai.service.url}")
    private String aiServiceUrl;
    
    @Value("${ai.service.token}")
    private String aiServiceToken;
    
    private final ObjectMapper objectMapper;
    
    /**
     * 调用AI服务获取食物推荐
     */
    public List<String> getRecommendations(List<String> healthGoals, 
                                         List<String> dietPreferences, 
                                         String allergies, 
                                         List<Food> allFoods,
                                         String userId) {
        try {
            // 构建AI输入数据
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("health_goals", healthGoals);
            inputs.put("diet_preferences", dietPreferences);
            inputs.put("allergies", allergies);
            inputs.put("foods_database", allFoods);
            
            // 构建提示词
            String prompt = buildPrompt(healthGoals, dietPreferences, allergies, allFoods, userId);
            
            // 输出最终提示词用于调试优化 - 与Node.js后端保持一致
            log.info("\n=== 最终输入AI的完整Prompt ===");
            log.info("用户ID: {}", userId);
            Map<String, Object> filtersDebug = new HashMap<>();
            filtersDebug.put("healthGoals", healthGoals);
            filtersDebug.put("dietPreferences", dietPreferences);
            filtersDebug.put("allergies", allergies);
            log.info("用户选择的filters: {}", filtersDebug);
            log.info("\n--- AI输入数据 ---");
            log.info("健康目标: {}", healthGoals);
            log.info("饮食偏好: {}", dietPreferences);
            log.info("过敏信息: {}", allergies);
            log.info("\n--- 完整Prompt内容 ---");
            log.info(prompt);
            log.info("\n=== Prompt结束 ===\n");
            
            // 创建AI请求
            AiRequest aiRequest = new AiRequest();
            aiRequest.setInputs(inputs);
            aiRequest.setQuery(prompt);
            aiRequest.setUser(userId);
            
            // 发送HTTP请求
            String responseBody = sendHttpRequest(aiRequest);
            
            // 解析响应
            List<String> recommendedNames = parseAiResponse(responseBody);
            
            log.info("AI推荐结果: {}", recommendedNames);
            return recommendedNames;
            
        } catch (Exception e) {
            log.error("AI推荐失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 构建AI提示词 - 与Node.js后端保持一致
     */
    private String buildPrompt(List<String> healthGoals, 
                              List<String> dietPreferences, 
                              String allergies, 
                              List<Food> allFoods,
                              String userId) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为专业营养师，请从提供的食物数据库中推荐12-18种最适合的食物。\n\n");
        
        // 用户需求
        prompt.append("用户需求：\n");
        prompt.append("- 健康目标：").append(formatHealthGoals(healthGoals)).append("\n");
        prompt.append("- 饮食偏好：").append(formatDietPreferences(dietPreferences)).append("\n");
        prompt.append("- 过敏信息：").append(allergies != null ? allergies : "无").append("\n\n");
        
        // 推荐原则
        prompt.append("推荐原则：\n");
        
        // 根据健康目标添加具体指导
        for (String goal : healthGoals) {
            switch (goal) {
                case "gain-muscle":
                case "增肌":
                    prompt.append("- 优先推荐高蛋白食物（蛋白质≥10g/100g）\n")
                          .append("- 包含优质蛋白源：鱼类、瘦肉、豆制品、蛋类\n")
                          .append("- 适量碳水化合物支持训练\n")
                          .append("- 富含支链氨基酸的食物\n");
                    break;
                case "lose-weight":
                case "减脂":
                    prompt.append("- 优先推荐低热量高饱腹感食物（热量<150kcal/100g）\n")
                          .append("- 高纤维蔬菜和水果\n")
                          .append("- 优质蛋白维持肌肉量\n")
                          .append("- 避免高糖高脂食物\n");
                    break;
                case "improve-immunity":
                case "增强免疫力":
                    prompt.append("- 富含维生素C、D、锌的食物\n")
                          .append("- 益生菌和益生元食物\n")
                          .append("- 抗氧化食物如浆果类\n");
                    break;
                case "improve-digestion":
                case "改善消化":
                    prompt.append("- 高纤维食物促进肠道蠕动\n")
                          .append("- 发酵食品改善肠道菌群\n")
                          .append("- 易消化的食物\n");
                    break;
            }
        }
        
        // 根据饮食偏好添加约束
        for (String pref : dietPreferences) {
            switch (pref) {
                case "low-fat":
                case "低脂":
                    prompt.append("- 脂肪含量<5g/100g的食物为主\n")
                          .append("- 选择蒸煮烹饪方式的食物\n");
                    break;
                case "low-sugar":
                case "低糖":
                    prompt.append("- 碳水化合物<20g/100g的食物为主\n")
                          .append("- 避免高GI值食物\n")
                          .append("- 选择复合碳水化合物\n");
                    break;
                case "vegetarian":
                case "素食":
                    prompt.append("- 只推荐植物性食物\n")
                          .append("- 豆制品、坚果、蔬菜、水果、谷物\n")
                          .append("- 确保蛋白质互补\n");
                    break;
                case "high-protein":
                case "高蛋白":
                    prompt.append("- 蛋白质含量≥15g/100g的食物\n")
                          .append("- 动植物蛋白搭配\n");
                    break;
                case "low-sodium":
                case "低盐":
                    prompt.append("- 天然低钠食物\n")
                          .append("- 避免加工腌制食品\n");
                    break;
                case "gluten-free":
                case "无麸质":
                    prompt.append("- 避免含麸质的谷物\n")
                          .append("- 推荐米类、玉米、藜麦等\n");
                    break;
            }
        }
        
        // 精确食物包含/排除处理 - 与Node.js后端保持一致
        List<String> includeKeywords = Arrays.asList("只要", "仅要", "只吃", "专门要", "特别要");
        List<String> excludeKeywords = Arrays.asList("不要", "避免", "拒绝", "不吃", "禁止", "排除");
        
        StringBuilder specificInstructions = new StringBuilder();
        boolean hasSpecificRequirements = false;
        
        // 食物类别定义
        List<String> foodCategories = Arrays.asList(
                "十字花科", "绿叶菜", "根茎类", "茄果类", "瓜类", "葱蒜类", "茎叶类", "菌菇类", 
                "芽菜类", "野菜", "藻类", "叶菜类", "仁果类", "核果类", "浆果类", "热带水果", 
                "水果类", "高蛋白", "嫩豆腐", "低脂", "豆制品", "发酵豆制品", "杂豆", "鲜豆类",
                "低GI主食", "粗粮", "全谷物", "杂粮", "有色谷物", "药食同源", "高原谷物", "全蛋白谷物",
                "海鲜", "淡水鱼", "深海鱼", "贝类", "禽肉", "畜肉", "内脏", "蛋类", "乳制品"
        );
        
        // 检查饮食偏好中的具体食物要求
        for (String pref : dietPreferences) {
            if (pref == null || pref.trim().isEmpty()) continue;
            
            // 首先检查简单的包含/排除指令
            for (String keyword : includeKeywords) {
                if (pref.contains(keyword)) {
                    hasSpecificRequirements = true;
                    specificInstructions.append("- 【重要】用户明确要求：").append(pref)
                            .append("，请严格按此要求推荐\n");
                    break;
                }
            }
            
            for (String keyword : excludeKeywords) {
                if (pref.contains(keyword)) {
                    hasSpecificRequirements = true;
                    specificInstructions.append("- 【严格禁止】用户明确拒绝：").append(pref)
                            .append("，绝对不能推荐相关食物\n");
                    break;
                }
            }
            
            // 智能食物偏好解析 - 动态识别任何食物类型
            // 检查包含类指令 - 智能匹配
            for (String keyword : includeKeywords) {
                if (pref.contains(keyword)) {
                    String foodKeyword = extractFoodKeywords(pref, includeKeywords, excludeKeywords);
                    hasSpecificRequirements = true;
                    
                    // 检查是否匹配已知类别
                    String matchedCategory = foodCategories.stream()
                            .filter(cat -> foodKeyword.contains(cat) || cat.contains(foodKeyword))
                            .findFirst()
                            .orElse(null);
                    
                    if (matchedCategory != null) {
                        specificInstructions.append("- 【重要】只推荐").append(matchedCategory)
                                .append("类食物，忽略其他所有推荐原则\n");
                    } else {
                        specificInstructions.append("- 【重要】只推荐包含\"").append(foodKeyword)
                                .append("\"的食物，请在数据库中查找所有相关食物，忽略其他推荐原则\n");
                    }
                    break;
                }
            }
            
            // 检查排除类指令 - 智能匹配
            for (String keyword : excludeKeywords) {
                if (pref.contains(keyword)) {
                    String foodKeyword = extractFoodKeywords(pref, includeKeywords, excludeKeywords);
                    hasSpecificRequirements = true;
                    
                    // 检查是否匹配已知类别
                    String matchedCategory = foodCategories.stream()
                            .filter(cat -> foodKeyword.contains(cat) || cat.contains(foodKeyword))
                            .findFirst()
                            .orElse(null);
                    
                    if (matchedCategory != null) {
                        specificInstructions.append("- 【严格禁止】绝对不能推荐").append(matchedCategory)
                                .append("类食物\n");
                    } else {
                        specificInstructions.append("- 【严格禁止】绝对不能推荐任何包含\"").append(foodKeyword)
                                .append("\"的食物\n");
                    }
                    break;
                }
            }
        }
        
        // 如果有具体要求，添加到提示词中
        if (hasSpecificRequirements) {
            prompt.append("\n【用户明确要求 - 最高优先级】：\n")
                  .append(specificInstructions.toString())
                  .append("- 注意：上述要求优先级最高，如与其他推荐原则冲突，以用户明确要求为准\n");
        }
        
        // 注意事项和输出格式
        prompt.append("\n注意事项：\n")
              .append("- 如果用户有过敏信息，严格避免相关食物\n")
              .append("- 营养搭配要均衡，包含多种食物类别\n")
              .append("- 考虑食物的协同作用和营养互补\n")
              .append("- **重要**：推荐的食物名称必须与数据库中的name字段完全一致\n");
        
        // 如果用户要求多种类别，添加均衡分配指导
        boolean hasMultipleCategories = checkMultipleFoodCategories(dietPreferences);
        if (hasMultipleCategories) {
            prompt.append("- **关键要求**：用户要求多种食物类别，请均衡分配推荐数量，每个类别推荐3-6种食物\n")
                  .append("- **随机化要求**：在每个类别内随机选择食物，不要总是按照数据库顺序推荐\n")
                  .append("- **多样性要求**：确保每个用户要求的类别都有充分的代表性\n")
                  .append("- **避免重复模式**：不要每次都推荐相同的食物组合，要有变化和新鲜感\n");
        } else {
            prompt.append("- **随机化要求**：请随机选择推荐的食物，避免每次都推荐相同的食物\n")
                  .append("- **多样性要求**：在符合用户需求的前提下，尽量推荐不同的食物组合\n");
        }
        
        prompt.append("\n请只返回JSON格式，不要有其他文字。请推荐数据库中真实存在的食物：\n")
              .append("{\n")
              .append("  \"recommendations\": [\n")
              .append("    {\n")
              .append("      \"name\": \"食物名称（必须与数据库中name字段完全一致）\",\n")
              .append("      \"reason\": \"推荐理由（说明为什么适合用户的多重需求）\"\n")
              .append("    }\n")
              .append("  ]\n")
              .append("}\n\n")
              .append("数据库中可选择的食物包括：\n");
        
        // 添加所有可选食物 - 使用引号包围，与Node.js后端保持一致
        // 为了增加随机性，将食物按类别分组后再随机排列
        List<String> foodNames = new ArrayList<>();
        
        // 创建一个带时间种子的随机数生成器，确保每次调用都有不同的随机顺序
        Random random = new Random(System.currentTimeMillis() + userId.hashCode());
        
        if (hasMultipleCategories) {
            // 按类别分组并随机化
            Map<String, List<Food>> foodsByCategory = groupFoodsByCategory(allFoods);
            
            // 对类别本身也进行随机排序
            List<Map.Entry<String, List<Food>>> categoryEntries = new ArrayList<>(foodsByCategory.entrySet());
            Collections.shuffle(categoryEntries, random);
            
            for (Map.Entry<String, List<Food>> entry : categoryEntries) {
                List<Food> categoryFoods = new ArrayList<>(entry.getValue());
                Collections.shuffle(categoryFoods, random); // 使用同一个随机种子
                for (Food food : categoryFoods) {
                    foodNames.add("\"" + food.getName() + "\"");
                }
            }
        } else {
            // 如果没有特殊要求，使用强随机化所有食物
            List<Food> shuffledFoods = new ArrayList<>(allFoods);
            Collections.shuffle(shuffledFoods, random);
            for (Food food : shuffledFoods) {
                foodNames.add("\"" + food.getName() + "\"");
            }
        }
        
        // 为了进一步增加随机性，再次打乱整个食物名称列表
        Collections.shuffle(foodNames, random);
        
        prompt.append(String.join("、", foodNames));
        
        prompt.append("\n\n请确保推荐的每个食物名称都在上述列表中，名称必须完全匹配，包括标点符号。");
        
        return prompt.toString();
    }
    
    /**
     * 提取食物关键词 - 与Node.js后端保持一致
     */
    private String extractFoodKeywords(String text, List<String> includeKeywords, List<String> excludeKeywords) {
        String foodText = text;
        
        // 移除包含关键词
        for (String keyword : includeKeywords) {
            foodText = foodText.replace(keyword, "");
        }
        
        // 移除排除关键词
        for (String keyword : excludeKeywords) {
            foodText = foodText.replace(keyword, "");
        }
        
        return foodText.trim();
    }
    
    /**
     * 格式化健康目标
     */
    private String formatHealthGoals(List<String> healthGoals) {
        List<String> formatted = new ArrayList<>();
        for (String goal : healthGoals) {
            switch (goal) {
                case "lose-weight":
                    formatted.add("减脂");
                    break;
                case "gain-muscle":
                    formatted.add("增肌");
                    break;
                case "maintain":
                    formatted.add("维持体重");
                    break;
                case "improve-immunity":
                    formatted.add("增强免疫力");
                    break;
                case "improve-digestion":
                    formatted.add("改善消化");
                    break;
                default:
                    formatted.add(goal);
            }
        }
        return String.join("、", formatted);
    }
    
    /**
     * 格式化饮食偏好
     */
    private String formatDietPreferences(List<String> dietPreferences) {
        List<String> formatted = new ArrayList<>();
        for (String pref : dietPreferences) {
            switch (pref) {
                case "vegetarian":
                    formatted.add("素食");
                    break;
                case "low-fat":
                    formatted.add("低脂");
                    break;
                case "low-sugar":
                    formatted.add("低糖");
                    break;
                case "high-protein":
                    formatted.add("高蛋白");
                    break;
                case "low-sodium":
                    formatted.add("低盐");
                    break;
                case "gluten-free":
                    formatted.add("无麸质");
                    break;
                default:
                    formatted.add(pref);
            }
        }
        return String.join("、", formatted);
    }
    
    /**
     * 发送HTTP请求到AI服务
     */
    private String sendHttpRequest(AiRequest aiRequest) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(aiServiceUrl);
            
            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + aiServiceToken);
            httpPost.setHeader("Content-Type", "application/json");
            
            // 设置请求体
            String requestBody = objectMapper.writeValueAsString(aiRequest);
            httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));
            
            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.info("AI服务响应状态: {}", response.getStatusLine().getStatusCode());
                log.debug("AI服务响应内容: {}", responseBody);
                return responseBody;
            }
        }
    }
    
    /**
     * 解析AI响应 - 与Node.js后端保持一致的调试信息
     */
    private List<String> parseAiResponse(String responseBody) {
        List<String> recommendedNames = new ArrayList<>();
        
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String aiAnswer = rootNode.path("answer").asText();
            
            // 输出AI响应用于调试 - 与Node.js后端保持一致
            log.info("\n=== AI响应分析 ===");
            log.info("AI原始响应: {}", aiAnswer);
            
            // 提取JSON部分
            Pattern jsonPattern = Pattern.compile("\\{[\\s\\S]*\\}");
            Matcher matcher = jsonPattern.matcher(aiAnswer);
            
            if (matcher.find()) {
                String jsonStr = matcher.group();
                log.info("提取的JSON: {}", jsonStr);
                
                JsonNode recommendationNode = objectMapper.readTree(jsonStr);
                JsonNode recommendations = recommendationNode.path("recommendations");
                
                if (recommendations.isArray()) {
                    List<Map<String, String>> recommendationDetails = new ArrayList<>();
                    for (JsonNode recommendation : recommendations) {
                        String name = recommendation.path("name").asText();
                        String reason = recommendation.path("reason").asText();
                        if (!name.isEmpty()) {
                            recommendedNames.add(name);
                            Map<String, String> detail = new HashMap<>();
                            detail.put("name", name);
                            detail.put("reason", reason);
                            recommendationDetails.add(detail);
                        }
                    }
                    log.info("推荐的食物名称列表: {}", recommendedNames);
                    log.info("推荐详情: {}", recommendationDetails);
                }
            } else {
                log.info("❌ 未找到有效的JSON格式");
            }
            log.info("=== AI响应分析结束 ===\n");
            
        } catch (JsonProcessingException e) {
            log.error("\n❌ AI响应解析失败: {}", e.getMessage());
        }
        
        return recommendedNames;
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
        
        // 如果检测到2个或以上的不同类别，认为是多类别要求
        return categoryKeywords.size() >= 2;
    }
    
    /**
     * 按类别分组食物
     */
    private Map<String, List<Food>> groupFoodsByCategory(List<Food> allFoods) {
        Map<String, List<Food>> categoryMap = new HashMap<>();
        
        for (Food food : allFoods) {
            String category = food.getCategory();
            if (category == null) category = "其他";
            
            categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(food);
        }
        
        return categoryMap;
    }
}
