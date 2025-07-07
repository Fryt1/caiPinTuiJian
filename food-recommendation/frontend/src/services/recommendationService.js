import axios from 'axios'

const API_BASE_URL = 'http://localhost:3000'

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
