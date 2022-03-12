<template>
  <b-popover :target="target" placement="bottom" triggers="hover">
    {{created|formatDateTime}}
    {{error}}
  </b-popover>
</template>

<script>
import {BPopover} from "bootstrap-vue"
export default {
  name: 'submission-error',
  props: {
    id: {
      type: String,
      required: true
    },
    target: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      created: null,
      error: ''
    }
  },
  methods: {
    async load() {
      try {
        let rsp = await this.$xhr.get(`/submissions/error/${this.id}`)
        const data = rsp.data
        this.created = data.created
        this.error = data.error
      } catch (err) {
        this.error = err
      }
    },
  },
  mounted() {
    this.load()
  },
  components: {
    BPopover
  }
}
</script>

<style scoped>
</style>