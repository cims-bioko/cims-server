<template>
    <b-modal ref="modal" @show="init" @hide="deinit" ok-only>
        <template slot="modal-title">{{$t('campaigns.syncmodal.title')}}</template>
        <b-container fluid v-if="!enabled">
          <b-row class="text-center">
            <b-col>
              {{$t("campaigns.syncmodal.disabled")}}
            </b-col>
          </b-row>
        </b-container>
        <b-container fluid v-if="enabled">
            <b-row v-if="inProgress">
                <b-col><b-progress :value="progress" striped animated/></b-col>
            </b-row>
            <b-row class="text-center" v-if="$can('MANAGE_SYNC') && !inProgress">
                <b-col>
                    <b-button variant="outline-primary" size="lg" class="w-75" @click="start" :disabled="status !== 'PAUSED'">
                        <fa-icon icon="play"/>
                    </b-button>
                </b-col>
                <b-col>
                    <b-button variant="outline-primary" size="lg" class="w-75" @click="pause" :disabled="status !== 'SCHEDULED'">
                        <fa-icon icon="pause"/>
                    </b-button>
                </b-col>
            </b-row>
            <b-row class="text-center pt-2 pb-2 mx-auto" v-if="!inProgress">
                <b-col>{{$t('campaigns.syncmodal.nextrun', [nextRun])}}</b-col>
            </b-row>
            <b-row class="text-center pt-2 pb-2 mx-auto" v-if="!inProgress">
                <b-col>
                    <b-button variant="outline-primary" size="lg" class="w-75" @click="buildNow" :disabled="status === 'RUNNING'">
                        <fa-icon icon="bolt"/>
                    </b-button>
                </b-col>
            </b-row>
            <b-row class="text-center pt-2 pb-2 mx-auto" v-if="contentHash != null">
                <b-col>{{$t('campaigns.syncmodal.contenthash', [contentHash])}}</b-col>
            </b-row>
            <b-row class="text-center" v-if="$can('EXPORT_SYNC') && contentHash != null">
                <b-col>
                    <b-button variant="outline-primary" size="lg" class="w-75" :href="`/sync/${uuid}/export`" download>
                        <fa-icon icon="download"/>
                    </b-button>
                </b-col>
            </b-row>
        </b-container>
        <template slot="modal-ok">{{$t('campaigns.syncmodal.close')}}</template>
    </b-modal>
</template>

<script>
    import {BButton, BCol, BContainer, BModal, BRow, BProgress} from 'bootstrap-vue'
    import SockJS from 'sockjs-client'
    import Stomp from 'webstomp-client'
    export default {
        name: 'sync-dialog',
        props: {
            uuid: {
                type: String,
                required: true
            }
        },
        data() {
            return {
                enabled: false,
                nextRun: null,
                status: null,
                contentHash: null,
                progress: 0
            }
        },
        methods: {
            show() {
                this.$refs.modal.show()
            },
            hide() {
                this.$refs.modal.hide()
            },
            wsconnect() {
                let socket = new SockJS('/stomp')
                let stomp = Stomp.over(socket, {version: Stomp.VERSIONS.V1_2, debug: null})
                stomp.connect({},
                    () => {
                        this.stomp = stomp
                        stomp.subscribe(`/topic/sync/${this.uuid}`, msg => this.handleMessage(JSON.parse(msg.body)))
                    })
            },
            wsdisconnect() {
                if (this.stomp) {
                    this.stomp.disconnect()
                }
            },
            handleMessage(msg) {
                this.updateData(msg)
            },
            async init() {
                await this.load(this.uuid)
                this.wsconnect()
            },
            async load(uuid) {
                let response = await this.$xhr.get(`/sync/${uuid}`)
                if (response.status === 200) this.updateData(response.data)
            },
            updateData(data) {
                this.enabled = true
                this.nextRun = data.nextRun
                this.status = data.status
                this.contentHash = data.contentHash
                this.progress = parseInt(data.progress)
            },
            deinit() {
                this.wsdisconnect()
            },
            async start() {
                let response = await this.$xhr.get(`/sync/${this.uuid}/resume`)
                this.updateData(response.data)
            },
            async pause() {
                let response = await this.$xhr.get(`/sync/${this.uuid}/pause`)
                this.updateData(response.data)
            },
            async buildNow() {
                let response = await this.$xhr.get(`/sync/${this.uuid}/run`)
                this.updateData(response.data)
            }
        },
        computed: {
            inProgress() {
                return this.status === "RUNNING"
            }
        },
        components: {
            BModal, BButton, BContainer, BRow, BCol, BProgress
        }
    }
</script>