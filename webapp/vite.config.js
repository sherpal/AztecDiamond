import { createHtmlPlugin } from 'vite-plugin-html'

import { defineConfig } from 'vite'

const scalaVersion = "3.2.2"

// https://vitejs.dev/config/
export default defineConfig(({ command, mode, ssrBuild }) => {
  const mainJS = `/target/scala-${scalaVersion}/webapp-${mode === "production" ? "opt" : "fastopt"
    }/main.js`
  console.log("mainJS", mainJS)
  const script = `<script type="module" src="${mainJS}"></script>`

  const base = "/aztec-diamond-generator/"
  const viteMetadata = `<script type="text/javascript">window.basePath = "${base}";window.aztecProdMode = ${mode === "production"};</script>`

  return {
    publicDir: "./public",
    plugins: createHtmlPlugin({
      minify: process.env.NODE_ENV === 'production',
      inject: {
        data: {
          "script": script,
          "viteMetadata": viteMetadata
        }
      }
    }),
    base: base,
    server: {
      open: base
    }
  }
})
