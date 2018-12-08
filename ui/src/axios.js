import axios from 'axios'
import NProgress from 'nprogress'

// identify axios requests so server sends 403s
axios.defaults.headers['X-Requested-With'] = 'XMLHttpRequest'

// start the progress indicator before every request
axios.interceptors.request.use(config => {
    NProgress.start()
    return config
})

// force browser navigation when 403 with location payload is received
axios.interceptors.response.use((response) => response, (error) => {
        let rsp = error.response
        if (rsp && rsp.status === 403 && rsp.data.location) {
            window.location = rsp.data.location
        } else {
            return Promise.reject(error)
        }
    }
)

// stop the progress indicator before responses finishes
axios.interceptors.response.use(response => {
    NProgress.done()
    return response
})

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