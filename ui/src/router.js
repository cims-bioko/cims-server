import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

import Home from './components/Home.vue'
import FormsPage from './components/forms/FormsPage.vue'
import Sync from './components/Sync.vue'
import BackupsPage from './components/backups/BackupsPage.vue'
import Users from './components/Users.vue'
import Roles from './components/roles/RolesPage.vue'
import Fieldworkers from './components/fieldworkers/FieldworkersPage.vue'

export default new VueRouter({
    routes: [
        { name: 'home', path: '/', component: Home },
        { name: 'forms', path: '/forms', component: FormsPage },
        { name: 'sync', path: '/sync', component: Sync },
        { name: 'backups', path: '/backups', component: BackupsPage },
        { name: 'users', path: '/users', component: Users },
        { name: 'roles', path: '/roles', component: Roles },
        { name: 'fieldworkers', path: '/fieldworkers', component: Fieldworkers },
    ]
});
