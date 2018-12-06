<template>
    <footer class="text bg-dark text-light fixed-bottom footer" role="contentinfo">
        <b-row class="row">
            <b-col class="col text-center">{{$t('app.copyright')}}</b-col>
        </b-row>
    </footer>
</template>

<script>
export default {
    name: 'app-footer'
}
</script>

<style scoped>
footer {
    padding: 1rem;
}
</style>