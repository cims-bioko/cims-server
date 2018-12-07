import Vue from 'vue'
import App from './App.vue'
import VueRouter, {Router} from './router'
import Icons from './icons'
import Filters from './filters'
import Axios from './axios'
import User from './user'
import i18n from './i18n'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(VueRouter)
Vue.use(Axios)
Vue.use(Filters)
Vue.use(Icons)
Vue.use(User)

new Vue({
    render: h => h(App),
    i18n,
    router: Router
}).$mount('#app')
