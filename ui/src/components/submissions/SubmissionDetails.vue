<template>
  <b-container>
    <b-progress v-if="inProgress" :value="100" striped animated/>
    <pre lang="xml">{{content}}</pre>
  </b-container>
</template>

<script>
import {BProgress, BContainer} from "bootstrap-vue"
export default {
  name: 'submission-details',
  props: {
    id: {
      type: String,
      required: true
    },
  },
  data() {
    return {
      content: '',
      inProgress: false
    }
  },
  methods: {
    async load() {
      try {
        this.inProgress = true
        let rsp = await this.$xhr.get(`/submissions/xml/${this.id}`)
        this.content = rsp.data
        this.inProgress = false
      } catch (err) {
        this.error = err
      }
    },
  },
  mounted() {
    this.load()
  },
  components: {
    BProgress, BContainer
  }
}
</script>

<style scoped>
  pre[lang='xml'] {
    background-color: #efefef;
    padding: 1.5em;
  }
</style>