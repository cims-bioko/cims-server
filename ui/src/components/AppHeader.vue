<template>
    <b-navbar type="dark" variant="info" class="bg-dark navbar-expand-lg fixed-top" role="navigation">
        <b-navbar-brand href="#">
            <img src="/img/logo.svg" class="img-fluid"/>
            CIMS Server
        </b-navbar-brand>
        <b-navbar-nav class="mr-auto">
            <b-nav-item :to="{name:'home'}" exact><fa-icon icon="home"/> Home</b-nav-item>
            <b-nav-item :to="{name:'forms'}"><fa-icon icon="file-alt"/> Forms</b-nav-item>
            <b-nav-item :to="{name:'sync'}"><fa-icon icon="sync"/> Sync</b-nav-item>
            <b-nav-item :to="{name:'backups'}"><fa-icon icon="business-time"/> Backups</b-nav-item>
            <b-nav-item :to="{name:'users'}"><fa-icon icon="user"/> Users</b-nav-item>
            <b-nav-item :to="{name:'roles'}"><fa-icon icon="users"/> Roles</b-nav-item>
            <b-nav-item :to="{name:'fieldworkers'}"><fa-icon icon="user"/> Fieldworkers</b-nav-item>
        </b-navbar-nav>
    </b-navbar>
</template>

<script>
    export default {
        name: 'app-header',
    }
</script>

<style scoped>
    .navbar-brand img {
        width: 3rem;
        height: 3rem;
    }
    .navbar-nav .nav-item a:focus {
        outline: 0;
    }
</style>