import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// 导入对应包

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

import ElementPlus from 'unplugin-element-plus/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver(
          { importStyle: 'sass' }
      )],
    }),
    Components({
      resolvers: [ElementPlusResolver(
          { importStyle: 'sass' }
      )],
    }),

    // 按需定制主题配置
    ElementPlus({
      useSource: true,
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 自动导入定制化样式文件进行样式覆盖
        additionalData: `
          @use "@/assets/styles/variables.scss" as *;
        `,
      }
    }
  },
  server: {
    port: 5173,  // 你的前端端口
    proxy: {
      // 文件上传接口代理
      '/files': {
        target: 'http://localhost:9090',  // 你的后端地址
        changeOrigin: true
      },
      // Banner 相关接口代理
      '/banner': {
        target: 'http://localhost:9090',
        changeOrigin: true
      }
    }
  }
})
