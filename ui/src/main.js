import Vue from 'vue'
import App from './App.vue'
import Home from './components/Home.vue'
import Forms from './components/Forms.vue'
import Sync from './components/Sync.vue'
import Backups from './components/Backups.vue'
import Users from './components/Users.vue'
import Roles from './components/Roles.vue'
import Fieldworkers from './components/Fieldworkers.vue'

import VueRouter from 'vue-router'
import BootstrapVue from 'bootstrap-vue'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faHome, faFileAlt, faSync, faBusinessTime, faUser, faUsers, faPlay, faPause, faBolt, faStopwatch, faDownload } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import moment from 'moment'

Vue.use(VueRouter)
Vue.use(BootstrapVue)

library.add(faHome, faFileAlt, faSync, faBusinessTime, faUser, faUsers, faPlay, faPause, faBolt, faStopwatch, faDownload)
Vue.component('fa-icon', FontAwesomeIcon)
Vue.filter('formatDate', v => {if (v) { return moment(v).utcOffset(v).format('MM/DD/YYYY hh:mm:ss z') }})

const router = new VueRouter({
  routes: [
    { name: 'home', path: '/', component: Home },
    { name: 'forms', path: '/forms', component: Forms },
    { name: 'sync', path: '/sync', component: Sync },
    { name: 'backups', path: '/backups', component: Backups },
    { name: 'users', path: '/users', component: Users },
    { name: 'roles', path: '/roles', component: Roles },
    { name: 'fieldworkers', path: '/fieldworkers', component: Fieldworkers },
  ]
})

new Vue({
  render: h => h(App),
  router: router
}).$mount('#app')
