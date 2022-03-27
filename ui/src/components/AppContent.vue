<template>
    <div id="content">
        <index-alert />
        <router-view />
    </div>
</template>

<script>
import IndexAlert from './search/IndexAlert'
export default {
  name: 'app-content',
  components: {
    IndexAlert
  }
}
</script>

<style scoped>
    #content {
        padding-top: 8rem;
        padding-bottom: 5rem;
    }
</style>