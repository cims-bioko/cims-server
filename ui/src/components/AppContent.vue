<template>
    <div id="content">
        <router-view />
    </div>
</template>

<script>
export default {
    name: 'app-content'
}
</script>

<style scoped>
    #content {
        padding-top: 8rem;
        padding-bottom: 5rem;
    }
</style>