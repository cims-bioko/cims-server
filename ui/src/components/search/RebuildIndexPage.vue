<template>
  <b-container>
    <b-row>
      <b-col>
        <template v-for="(message, index) of messages">
          <b-alert variant="success" :show="15" :key="`message-${index}`" fade>{{ message }}</b-alert>
        </template>
      </b-col>
    </b-row>
    <b-row class="align-items-center">
      <b-col class="col-auto">
        <h1>
          <fa-icon icon="file-alt"/>
          {{ $t('nav.rebuildindex') }}
        </h1>
      </b-col>
    </b-row>
    <b-row class="status" v-if="status.running">
      <b-col>{{ $t('rebuildindex.indexing') }}</b-col>
    </b-row>
    <b-row class="status" v-if="status.completedAt != null">
      <b-col>{{ completedAt }}</b-col>
    </b-row>
    <b-row class="status" v-if="!status.running && status.completedAt == null">
      <b-col>{{ $t('rebuildindex.needsindexing') }}</b-col>
    </b-row>
    <b-row class="status" v-if="status.running">
      <b-col>
        <b-progress :max="status.todo" height="2rem">
          <b-progress-bar :value="status.processed">
            <span>
              <strong>{{ progress }}</strong>
            </span>
          </b-progress-bar>
        </b-progress>
      </b-col>
    </b-row>
    <b-row class="speed" v-if="status.running">
      <b-col>{{ speed }}</b-col>
    </b-row>
    <b-row>
      <b-col>
        <b-button :disabled="status.running" @click="requestIndexing" variant="outline-primary">{{ $t('rebuildindex.reindex') }}</b-button>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import {BAlert, BButton, BCol, BContainer, BRow, BProgress, BProgressBar} from 'bootstrap-vue'
import SockJS from 'sockjs-client'
import Stomp from 'webstomp-client'
import {formatDateTime, formatLargeNumber} from '@/formatting'

export default {
  name: 'rebuild-index-page',
  data() {
    return {
      messages: [],
      status: {
        running: false,
        elapsed: 0,
        processed: 0,
        todo: 0,
        completedAt: null,
        dps: 0.0,
        pctComplete: 0.0
      }
    }
  },
  methods: {
    async loadData() {
      let response = await this.$xhr.get('/indexing')
      this.status = response.data
    },
    async requestIndexing() {
      let response = await this.$xhr.post('/indexing')
      this.status = response.data
    },
    wsconnect() {
      let socket = new SockJS('/stomp')
      let stomp = Stomp.over(socket, {version: Stomp.VERSIONS.V1_2, debug: null})
      stomp.connect({},
          () => {
            this.stomp = stomp
            stomp.subscribe('/topic/indexing', msg => this.handleMessage(JSON.parse(msg.body)))
          })
    },
    showMessages(messages) {
      // ensures same message triggers new alert
      this.messages = []
      this.$nextTick(() => this.messages = messages || [])
    },
    handleMessage(msg) {
      this.status = msg
    }
  },
  computed: {
    completedAt() {
      return this.$t('rebuildindex.lastcompleted', [formatDateTime(this.status.completedAt)])
    },
    progress() {
      return this.$t('rebuildindex.progress', [formatLargeNumber(this.processed), formatLargeNumber(this.todo), this.percentComplete])
    },
    processed() {
      return this.status.processed
    },
    todo() {
      return this.status.todo
    },
    percentComplete() {
      return this.$t('rebuildindex.percentcomplete', [this.status.pctComplete.toFixed(0)])
    },
    speed() {
      return this.$t('rebuildindex.docspersec', [this.status.dps.toFixed(0)])
    }
  },
  mounted() {
    this.loadData()
    this.wsconnect()
  },
  components: {
    BAlert, BContainer, BRow, BCol, BButton, BProgress, BProgressBar
  }
}
</script>

<style scoped>
  .status, .speed, .btn {
    margin-top: 0.5rem;
  }
</style>