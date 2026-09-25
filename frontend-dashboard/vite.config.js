import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],

  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },

      '/actuator': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },

      '/health/gateway': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) =>
          path.replace(/^\/health\/gateway/, ''),
      },

      '/health/registry': {
        target: 'http://localhost:8761',
        changeOrigin: true,
        rewrite: (path) =>
          path.replace(/^\/health\/registry/, ''),
      },

      '/health/product': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) =>
          path.replace(/^\/health\/product/, ''),
      },

      '/health/inventory': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (path) =>
          path.replace(/^\/health\/inventory/, ''),
      },

      '/health/recommendation': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        rewrite: (path) =>
          path.replace(/^\/health\/recommendation/, ''),
      },
    },
  },
})