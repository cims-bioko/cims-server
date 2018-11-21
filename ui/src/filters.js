import Vue from 'vue'
import moment from 'moment'

Vue.filter('formatDate', v => {
    if (v) {
        return moment(v).utcOffset(v).format('MM/DD/YYYY hh:mm:ss z')
    }
})
