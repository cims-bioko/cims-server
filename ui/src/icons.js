import Vue from 'vue'

import { library } from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import { faHome, faFileAlt, faSync, faBusinessTime, faUser, faUsers, faPlay, faPause, faBolt, faStopwatch, faDownload,
    faTrashAlt, faEdit, faPlus, faCheck, faTimes, faUpload, faSearch, faUndoAlt } from '@fortawesome/free-solid-svg-icons'

library.add(faHome, faFileAlt, faSync, faBusinessTime, faUser, faUsers, faPlay, faPause, faBolt, faStopwatch,
    faDownload, faTrashAlt, faEdit, faPlus, faCheck, faTimes, faUpload, faSearch, faUndoAlt)

Vue.component('fa-icon', FontAwesomeIcon)