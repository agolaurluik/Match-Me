import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
    define: {
    global: 'globalThis' 
  },
  server: {
    host: true,  // binds to 0.0.0.0, meaning all interfaces
    port: 5173
  }
})