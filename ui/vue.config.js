module.exports = {
  devServer: {
    port: 8081,
    proxy: 'http://localhost:8080'
  },

  outputDir: 'target/dist',

  pluginOptions: {
    i18n: {
      locale: 'en',
      fallbackLocale: 'en',
      localeDir: 'locales',
      enableInSFC: false
    }
  }
}
