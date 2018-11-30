import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

import { Alert, Button, ButtonGroup, ButtonToolbar, Card, Form, FormFile, FormGroup, FormInput, FormRadio, FormSelect,
    InputGroup, Layout, Modal, Navbar, Pagination, Progress, Table} from 'bootstrap-vue/es/components'

export default {
    install(Vue) {
        [Alert, Button, ButtonGroup, ButtonToolbar, Card, Form, FormFile, FormGroup, FormInput, FormRadio, FormSelect,
            InputGroup, Layout, Modal, Navbar, Pagination, Progress, Table]
            .forEach(v => Vue.use(v))
    }
}