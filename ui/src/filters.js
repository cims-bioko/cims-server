import i18n from './i18n'

function toDateObj(v) {
    return typeof v === 'string'? new Date(v) : v
}

let filters = {
    formatDate(v) {
        if (v) {
            return i18n.d(toDateObj(v), 'short')
        }
    },
    formatDateTime(v) {
        if (v) {
            return i18n.d(toDateObj(v), 'long')
        }
    }
}

export default {
    install(Vue) {
        Object.entries(filters)
            .forEach(([name, fn]) => Vue.filter(name, fn))
    }
}