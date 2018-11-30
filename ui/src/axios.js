import axios from 'axios'

// used by server to identify requests from axios and send 403 (handled by interceptor below)
axios.defaults.headers['X-Requested-With'] = 'XMLHttpRequest'

// force browser navigation if a request gets a 403 redirect
axios.interceptors.response.use(
    (response) => response,
    (error) => {
        let rsp = error.response
        if (rsp && rsp.status === 403 && rsp.data.location) {
            window.location = rsp.data.location
        } else {
            return Promise.reject(error)
        }
    }
)

export default {
    install(Vue) {
        Object.defineProperties(Vue.prototype, {
            $xhr: {
                get() {
                    return axios
                }
            }
        })
    }
}