import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

import Home from './components/Home.vue'
import Forms from './components/Forms.vue'
import Sync from './components/Sync.vue'
import Backups from './components/Backups.vue'
import Users from './components/Users.vue'
import Roles from './components/Roles.vue'
import Fieldworkers from './components/Fieldworkers.vue'

export default new VueRouter({
    routes: [
        { name: 'home', path: '/', component: Home },
        { name: 'forms', path: '/forms', component: Forms },
        { name: 'sync', path: '/sync', component: Sync },
        { name: 'backups', path: '/backups', component: Backups },
        { name: 'users', path: '/users', component: Users },
        { name: 'roles', path: '/roles', component: Roles },
        { name: 'fieldworkers', path: '/fieldworkers', component: Fieldworkers },
    ]
});
