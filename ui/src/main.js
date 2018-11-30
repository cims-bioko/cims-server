import Vue from 'vue'
import App from './App.vue'
import VueRouter, {Router} from './router'
import Icons from './icons'
import Filters from './filters'
import BootstrapVue from './bootstrap'
import Axios from './axios'

Vue.use(VueRouter)
Vue.use(Axios)
Vue.use(BootstrapVue)
Vue.use(Filters)
Vue.use(Icons)

new Vue({
  render: h => h(App),
  router: Router
}).$mount('#app')
