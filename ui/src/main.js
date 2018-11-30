import Vue from 'vue'
import App from './App.vue'
import Router from './router'
import './icons'
import './filters'
import './bootstrap'
import Axios from './axios'

Vue.use(Axios)

new Vue({
  render: h => h(App),
  router: Router
}).$mount('#app')
