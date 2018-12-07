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
                <h1><fa-icon icon="user"/> {{$t('nav.fieldworkers')}}</h1>
            </b-col>
            <b-col v-if="$can('CREATE_FIELDWORKERS')">
                <b-button variant="primary" @click="createItem"><fa-icon icon="plus"/> {{$t('fieldworkers.create')}}</b-button>
                <create-dialog ref="createDialog" @ok="itemCreated"/>
            </b-col>
            <b-col>
                <search-box :placeholder="$t('fieldworkers.searchph')" v-model="searchQuery" @search="search" />
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
                    <template slot="actions" slot-scope="data">
                        <b-button-group>
                            <b-button v-if="$can('EDIT_FIELDWORKERS')" variant="outline-primary" @click="editItem(data.index)" :disabled="data.item.deleted">
                                <fa-icon icon="edit"/>
                            </b-button>
                            <b-button v-if="!data.item.deleted && $can('DELETE_FIELDWORKERS')" variant="outline-primary" @click="deleteItem(data.item.uuid)">
                                <fa-icon icon="trash-alt"/>
                            </b-button>
                            <b-button v-if="data.item.deleted && $can('RESTORE_FIELDWORKERS')" variant="outline-primary" @click="restoreItem(data.item.uuid)">
                                <fa-icon icon="undo-alt"/>
                            </b-button>
                        </b-button-group>
                        <edit-dialog v-if="$can('EDIT_FIELDWORKERS') && !data.item.deleted" :ref="`editDialog${data.index}`" v-bind="data.item" @ok="itemEdited"/>
                    </template>
                </b-table>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import CreateDialog from './CreateDialog'
    import EditDialog from './EditDialog'
    import SearchBox from '../SearchBox'
    export default {
        name: 'fieldworkers-page',
        data() {
            return {
                fields: [
                    {key: 'extId', label: this.$t("fieldworkers.id"), tdClass: 'align-middle'},
                    {key: 'firstName', label: this.$t("fieldworkers.firstName"), tdClass: 'align-middle'},
                    {key: 'lastName', label: this.$t("fieldworkers.lastName"), tdClass: 'align-middle'},
                    {key: 'actions', label: this.$t("fieldworkers.actions"), tdClass: 'align-middle'},
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
                if (this.searchQuery) { params.q = this.searchQuery }
                let rsp = await this.$xhr.get('/fieldworkers', {params: params})
                let data = rsp.data
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
                let response = await this.$xhr.delete(`/fieldworker/${uuid}`)
                this.showMessages(response.data.messages)
                this.reloadPage()
            },
            async restoreItem(uuid) {
                let response = await this.$xhr.put(`/fieldworker/restore/${uuid}`)
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
            CreateDialog,
            EditDialog,
            SearchBox
        }
    }
</script>

<style scoped>
    >>> .b-table tr.deleted td {
        text-decoration-line: line-through;
    }
</style>
