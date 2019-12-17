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
                <h1><fa-icon icon="shuttle-van"/> {{$t('nav.campaigns')}}</h1>
            </b-col>
            <b-col v-if="$can('CREATE_CAMPAIGNS')">
                <b-button variant="primary" @click="createItem"><fa-icon icon="plus"/> {{$t('campaigns.create')}}</b-button>
                <create-dialog ref="createDialog" @ok="itemCreated" />
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
                    <template slot="start" slot-scope="data">{{data.value|formatDateTime}}</template>
                    <template slot="end" slot-scope="data">{{data.value|formatDateTime}}</template>
                    <template slot="defaultCampaign" slot-scope="data">
                        <fa-icon v-if="data.value" icon="check"/>
                    </template>
                    <template slot="actions" slot-scope="data">
                        <b-button-group>
                            <b-button v-if="$can('EDIT_CAMPAIGNS')" variant="outline-primary" @click="editItem(data.index)" :disabled="data.item.deleted">
                                <fa-icon icon="edit"/>
                            </b-button>
                            <b-button v-if="$can('UPLOAD_CAMPAIGNS')" variant="outline-primary" @click="upload(data.index)">
                                <fa-icon icon="upload"/>
                            </b-button>
                            <b-button v-if="$can('DOWNLOAD_CAMPAIGNS')" variant="outline-primary" :href="`/campaign/export/${data.item.name}`" download>
                                <fa-icon icon="download"/>
                            </b-button>
                        </b-button-group>
                        <upload-dialog :ref="`uploadDialog${data.index}`" v-bind="data.item" @ok="itemUploaded" v-if="$can('UPLOAD_CAMPAIGNS')" />
                        <edit-dialog :ref="`editDialog${data.index}`" v-bind="data.item" @ok="itemEdited" v-if="$can('EDIT_CAMPAIGNS') && !data.item.deleted" />
                    </template>
                </b-table>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import {BContainer, BRow, BCol, BAlert, BButton, BPagination, BTable, BButtonGroup} from 'bootstrap-vue'
    import EditDialog from './EditDialog'
    import UploadDialog from './UploadDialog'
    import CreateDialog from './CreateDialog'
    export default {
        name: 'campaigns-page',
        data() {
            return {
                fields: [
                    {key: 'name', label: this.$t('campaigns.name'), tdClass: 'align-middle'},
                    {key: 'description', label: this.$t('campaigns.description'), tdClass: 'align-middle'},
                    {key: 'start', label: this.$t('campaigns.start'), tdClass: 'align-middle'},
                    {key: 'end', label: this.$t('campaigns.end'), tdClass: 'align-middle'},
                    {key: 'defaultCampaign', label: this.$t('campaigns.default'), tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'actions', label: this.$t('campaigns.actions'), tdClass: 'align-middle'}
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
                let rsp = await this.$xhr.get('/campaigns', {params: {p: page - 1}})
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
            upload(index) {
                this.$refs[`uploadDialog${index}`].show()
            },
            showMessages(messages) {
                // ensures same message triggers new alert
                this.messages = []
                this.$nextTick(() => this.messages = messages || [])
            },
            itemUploaded(data) {
                this.showMessages(data.messages);
            }
        },
        mounted() {
            this.reloadPage()
        },
        components: {
            BContainer, BRow, BCol, BAlert, BButton, BPagination, BTable, BButtonGroup,
            EditDialog, UploadDialog, CreateDialog
        }
    }
</script>

<style scoped>
    >>> .b-table tr.deleted td {
        text-decoration-line: line-through;
    }
</style>