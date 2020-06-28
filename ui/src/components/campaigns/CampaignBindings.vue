<template>
    <b-container>
        <b-row>
            <b-col>
                <template v-for="(message, index) of messages">
                    <b-alert variant="success" :show="15" :key="`message-${index}`" fade>{{message}}</b-alert>
                </template>
            </b-col>
        </b-row>
        <b-row>
            <b-col>
                <b-table :items="decoratedItems" :fields="fields" show-empty :empty-text="$t('table.empty')">
                    <template slot="actions" slot-scope="data">
                        <b-button-group>
                            <b-button variant="outline-primary" @click="confirmReprocess(data.index)">
                                <fa-icon icon="cogs"/> {{$t("campaigns.reprocessmodal.reprocess")}}
                            </b-button>
                        </b-button-group>
                        <reprocess-dialog :ref="`reprocessDialog${data.index}`" :campaign="campaign" :binding="data.item.binding" @ok="itemReprocessed" />
                    </template>
                </b-table>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import {BContainer, BRow, BCol, BTable, BButton, BButtonGroup, BAlert} from 'bootstrap-vue'
    import ReprocessDialog from "./ReprocessDialog"
    export default {
        name: 'campaign-bindings',
        props: {
            campaign: {
                type: String,
                required: true
            }
        },
        data() {
            return {
                fields: [
                    {key: 'binding', label: this.$t('campaigns.bindingName'), tdClass: 'align-middle'},
                    {key: 'actions', label: this.$t('campaigns.actions'), tdClass: 'align-middle'}
                ],
                messages: [],
                bindings: []
            }
        },
        methods: {
            async load() {
                let rsp = await this.$xhr.get(`/campaign/${this.campaign}/bindings`)
                this.bindings = rsp.data
            },
            confirmReprocess(index) {
                this.$refs[`reprocessDialog${index}`].show()
            },
            itemReprocessed(rsp) {
                this.showMessages([this.$t('campaigns.binding.reprocessed', [rsp.affected])])
            },
            showMessages(messages) {
                // ensures same message triggers new alert
                this.messages = []
                this.$nextTick(() => this.messages = messages || [])
            },
        },
        mounted() {
            this.load()
        },
        computed: {
            decoratedItems() {
                return this.bindings.map(binding => ({'binding': binding}))
            }
        },
        components: {
            BContainer, BRow, BCol, BTable, ReprocessDialog, BButton, BButtonGroup, BAlert
        }
    }
</script>