<template>
    <b-container>
        <b-row>
            <b-col>
                <template v-for="(message, index) of messages">
                    <b-alert variant="success" :show="5" :key="`message-${index}`" fade>{{message}}</b-alert>
                </template>
                <template v-for="(error, index) of errors">
                    <b-alert variant="danger" :show="15" :key="`error-${index}`" fade>{{error}}</b-alert>
                </template>
            </b-col>
        </b-row>
        <b-row class="align-items-center">
            <b-col class="col-auto">
                <h1><fa-icon icon="users"/> {{$t('nav.roles')}}</h1>
            </b-col>
            <b-col v-if="$can('CREATE_ROLES')">
                <b-button variant="primary" @click="createItem"><fa-icon icon="plus"/> {{$t('roles.create')}}</b-button>
                <create-dialog ref="createDialog" @ok="itemCreated" :available-privileges="availablePrivileges"/>
            </b-col>
        </b-row>
        <b-row v-if="totalItems > pageSize">
            <b-col>
                <b-pagination align="center" v-model="currentPage" :total-rows="totalItems" :per-page="pageSize" @change="loadPage"/>
            </b-col>
        </b-row>
        <b-row>
            <b-col>
                <b-table :items="items" :fields="fields" show-empty :empty-text="$t('table.empty')">
                    <template slot="actions" slot-scope="data">
                        <b-button-group>
                            <b-button v-if="$can('EDIT_ROLES')" variant="outline-primary" @click="editItem(data.index)">
                                <fa-icon icon="edit"/>
                            </b-button>
                            <b-button v-if="$can('DELETE_ROLES')" variant="outline-primary" @click="deleteItem(data.item.uuid)">
                                <fa-icon icon="trash-alt"/>
                            </b-button>
                        </b-button-group>
                        <edit-dialog :ref="`editDialog${data.index}`" v-bind="data.item" :available-privileges="availablePrivileges" @ok="itemEdited"/>
                    </template>
                </b-table>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import {BContainer, BRow, BCol, BAlert, BButton, BPagination, BTable, BButtonGroup} from 'bootstrap-vue'
    import CreateDialog from './CreateDialog'
    import EditDialog from './EditDialog'
    export default {
        name: 'roles-page',
        data() {
            return {
                fields: [
                    {key: 'name', label: this.$t('roles.name'), tdClass: 'align-middle'},
                    {key: 'description', label: this.$t('roles.description'), tdClass: 'align-middle'},
                    {key: 'actions', label: this.$t('roles.actions'), tdClass: 'align-middle'},
                ],
                errors: [],
                messages: [],
                searchQuery: '',
                totalItems: 0,
                pageSize: 0,
                currentPage: 1,
                items: [],
                availablePrivileges: []
            }
        },
        methods: {
            async loadPage(page) {
                let params = {p: page - 1}
                if (this.searchQuery) {
                    params.q = this.searchQuery
                }
                let rsp = await this.$xhr.get('/roles', {params: params})
                let data = rsp.data
                this.items = data.content
                this.totalItems = data.totalElements
                this.currentPage = data.pageable.pageNumber + 1
                this.pageSize = data.size
                if (this.items.length === 0 && this.currentPage > 1) {
                    this.loadPage(this.currentPage - 1)
                }
            },
            reloadPage() {
                this.loadPage(this.currentPage)
            },
            showMessages(messages) {
                // ensures same message triggers new alert
                this.messages = []
                this.$nextTick(() => this.messages = messages || [])
            },
            editItem(index) {
                this.$refs[`editDialog${index}`].show()
            },
            itemEdited(rsp) {
                this.showMessages(rsp.messages)
                this.reloadPage()
            },
            createItem() {
                this.$refs.createDialog.show()
            },
            itemCreated(rsp) {
                this.showMessages(rsp.messages)
                this.reloadPage()
            },
            async deleteItem(uuid) {
                let response = await this.$xhr.delete(`/role/${uuid}`)
                this.showMessages(response.data.messages)
                this.reloadPage()
            },
            async fetchPrivileges() {
                let response = await this.$xhr.get('/privileges')
                this.availablePrivileges = response.data.map(
                    priv => ({'text': priv.privilege, 'value': priv.uuid})
                )
            }
        },
        mounted() {
            this.fetchPrivileges()
            this.reloadPage()
        },
        components: {
            BContainer, BRow, BCol, BAlert, BButton, BPagination, BTable, BButtonGroup,
            CreateDialog, EditDialog
        }
    }
</script>
