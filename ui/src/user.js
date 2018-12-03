import Vue from 'vue'

const user = new Vue({
    data: {
        username: null,
        permissions: {}
    }
})

async function initializeUser() {
    let rsp = await this.$xhr.get('/whoami'), data = rsp.data
    this.$user.username = data.username
    this.$user.permissions = data.permissions.reduce((tbl, p) => Object.assign(tbl, {[p]: true}), {})
}

let init = false

export default {
    install(Vue) {
        Object.defineProperties(Vue.prototype, {
            $user: {
                get() {
                    if (!init) {
                        initializeUser.call(this)
                        init = true
                    }
                    return user.$data
                }
            }
        })
        Vue.mixin({
            methods: {
                $can(permission) {
                    return permission in this.$user.permissions
                }
            }
        })
    }
}