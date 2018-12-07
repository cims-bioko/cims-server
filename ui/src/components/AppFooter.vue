<template>
    <footer class="text bg-dark text-light fixed-bottom footer" role="contentinfo">
        <b-row class="row">
            <b-col class="col text-center">{{$t('app.copyright')}}</b-col>
        </b-row>
    </footer>
</template>

<script>
    import bRow from 'bootstrap-vue/es/components/layout/row'
    import bCol from 'bootstrap-vue/es/components/layout/col'
    export default {
        name: 'app-footer',
        components: {
            bRow, bCol
        }
    }
</script>

<style scoped>
footer {
    padding: 1rem;
}
</style>