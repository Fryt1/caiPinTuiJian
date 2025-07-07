import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0', // 允许外部访问
    port: 5173,      // 指定端口
    strictPort: true, // 如果端口被占用，不自动尝试下一个端口
  }
})
