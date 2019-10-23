<template>
    <b-container>
        <b-row>
            <b-col>
                <template v-for="(message, index) of messages">
                    <b-alert variant="success" :show="15" :key="`message-${index}`" fade>{{message}}</b-alert>
                </template>
                <template v-for="(error, index) of errors">
                    <b-alert variant="danger" :show="15" :key="`error-${index}`" fade>{{error}}</b-alert>
                </template>
            </b-col>
        </b-row>
        <b-row class="align-items-center">
            <b-col class="col-auto">
                <h1><fa-icon icon="tablet-alt"/> {{$t('nav.devices')}}</h1>
            </b-col>
            <b-col v-if="$can('CREATE_DEVICES')">
                <b-button variant="primary" @click="createItem"><fa-icon icon="plus"/> {{$t('devices.create')}}</b-button>
                <create-dialog ref="createDialog" @ok="itemCreated" />
            </b-col>
            <b-col>
                <search-box :placeholder="$t('devices.searchph')" v-model="searchQuery" @search="search" />
            </b-col>
        </b-row>
        <b-row v-if="totalItems > pageSize">
            <b-col>
                <b-pagination align="center" v-model="currentPage" :total-rows="totalItems" :per-page="pageSize" @change="loadPage"/>
            </b-col>
        </b-row>
        <b-row>
            <b-col>
                <b-table :items="decoratedItems" :fields="fields" show-empty :empty-text="$t('table.empty')">
                    <template slot="lastLogin" slot-scope="data">{{data.value|formatDateTime}}</template>
                    <template slot="actions" slot-scope="data">
                        <b-button-group>
                            <b-button v-if="$can('EDIT_DEVICES')" variant="outline-primary" @click="editItem(data.index)" :disabled="data.item.deleted">
                                <fa-icon icon="edit"/>
                            </b-button>
                            <b-button v-if="$can('DELETE_DEVICES') && !data.item.deleted" variant="outline-primary" @click="deleteItem(data.item.uuid)">
                                <fa-icon icon="trash-alt"/>
                            </b-button>
                            <b-button v-if="$can('RESTORE_DEVICES') && data.item.deleted" variant="outline-primary" @click="restoreItem(data.item.uuid)">
                                <fa-icon icon="undo-alt"/>
                            </b-button>
                            <b-button v-if="$can('VIEW_MOBILE_CONFIG_CODES') && hasSecret(data.item.name)" variant="outline-primary" @click="showQrcode(data.index)">
                                <fa-icon icon="qrcode"/>
                            </b-button>
                        </b-button-group>
                        <edit-dialog v-if="$can('EDIT_DEVICES')" :ref="`editDialog${data.index}`" v-bind="data.item" @ok="itemEdited"/>
                        <qr-code-dialog v-if="$can('VIEW_MOBILE_CONFIG_CODES') && hasSecret(data.item.name)" :ref="`qrcodeDialog${data.index}`"
                                        :name="data.item.name" :secret="getSecret(data.item.name)" @forget="clearSecret" />
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
    import SearchBox from '../SearchBox'
    import QrCodeDialog from './QrCodeDialog'
    export default {
        name: 'devices-page',
        data() {
            return {
                fields: [
                    {key: 'name', label: this.$t('devices.name'), tdClass: 'align-middle'},
                    {key: 'description', label: this.$t('devices.description'), tdClass: 'align-middle'},
                    {key: 'lastLogin', label: this.$t('devices.lastLogin'), tdClass: 'align-middle'},
                    {key: 'actions', label: this.$t('devices.actions'), tdClass: 'align-middle'}
                ],
                errors: [],
                messages: [],
                searchQuery: '',
                totalItems: 0,
                pageSize: 0,
                currentPage: 1,
                items: []
            }
        },
        methods: {
            async loadPage(page) {
                let params = {p: page - 1}
                if (this.searchQuery) {
                    params.q = this.searchQuery
                }
                let rsp = await this.$xhr.get('/devices', {params: params})
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
                this.saveSecret(rsp.data)
                this.showMessages(rsp.messages)
                this.reloadPage()
            },
            saveSecret(data) {
                sessionStorage.setItem(`ds-${data.device}`, `${data.secret}`)
            },
            getSecret(device) {
                return sessionStorage.getItem(`ds-${device}`)
            },
            hasSecret(device) {
                return sessionStorage.getItem(`ds-${device}`) != null
            },
            clearSecret(device) {
                sessionStorage.removeItem(`ds-${device}`);
                this.reloadPage();
            },
            showQrcode(index) {
                this.$refs[`qrcodeDialog${index}`].show()
            },
            createItem() {
                this.$refs.createDialog.show()
            },
            itemCreated(rsp) {
                this.showMessages(rsp.messages)
                this.reloadPage()
            },
            async deleteItem(uuid) {
                let response = await this.$xhr.delete(`/device/${uuid}`)
                this.showMessages(response.data.messages)
                this.reloadPage()
            },
            async restoreItem(uuid) {
                let response = await this.$xhr.put(`/device/restore/${uuid}`)
                this.showMessages(response.data.messages)
                this.reloadPage()
            },
            search() {
                this.reloadPage()
            }
        },
        mounted() {
            this.reloadPage()
        },
        computed: {
            decoratedItems() {
                return this.items.map(
                    item => item.deleted? Object.assign({_rowVariant:'rowclasshack deleted'}, item) : item)
            }
        },
        components: {
            BContainer, BRow, BCol, BAlert, BButton, BPagination, BTable, BButtonGroup,
            CreateDialog, EditDialog, SearchBox, QrCodeDialog
        }
    }
</script>

<style scoped>
    >>> .b-table tr.deleted td {
        text-decoration-line: line-through;
    }
</style>
