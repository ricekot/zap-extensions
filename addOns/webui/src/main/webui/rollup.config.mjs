import autoprefixer from "autoprefixer";
import commonjs from "@rollup/plugin-commonjs";
import nodeResolve from "@rollup/plugin-node-resolve";
import postcss from "rollup-plugin-postcss";
import tailwindcss from "tailwindcss";
import copy from "rollup-plugin-copy";
import postcssImport from "postcss-import";

/** @type {import('rollup').RollupOptions} */
const config = {
  input: "scripts/main.js",
  output: {
    name: "webui",
    file: "dist/webui/bundle.js",
    format: "iife",
  },
  plugins: [
    commonjs(),
    postcss({
      extensions: [".css"],
      plugins: [
        postcssImport(),
        tailwindcss({
          content: ["index.html", "templates/**/*.html"],
        }),
        autoprefixer(),
      ],
    }),
    nodeResolve(),
    copy({
      targets: [
        { src: "index.html", dest: "dist/webui" },
        { src: "404.html", dest: "dist/webui" },
        { src: "assets", dest: "dist/webui" },
        { src: "templates", dest: "dist/webui", rename: "render" },
      ],
    }),
  ],
};

export default config;
