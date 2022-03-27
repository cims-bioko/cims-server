import {formatDate, formatDateTime, formatLargeNumber} from './formatting'

const filters = {
    formatDate: formatDate,
    formatDateTime: formatDateTime,
    formatLargeNumber: formatLargeNumber
}

export default {
    install(Vue) {
        Object.entries(filters)
            .forEach(([name, fn]) => Vue.filter(name, fn))
    }
}