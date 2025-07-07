import axios from 'axios'

const DIFY_API_KEY = 'app-uAGYYnfpXdB5t1EdYdNP7wgP'
const DIFY_BASE_URL = 'http://localhost:8082/v1'

async function testAPIConnection() {
  console.log('开始测试Dify API连接...')
  
  try {
    // 1. 测试基础连接
    console.log('测试基础连接...')
    await axios.get(`${DIFY_BASE_URL}/`, {
      headers: {
        'Authorization': `Bearer ${DIFY_API_KEY}`,
        'Content-Type': 'application/json'
      }
    })
    console.log('✓ 基础连接成功')

    // 2. 测试认证
    console.log('测试API Key认证...')
    const authTest = await axios.post(`${DIFY_BASE_URL}/chat-messages`, {
      inputs: {},
      query: "测试连接",
      response_mode: "blocking",
      user: "test-user"
    }, {
      headers: {
        'Authorization': `Bearer ${DIFY_API_KEY}`,
        'Content-Type': 'application/json'
      }
    })
    
    if (authTest.data) {
      console.log('✓ API Key认证成功')
      console.log('API响应示例:', authTest.data)
    } else {
      console.log('× API返回了空响应')
    }

    // 3. 测试推荐功能
    console.log('测试推荐功能...')
    const recTest = await axios.post(`${DIFY_BASE_URL}/chat-messages`, {
      inputs: {
        health_goal: "weight_loss",
        diet_preference: "low_carb"
      },
      query: "测试食物推荐",
      response_mode: "blocking",
      user: "test-user"
    }, {
      headers: {
        'Authorization': `Bearer ${DIFY_API_KEY}`,
        'Content-Type': 'application/json'
      }
    })
    
    console.log('✓ 推荐功能测试完成')
    console.log('推荐结果:', recTest.data)

  } catch (error) {
    console.error('API测试失败:', error.message)
    if (error.response) {
      console.error('HTTP状态码:', error.response.status)
      console.error('错误详情:', JSON.stringify(error.response.data, null, 2))
      console.error('请检查:')
      console.error('1. Dify应用是否已发布')
      console.error('2. API Key是否正确')
      console.error('3. 请求参数是否符合API文档要求')
    }
  }
}

// 执行测试
testAPIConnection()
