<template>
    <footer class="text bg-dark text-light fixed-bottom footer" role="contentinfo">
        <b-row class="row">
            <b-col class="col text-center">{{$t('app.copyright')}}</b-col>
        </b-row>
    </footer>
</template>

<script>
    import {BRow, BCol} from 'bootstrap-vue'
    export default {
        name: 'app-footer',
        components: {
            BRow, BCol
        }
    }
</script>

<style scoped>
footer {
    padding: 1rem;
}
</style>