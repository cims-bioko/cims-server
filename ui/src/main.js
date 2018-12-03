import Vue from 'vue'
import App from './App.vue'
import VueRouter, {Router} from './router'
import Icons from './icons'
import Filters from './filters'
import BootstrapVue from './bootstrap'
import Axios from './axios'
import User from './user'

Vue.use(VueRouter)
Vue.use(Axios)
Vue.use(BootstrapVue)
Vue.use(Filters)
Vue.use(Icons)
Vue.use(User)

new Vue({
    render: h => h(App),
    router: Router
}).$mount('#app')
