import VueRouter from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const HomePage = () => import('./components/HomePage.vue')
const FormsPage = () => import('./components/forms/FormsPage.vue')
const BackupsPage = () => import('./components/backups/BackupsPage.vue')
const UsersPage = () => import('./components/users/UsersPage.vue')
const RolesPage = () => import('./components/roles/RolesPage.vue')
const DevicesPage = () => import('./components/devices/DevicesPage.vue')
const CampaignsPage = () => import('./components/campaigns/CampaignsPage.vue')
const Campaign = () => import('./components/campaigns/Campaign.vue')
const CampaignBindings = () => import('./components/campaigns/CampaignBindings.vue')
const FieldworkersPage = () => import('./components/fieldworkers/FieldworkersPage.vue')
const SubmissionsPage = () => import('./components/submissions/SubmissionsPage.vue')
const RebuildIndexPage = () => import('./components/search/RebuildIndexPage.vue')

export const Router = new VueRouter({
    routes: [
        { name: 'home', path: '/', component: HomePage },
        { name: 'forms', path: '/forms', component: FormsPage },
        { name: 'backups', path: '/backups', component: BackupsPage },
        { name: 'users', path: '/users', component: UsersPage },
        { name: 'roles', path: '/roles', component: RolesPage },
        { name: 'devices', path: '/devices', component: DevicesPage },
        { name: 'campaigns', path: '/campaigns', component: CampaignsPage },
        {
            path: '/campaigns/:campaign', component: Campaign, props: true, children: [
                { path: '', redirect: 'bindings' },
                { path: 'bindings', component: CampaignBindings, props: true }
            ]
        },
        { name: 'fieldworkers', path: '/fieldworkers', component: FieldworkersPage },
        { name: 'submissions', path: '/submissions', component: SubmissionsPage },
        { name: 'rebuildindex', path: '/rebuildindex', component: RebuildIndexPage }
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