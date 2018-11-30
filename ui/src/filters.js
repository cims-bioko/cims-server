import moment from 'moment'

let filters = {
    formatDate(v) {
        if (v) {
            return moment(v).utcOffset(v).format('MM/DD/YYYY hh:mm:ss z')
        }
    }
}

export default {
    install(Vue) {
        Object.entries(filters)
            .forEach(([name, fn]) => Vue.filter(name, fn))
    }
}