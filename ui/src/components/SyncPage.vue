<template>
    <b-container>
        <b-row class="align-items-center">
            <b-col class="col-auto">
                <h1><fa-icon icon="sync"/> Sync</h1>
            </b-col>
            <b-col v-if="$can('MANAGE_SYNC')">
                <b-button-toolbar>
                    <b-button-group class="mr-2">
                        <b-button variant="primary" @click="pause" :class="{disabled: !status.scheduled}">
                            <fa-icon icon="pause"/> Pause
                        </b-button>
                        <b-button variant="primary" @click="start" :class="{disabled: status.scheduled || !status.schedule}">
                            <fa-icon icon="play"/> Resume
                        </b-button>
                    </b-button-group>
                    <b-button-group>
                        <b-button variant="primary" @click="build" :class="{disabled: status.running}">
                            <fa-icon icon="bolt"/> Build Now
                        </b-button>
                    </b-button-group>
                </b-button-toolbar>
            </b-col>
        </b-row>
        <b-row>
            <b-col>
                <p class="text-center">
                    <fa-icon icon="stopwatch"/> Next run: {{ nextRunFormatted }}
                </p>
            </b-col>
        </b-row>
        <b-row>
            <b-col>
                <b-table :items="[status.task]" :fields="fields" >
                    <template slot="started" slot-scope="data">{{ data.value | formatDate }}</template>
                    <template slot="finished" slot-scope="data">{{ data.value | formatDate }}</template>
                    <template slot="descriptor" slot-scope="data">
                        <b-button v-if="$can('EXPORT_SYNC')" variant="primary" :class="{disabled: !status.downloadable}" href="/sync/export" download>
                            <fa-icon icon="download"/>
                        </b-button>
                        {{ data.value }}
                    </template>
                </b-table>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import SockJS from 'sockjs-client'
    import Stomp from 'webstomp-client'
    export default {
        name: 'sync-page',
        data() {
            return {
                fields: [
                    {key: 'started', label: 'Started', tdClass: 'align-middle'},
                    {key: 'finished', label: 'Finished', tdClass: 'align-middle'},
                    {key: 'descriptor', label: 'Content', tdClass: 'align-middle'},
                    {key: 'itemCount', label: 'Items', tdClass: 'align-middle'}
                ],
                status: {
                    scheduled: false,
                    running: false,
                    downloadable: false,
                    task: {}
                }
            }
        },
        computed: {
            nextRunFormatted() {
                return this.status.nextRun? `${this.status.nextRun} minutes` : 'none'
            }
        },
        methods: {
            async update(data) {
                if (data) {
                    this.status = data
                } else {
                    let rsp = await this.$xhr.get('/sync')
                    this.status = rsp.data
                }
            },
            async build() {
                let rsp = await this.$xhr.get('/sync/run')
                this.update(rsp.data)
            },
            async start() {
                let rsp = await this.$xhr.get('/sync/start')
                this.update(rsp.data)
            },
            async pause() {
                let rsp = await this.$xhr.get('/sync/pause')
                this.update(rsp.data)
            },
            wsconnect() {
                let socket = new SockJS('/stomp')
                let stomp = Stomp.over(socket, {version: Stomp.VERSIONS.V1_2, debug: null})
                stomp.connect({}, () => {
                    this.stomp = stomp
                    stomp.subscribe('/topic/syncstatus', msg => this.update(JSON.parse(msg.body)))
                })
            }
        },
        mounted() {
            this.update()
            this.wsconnect()
        },
        beforeDestroy() {
            if (this.stomp) {
                this.stomp.disconnect()
            }
        }
    }
</script>
