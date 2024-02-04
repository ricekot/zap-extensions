import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import process from "process";

// https://vitejs.dev/config/
export default defineConfig({
  base: process.env.BASE_URL,
  plugins: [react()],
  server: {
    // port: 8001,
  },
  assetsInclude: ["**/*.ico"],
});
