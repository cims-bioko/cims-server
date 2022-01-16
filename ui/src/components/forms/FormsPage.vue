<template>
    <b-container>
        <b-row>
            <b-col>
                <template v-for="(message, index) of messages">
                    <b-alert variant="success" :show="15" :key="`message-${index}`" fade>{{message}}</b-alert>
                </template>
            </b-col>
        </b-row>
        <b-row class="align-items-center">
            <b-col class="col-auto">
                <h1><fa-icon icon="file-alt"/> {{$t('nav.forms')}}</h1>
            </b-col>
            <b-col v-if="$can('FORM_UPLOAD') || $can('FORM_UPLOAD_XLS')">
                <b-button variant="primary" @click="showUploadDialog"><fa-icon icon="plus"/> {{$t('forms.add')}}</b-button>
                <upload-dialog ref="uploadDialog" @formUploaded="formUploaded"/>
            </b-col>
            <b-col>
                <search-box :placeholder="$t('forms.searchph')" v-model="searchQuery" @search="search" />
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
                    <template slot="formId" slot-scope="data">
                        {{$t("forms.nameFormat", [data.value.id, data.value.version])}}
                    </template>
                    <template slot="downloads" slot-scope="data">
                        <b-button :disabled="!$can('MANAGE_FORMS')" variant="outline-primary" @click="toggleDownload(data.item)">
                            <fa-icon :icon="data.value? 'check' : 'times'"/>
                        </b-button>
                    </template>
                    <template slot="submissions" slot-scope="data">
                        <b-button :disabled="!$can('MANAGE_FORMS')" variant="outline-primary" @click="toggleSubmission(data.item)">
                            <fa-icon :icon="data.value? 'check' : 'times'"/>
                        </b-button>
                    </template>
                    <template slot="uploaded" slot-scope="data">{{data.value|formatDateTime}}</template>
                    <template slot="lastSubmission" slot-scope="data">{{data.value|formatDateTime}}</template>
                    <template slot="actions" slot-scope="data">
                        <b-button-group>
                            <b-button v-if="$can('EXPORT_FORMS')" variant="outline-primary" :href="`/forms/export/${data.item.formId.id}/${data.item.formId.version}`" download>
                                <fa-icon icon="download"/>
                            </b-button>
                            <b-button v-if="$can('WIPE_FORM_SUBMISSIONS')" variant="outline-primary" @click="showResetDialog(data.index)">
                                <fa-icon icon="folder-minus"/>
                            </b-button>
                            <b-button v-if="$can('DELETE_FORMS')" variant="outline-primary" @click="showDeleteDialog(data.index)">
                                <fa-icon icon="trash-alt"/>
                            </b-button>
                        </b-button-group>
                        <form-reset-dialog v-if="$can('WIPE_FORM_SUBMISSIONS')" :ref="`resetDialog${data.index}`"
                                           :id="data.item.formId.id" :version="data.item.formId.version"
                                           @ok="formReset"/>
                        <form-delete-dialog v-if="$can('DELETE_FORMS')" :ref="`deleteDialog${data.index}`"
                                           :id="data.item.formId.id" :version="data.item.formId.version"
                                           @ok="formReset"/>
                    </template>
                </b-table>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import {BAlert, BContainer, BRow, BCol, BButton, BButtonGroup, BPagination, BTable} from 'bootstrap-vue'
    import UploadDialog from './UploadDialog'
    import FormResetDialog from './ResetDialog'
    import FormDeleteDialog from './DeleteDialog'
    import SearchBox from "../SearchBox";
    export default {
        name: 'forms-page',
        data() {
            return {
                fields: [
                    {key: 'formId', label: this.$t('forms.label'), tdClass: 'align-middle', thClass: 'text-center'},
                    {key: 'actions', label: this.$t('forms.actions'), tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'downloads', label: this.$t('forms.downloads'), tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'submissions', label: this.$t('forms.submissions'), tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'uploaded', label: this.$t('forms.uploaded'), tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'lastSubmission', label: this.$t('forms.lastSubmission'), tdClass: 'align-middle text-center', thClass: 'text-center'}
                ],
                totalItems: 0,
                pageSize: 0,
                currentPage: 1,
                items: [],
                messages: [],
                searchQuery: ''
            }
        },
        methods: {
            async loadPage(page) {
                let params = {p: page - 1}
                if (this.searchQuery) {
                    params.q = this.searchQuery
                }
                let rsp = await this.$xhr.get('/forms', {params: params})
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
            async manageForm(formId, downloads, submissions) {
                let data = {downloads, submissions}, config = {transformResponse: []}
                await this.$xhr.patch(`/forms/manage/${formId.id}/${formId.version}`, data, config)
                this.reloadPage()
            },
            toggleDownload({formId, downloads, submissions}) {
                this.manageForm(formId, !downloads, submissions)
            },
            toggleSubmission({formId, downloads, submissions}) {
                this.manageForm(formId, downloads, !submissions)
            },
            showUploadDialog() {
                this.$refs.uploadDialog.show()
            },
            formUploaded() {
                this.reloadPage()
            },
            showResetDialog(index) {
                this.$refs[`resetDialog${index}`].show()
            },
            showDeleteDialog(index) {
                this.$refs[`deleteDialog${index}`].show()
            },
            formReset(rsp) {
                this.showMessages(rsp.messages)
                this.reloadPage()
            },
            showMessages(messages) {
                // ensures same message triggers new alert
                this.messages = []
                this.$nextTick(() => this.messages = messages || [])
            },
            search() {
                this.reloadPage()
            }
        },
        mounted() {
            this.reloadPage()
        },
        components: {
            BAlert, BContainer, BRow, BCol, BButton, BButtonGroup, BPagination, BTable, UploadDialog, FormResetDialog,
            FormDeleteDialog, SearchBox
        }
    }
</script>
