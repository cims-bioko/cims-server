import Vue from 'vue'
import App from './App.vue'
import VueRouter from 'vue-router'
import BootstrapVue from 'bootstrap-vue'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(VueRouter)
Vue.use(BootstrapVue)

const Home = { name: 'Home', render: (e) => e('h1', 'Home') }
const Another = { name: 'Another', render: (e) => e('h1', 'Another') }

const router = new VueRouter({
  routes: [
    { name: 'home', path: '/', component: Home },
    { name: 'another', path: '/another', component: Another },
  ]
})

new Vue({
  render: h => h(App),
  router: router
}).$mount('#app')
