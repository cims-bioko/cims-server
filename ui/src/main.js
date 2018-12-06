import Vue from 'vue'
import App from './App.vue'
import VueRouter, {Router} from './router'
import Icons from './icons'
import Filters from './filters'
import BootstrapVue from './bootstrap'
import Axios from './axios'
import User from './user'
import i18n from './i18n'

Vue.use(VueRouter)
Vue.use(Axios)
Vue.use(BootstrapVue)
Vue.use(Filters)
Vue.use(Icons)
Vue.use(User)

new Vue({
    render: h => h(App),
    i18n,
    router: Router
}).$mount('#app')
