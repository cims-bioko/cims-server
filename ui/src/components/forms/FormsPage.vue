<template>
    <b-container>
        <b-row class="align-items-center">
            <b-col class="col-auto">
                <h1><fa-icon icon="file-alt"/> {{$t('nav.forms')}}</h1>
            </b-col>
            <b-col v-if="$can('FORM_UPLOAD') || $can('FORM_UPLOAD_XLS')">
                <b-button variant="primary" @click="showUploadDialog"><fa-icon icon="plus"/> {{$t('forms.add')}}</b-button>
                <upload-dialog ref="uploadDialog" @formUploaded="formUploaded"/>
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
                    <template slot="formId.version" slot-scope="data">
                        {{data.value}}
                        <b-button v-if="$can('EXPORT_FORMS')" variant="primary" :href="`/forms/export/${data.item.formId.id}/${data.value}`" download>
                            <fa-icon icon="download"/>
                        </b-button>
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
                </b-table>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import bContainer from 'bootstrap-vue/es/components/layout/container'
    import bRow from 'bootstrap-vue/es/components/layout/row'
    import bCol from 'bootstrap-vue/es/components/layout/col'
    import bButton from 'bootstrap-vue/es/components/button/button'
    import bPagination from 'bootstrap-vue/es/components/pagination/pagination'
    import bTable from 'bootstrap-vue/es/components/table/table'
    import UploadDialog from './UploadDialog'
    export default {
        name: 'forms-page',
        data() {
            return {
                fields: [
                    {key: 'formId.id', label: this.$t('forms.id'), tdClass: 'align-middle'},
                    {key: 'formId.version', label: this.$t('forms.version'), tdClass: 'align-middle'},
                    {key: 'downloads', label: this.$t('forms.downloads'), tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'submissions', label: this.$t('forms.submissions'), tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'uploaded', label: this.$t('forms.uploaded'), tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'lastSubmission', label: this.$t('forms.lastSubmission'), tdClass: 'align-middle text-center', thClass: 'text-center'},
                ],
                totalItems: 0,
                pageSize: 0,
                currentPage: 1,
                items: []
            }
        },
        methods: {
            async loadPage(page) {
                let rsp = await this.$xhr.get('/forms', {params: {p: page - 1}})
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
            }
        },
        mounted() {
            this.reloadPage()
        },
        components: {
            bContainer, bRow, bCol, bButton, bPagination, bTable,
            UploadDialog
        }
    }
</script>
