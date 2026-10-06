const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');

module.exports = {
  
  webpack: {
    plugins: [new ESLintPlugin({failOnError: false})],
    module: {
      rules: [
        {
          test: /\.s[ac]ss$/i,
          use: [
            // Creates `style` nodes from JS strings
            "style-loader",
            // Translates CSS into CommonJS
            "css-loader",
            // Compiles Sass to CSS
            "sass-loader"
          ]
        }
      ]
    },
    configure: {
      resolve: {
        fallback: {
          path: require.resolve("path-browserify")
        }
      }
    },
    alias: {
      "@": [
        path.join(path.resolve(__dirname, './src/custom')),
        path.join(path.resolve(__dirname, './src/default'))
      ],
      "@Components": [
        path.join(path.resolve(__dirname, './src/custom/views/components')),
        path.join(path.resolve(__dirname, './src/default/views/components'))
      ],
      "@Elements": [
        path.join(path.resolve(__dirname, './src/custom/views/elements')),
        path.join(path.resolve(__dirname, './src/default/views/elements'))
      ],
      "@Pages": [
        path.join(path.resolve(__dirname, './src/custom/views/pages')),
        path.join(path.resolve(__dirname, './src/default/views/pages'))
      ],
      "@Templates": [
        path.join(path.resolve(__dirname, './src/custom/views/templates')),
        path.join(path.resolve(__dirname, './src/default/views/templates'))
      ],
      "@Styles": [
        path.join(path.resolve(__dirname, './src/custom/styles')),
        path.join(path.resolve(__dirname, './src/default/styles'))
      ],
      "@Assets": [
        path.join(path.resolve(__dirname, './src/custom/assets')),
        path.join(path.resolve(__dirname, './src/default/assets'))
      ]
    }
  }
};