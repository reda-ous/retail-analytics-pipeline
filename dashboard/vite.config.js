import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // Forwards to the consumer's REST API server-side, so the browser
      // only ever talks to this dev server (same-origin) and we never
      // need to touch CORS config on the Spring Boot side.
      '/api': 'http://localhost:8080',
    },
  },
})
