import VueRouter from 'vue-router'

import HomePage from './components/HomePage.vue'
import FormsPage from './components/forms/FormsPage.vue'
import SyncPage from './components/SyncPage.vue'
import BackupsPage from './components/backups/BackupsPage.vue'
import UsersPage from './components/users/UsersPage.vue'
import RolesPage from './components/roles/RolesPage.vue'
import FieldworkersPage from './components/fieldworkers/FieldworkersPage.vue'

export const Router = new VueRouter({
    routes: [
        { name: 'home', path: '/', component: HomePage },
        { name: 'forms', path: '/forms', component: FormsPage },
        { name: 'sync', path: '/sync', component: SyncPage },
        { name: 'backups', path: '/backups', component: BackupsPage },
        { name: 'users', path: '/users', component: UsersPage },
        { name: 'roles', path: '/roles', component: RolesPage },
        { name: 'fieldworkers', path: '/fieldworkers', component: FieldworkersPage },
    ]
})

export default VueRouter