import axios from 'axios'

// 动态获取后端地址，支持局域网访问
const getAPIBaseURL = () => {
  const hostname = window.location.hostname
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:3001'
  } else {
    // 局域网访问时使用相同的IP地址
    return `http://${hostname}:3001`
  }
}

const API_BASE_URL = getAPIBaseURL()

export default {
  async getRecommendations(userId, filters = {}) {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/recommendations`, {
        userId,
        filters
      }, {
        headers: {
          'Content-Type': 'application/json'
        }
      })
      return response.data
    } catch (error) {
      console.error('获取推荐失败:', error)
      throw new Error('获取推荐失败，请稍后重试')
    }
  },

  // 保留方法签名以保持接口兼容
  parseDifyResponse() {
    return []
  }
}
