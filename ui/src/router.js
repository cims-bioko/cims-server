import VueRouter from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const HomePage = () => import('./components/HomePage.vue')
const FormsPage = () => import('./components/forms/FormsPage.vue')
const SyncPage = () => import('./components/SyncPage.vue')
const BackupsPage = () => import('./components/backups/BackupsPage.vue')
const UsersPage = () => import('./components/users/UsersPage.vue')
const RolesPage = () => import('./components/roles/RolesPage.vue')
const DevicesPage = () => import('./components/devices/DevicesPage.vue')
const FieldworkersPage = () => import('./components/fieldworkers/FieldworkersPage.vue')

export const Router = new VueRouter({
    routes: [
        { name: 'home', path: '/', component: HomePage },
        { name: 'forms', path: '/forms', component: FormsPage },
        { name: 'sync', path: '/sync', component: SyncPage },
        { name: 'backups', path: '/backups', component: BackupsPage },
        { name: 'users', path: '/users', component: UsersPage },
        { name: 'roles', path: '/roles', component: RolesPage },
        { name: 'devices', path: '/devices', component: DevicesPage },
        { name: 'fieldworkers', path: '/fieldworkers', component: FieldworkersPage },
    ]
})

Router.beforeEach((to, from, next) => {
    if (to.name) {
        NProgress.start()
    }
    next()
})

Router.afterEach(() => {
    NProgress.done()
})

export default VueRouter