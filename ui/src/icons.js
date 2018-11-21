import Vue from 'vue'

import { library } from '@fortawesome/fontawesome-svg-core'
import { faHome, faFileAlt, faSync, faBusinessTime, faUser, faUsers, faPlay, faPause, faBolt, faStopwatch, faDownload,
    faTrashAlt, faEdit } from '@fortawesome/free-solid-svg-icons'

library.add(faHome, faFileAlt, faSync, faBusinessTime, faUser, faUsers, faPlay, faPause, faBolt, faStopwatch,
    faDownload, faTrashAlt, faEdit)

import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'

Vue.component('fa-icon', FontAwesomeIcon)