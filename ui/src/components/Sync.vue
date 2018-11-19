<template>
    <div>
        <div class="row align-items-center">
            <h1 class="col col-auto">
                <fa-icon icon="sync"/> Sync
            </h1>
            <div class="col">
                <div class="btn-toolbar" role="toolbar">
                    <div class="btn-group mr-2">
                        <a class="btn btn-primary" href="#" @click="pause" :class="{disabled: !status.scheduled}">
                            <fa-icon icon="pause"/> Pause
                        </a>
                        <a class="btn btn-primary" href="#" @click="start" :class="{disabled: status.scheduled || !status.schedule}">
                            <fa-icon icon="play"/> Resume
                        </a>
                    </div>
                    <div class="btn-group">
                        <a class="btn btn-primary" href="#" @click="build" :class="{disabled: status.running}">
                            <fa-icon icon="bolt"/> Build Now
                        </a>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <p class="text-center">
                    <fa-icon icon="stopwatch"/> Next run: {{ status.nextRun || 'none' }}
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
            }
        },
        mounted() {
            this.update()
        },
        filters: {
            formatDate(v) {
                if (v) {
                    return moment().zone(v).format('MM/DD/YYYY hh:mm:ss z')
                }
            }
        }
    }
</script>
