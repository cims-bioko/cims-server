<template>
    <b-navbar type="dark" variant="info" class="bg-dark navbar-expand-lg fixed-top" role="navigation">
        <b-navbar-brand href="#">
            <img src="/img/logo.svg" class="img-fluid"/>
            CIMS Server
        </b-navbar-brand>
        <b-navbar-nav class="mr-auto">
            <b-nav-item :to="{name:'home'}" exact>Home</b-nav-item>
            <b-nav-item :to="{name:'another'}">Another</b-nav-item>
        </b-navbar-nav>
    </b-navbar>
</template>

<script>
    export default {
        name: 'app-header'
    }
</script>

<style scoped>
    .navbar-brand img {
        width: 3rem;
        height: 3rem;
    }
</style>