import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api/v1": {
        target: "http://localhost:8080",
        changeOrigin: true
      }
    }
  },
  build: {
    sourcemap: false,
    reportCompressedSize: false,
    minify: "esbuild",
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ["react", "react-dom", "react-router-dom"],
          motion: ["framer-motion"],
          flow: ["@xyflow/react"],
          query: ["@tanstack/react-query"]
        }
      }
    }
  }
});
