import axios from 'axios'

// identify axios requests so server sends 403s
axios.defaults.headers['X-Requested-With'] = 'XMLHttpRequest'

// force browser navigation when 403 with location payload is received
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