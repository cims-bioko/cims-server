<template>
    <b-container>
        <b-row class="align-items-center">
            <b-col class="col-auto">
                <h1><fa-icon icon="file-alt"/> Forms</h1>
            </b-col>
            <b-col>
                <b-button variant="primary" @click="showUploadDialog"><fa-icon icon="plus"/> Add</b-button>
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
                <b-table :items="items" :fields="fields" show-empty>
                    <template slot="formId.version" slot-scope="data">
                        {{data.value}}
                        <b-button variant="primary" :href="`/forms/export/${data.item.formId.id}/${data.value}`" download>
                            <fa-icon icon="download"/>
                        </b-button>
                    </template>
                    <template slot="downloads" slot-scope="data">
                        <b-button variant="outline-primary" @click="toggleDownload(data.item)">
                            <fa-icon :icon="data.value? 'check' : 'times'"/>
                        </b-button>
                    </template>
                    <template slot="submissions" slot-scope="data">
                        <b-button variant="outline-primary" @click="toggleSubmission(data.item)">
                            <fa-icon :icon="data.value? 'check' : 'times'"/>
                        </b-button>
                    </template>
                    <template slot="uploaded" slot-scope="data">{{ data.value | formatDate }}</template>
                    <template slot="lastSubmission" slot-scope="data">{{ data.value | formatDate }}</template>
                </b-table>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import UploadDialog from './UploadDialog'
    export default {
        name: 'forms-page',
        data() {
            return {
                fields: [
                    {key: 'formId.id', label: 'Id', tdClass: 'align-middle'},
                    {key: 'formId.version', label: 'Version', tdClass: 'align-middle'},
                    {key: 'downloads', tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'submissions', tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'uploaded', tdClass: 'align-middle text-center', thClass: 'text-center'},
                    {key: 'lastSubmission', tdClass: 'align-middle text-center', thClass: 'text-center'},
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
            UploadDialog
        }
    }
</script>
