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
        </b-row>
        <b-row v-if="totalItems > pageSize">
            <b-col>
                <b-pagination align="center" v-model="currentPage" :total-rows="totalItems" :per-page="pageSize" @change="loadPage"/>
            </b-col>
        </b-row>
        <b-row>
            <b-col>
                <b-table :items="items" :fields="fields" show-empty :empty-text="$t('table.empty')">
                    <template slot="lastLogin" slot-scope="data">{{data.value|formatDateTime}}</template>
                    <template slot="actions" slot-scope="data">
                        <b-button-group>
                            <b-button v-if="$can('UPLOAD_CAMPAIGNS')" variant="outline-primary" @click="upload(data.index)">
                                <fa-icon icon="upload"/>
                            </b-button>
                            <b-button v-if="$can('DOWNLOAD_CAMPAIGNS')" variant="outline-primary" :href="`/campaign/export/${data.item.name}`" download>
                                <fa-icon icon="download"/>
                            </b-button>
                        </b-button-group>
                        <upload-dialog :ref="`uploadDialog${data.index}`" :name="data.item.name" @ok="itemUploaded" v-if="$can('UPLOAD_CAMPAIGNS')" />
                    </template>
                </b-table>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import {BContainer, BRow, BCol, BAlert, BButton, BPagination, BTable, BButtonGroup} from 'bootstrap-vue'
    import UploadDialog from './UploadDialog'
    export default {
        name: 'devices-page',
        data() {
            return {
                fields: [
                    {key: 'name', label: this.$t('devices.name'), tdClass: 'align-middle'},
                    {key: 'actions', label: this.$t('devices.actions'), tdClass: 'align-middle'}
                ],
                errors: [],
                messages: [],
                totalItems: 0,
                pageSize: 0,
                currentPage: 1,
                items: [{name: 'default'}]
            }
        },
        methods: {
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
        components: {
            BContainer, BRow, BCol, BAlert, BButton, BPagination, BTable, BButtonGroup, UploadDialog
        }
    }
</script>

<style scoped>
    >>> .b-table tr.deleted td {
        text-decoration-line: line-through;
    }
</style>