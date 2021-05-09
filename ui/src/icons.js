import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {faHome, faFileAlt, faSync, faBusinessTime, faUser, faUsers, faPlay, faPause, faBolt, faStopwatch, faDownload,
    faTrashAlt, faEdit, faPlus, faCheck, faTimes, faUpload, faSearch, faUndoAlt, faTabletAlt, faQrcode, faFolderMinus,
    faShuttleVan, faCogs, faAngleUp, faAngleDown, faCheckCircle, faTimesCircle, faFileUpload} from '@fortawesome/free-solid-svg-icons'

library.add(faHome, faFileAlt, faSync, faBusinessTime, faUser, faUsers, faPlay, faPause, faBolt, faStopwatch,
    faDownload, faTrashAlt, faEdit, faPlus, faCheck, faTimes, faUpload, faSearch, faUndoAlt, faTabletAlt, faQrcode,
    faFolderMinus, faShuttleVan, faCogs, faAngleUp, faAngleDown, faCheckCircle, faTimesCircle, faFileUpload)

export default {
    install(Vue) {
        Vue.component('fa-icon', FontAwesomeIcon)
    }
}