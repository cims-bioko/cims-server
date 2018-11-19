<template>
    <div>
        <div class="row align-items-center">
            <h1 class="col col-auto">
                <fa-icon icon="sync"/> Sync
            </h1>
            <div class="col">
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
            </div>
        </div>
        <div class="row">
            <div class="col">
                <p class="text-center">
                    <fa-icon icon="stopwatch"/> Next run: {{ nextRunFormatted }}
                </p>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <table class="table">
                    <thead>
                    <tr>
                        <th class="d-none d-lg-table-cell">Started</th>
                        <th class="d-none d-sm-table-cell">Ended</th>
                        <th>Content</th>
                        <th class="d-none d-xl-table-cell">Items</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="align-middle d-none d-lg-table-cell">{{ status.task.started | formatDate }}</td>
                        <td class="align-middle d-none d-sm-table-cell">{{ status.task.finished | formatDate }}</td>
                        <td class="align-middle">
                            <a class="btn btn-primary" href="/sync/export" download :class="{disabled: !status.downloadable}">
                                <fa-icon icon="download"/>
                            </a>
                            {{ status.task.descriptor }}
                        </td>
                        <td class="align-middle d-none d-xl-table-cell">{{ status.task.itemCount }}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<script>
    import axios from 'axios'
    import moment from 'moment'
    import SockJS from 'sockjs-client'
    import Stomp from 'webstomp-client'

    export default {
        name: 'sync',
        data() {
            return {
                status: {
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
            update(data) {
                if (data) {
                    this.status = data
                } else {
                    axios.get('/sync').then(rsp => this.status = rsp.data)
                }
            },
            build() {
                axios.get('/sync/run').then(rsp => this.update(rsp.data))
            },
            start() {
                axios.get('/sync/start').then(rsp => this.update(rsp.data))
            },
            pause() {
                axios.get('/sync/pause').then(rsp => this.update(rsp.data))
            },
            wsconnect() {
                const socket = new SockJS('/stomp');
                const stomp = Stomp.over(socket, {version: Stomp.VERSIONS.V1_2, debug: null});
                stomp.connect({},
                    () => stomp.subscribe('/topic/syncstatus', msg => this.update(JSON.parse(msg.body))))
            }
        },
        mounted() {
            this.update()
            this.wsconnect()
        },
        filters: {
            formatDate(v) {
                if (v) {
                    return moment(v).utcOffset(v).format('MM/DD/YYYY hh:mm:ss z')
                }
            }
        }
    }
</script>
