import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 前端工程配置：开发阶段将 /api 请求代理到本地 Tomcat 后端，避免浏览器跨域问题。
export default defineConfig({
  // Vue 单文件组件支持插件，负责解析 .vue 文件。
  plugins: [vue()],
  server: {
    // 前端开发服务器端口，与 spring-mvc.xml 中允许的跨域来源保持一致。
    port: 5173,
    proxy: {
      '/api': {
        // 后端 war 包部署后的默认访问地址；如 Tomcat 端口或上下文变化，需要同步修改这里。
        target: 'http://localhost:8080/fishMall',
        changeOrigin: true
      }
    }
  }
})
