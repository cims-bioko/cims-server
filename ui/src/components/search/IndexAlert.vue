<template>
  <b-container v-if="showAlert">
    <b-row>
      <b-col>
        <b-alert ref="alert" :variant="alertVariant" dismissible>{{ alertMessage }}</b-alert>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import {BContainer, BRow, BCol, BAlert} from 'bootstrap-vue'
export default {
  name: 'index-alert',
  data() {
    return {
      status: {
        running: false,
        completedAt: null,
      }
    }
  },
  methods: {
    async loadData() {
      let response = await this.$xhr.get('/indexing')
      this.status = response.data
      this.$refs.alert.show = true
    },
  },
  mounted() {
    this.loadData()
  },
  computed: {
    needsIndexing() {
      return this.status.completedAt == null
    },
    isRunning() {
      return this.status.running
    },
    showAlert() {
      return this.needsIndexing || this.isRunning
    },
    alertMessage() {
      return this.needsIndexing? this.$t('rebuildindex.needsindex') : this.$t('rebuildindex.indexrun')
    },
    alertVariant() {
      return this.needsIndexing && !this.isRunning? 'warning' : 'info'
    }
  },
  components: {
    BContainer, BRow, BCol, BAlert
  }
}
</script>

<style scoped>
</style>