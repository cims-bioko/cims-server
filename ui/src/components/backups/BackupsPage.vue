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
                <h1><fa-icon icon="business-time"/> {{$t('nav.backups')}}</h1>
            </b-col>
            <b-col v-if="$can('CREATE_BACKUPS')">
                <b-button variant="primary" @click="createItem"><fa-icon icon="plus"/> {{$t('backups.create')}}</b-button>
                <create-dialog ref="createDialog" @ok="itemCreated"/>
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
                    <template slot="created" slot-scope="data">{{data.value|formatDateTime}}</template>
                    <template slot="actions" slot-scope="data">
                        <b-button-group>
                            <b-button v-if="$can('EDIT_BACKUPS')" variant="outline-primary" @click="editItem(data.index)">
                                <fa-icon icon="edit"/>
                            </b-button>
                            <b-button v-if="$can('DELETE_BACKUPS')" variant="outline-primary" @click="deleteItem(data.item.name)">
                                <fa-icon icon="trash-alt"/>
                            </b-button>
                        </b-button-group>
                        <edit-dialog :ref="`editDialog${data.index}`" v-bind="data.item" @ok="itemEdited" v-if="$can('EDIT_BACKUPS')"/>
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
    import SockJS from 'sockjs-client'
    import Stomp from 'webstomp-client'
    export default {
        name: 'backups-page',
        data() {
            return {
                fields: [
                    {key: 'name', label: this.$t('backups.name'), tdClass: 'align-middle'},
                    {key: 'description', label: this.$t('backups.description'), tdClass: 'align-middle'},
                    {key: 'created', label: this.$t('backups.created'), tdClass: 'align-middle'},
                    {key: 'actions', label: this.$t('backups.actions'), tdClass: 'align-middle'}
                ],
                errors: [],
                messages: [],
                totalItems: 0,
                pageSize: 0,
                currentPage: 1,
                items: []
            }
        },
        methods: {
            async loadPage(page) {
                let response = await this.$xhr.get('/backups', {params: {p: page - 1}})
                let data = response.data
                this.items = data.content
                this.totalItems = data.totalElements
                this.currentPage = data.pageable.pageNumber + 1
                this.pageSize = data.size
                if (this.items.length === 0 && this.currentPage > 1) {
                    this.loadPage(this.currentPage-1)
                }
            },
            reloadPage() {
                this.loadPage(this.currentPage)
            },
            async deleteItem(name) {
                let response = await this.$xhr.delete(`/backup/${name}`)
                this.showMessages(response.data.messages)
                this.reloadPage()
            },
            createItem() {
                this.$refs.createDialog.show()
            },
            itemCreated(rsp) {
                this.showMessages(rsp.messages)
                this.reloadPage()
            },
            editItem(index) {
                this.$refs[`editDialog${index}`].show()
            },
            itemEdited(rsp) {
                this.showMessages(rsp.messages)
                this.reloadPage()
            },
            showMessages(messages) {
                // ensures same message triggers new alert
                this.messages = []
                this.$nextTick(() => this.messages = messages || [])
            },
            wsconnect() {
                let socket = new SockJS('/stomp')
                let stomp = Stomp.over(socket, {version: Stomp.VERSIONS.V1_2, debug: null})
                stomp.connect({},
                    () => {
                        this.stomp = stomp
                        stomp.subscribe('/topic/backups', msg => this.handleMessage(JSON.parse(msg.body)))
                    })
            },
            handleMessage(msg) {
                if (msg.status === 'created') {
                    this.messages.push(this.$t("backups.success", [msg.backup]));
                    this.reloadPage()
                } else if (msg.status === 'error') {
                    this.errors.push(this.$t("backups.error", [msg.backup, msg.errorMessage]))
                }
            }
        },
        mounted() {
            this.reloadPage()
            this.wsconnect()
        },
        beforeDestroy() {
            if (this.stomp) {
                this.stomp.disconnect()
            }
        },
        components: {
            BContainer, BRow, BCol, BAlert, BButton, BPagination, BButtonGroup, BTable, CreateDialog, EditDialog
        }
    }
</script>
