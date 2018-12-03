<template>
    <b-navbar toggleable="md" type="dark" variant="info" class="bg-dark navbar-expand-lg fixed-top" role="navigation">
        <b-navbar-brand href="#">
            <img src="/img/logo.svg" class="img-fluid"/> CIMS Server
        </b-navbar-brand>
        <b-navbar-toggle target="nav_collapse" />
        <b-collapse is-nav id="nav_collapse">
            <b-navbar-nav class="mr-auto" v-if="$user.username">
                <b-nav-item :to="{name:'home'}" exact><fa-icon icon="home"/> Home</b-nav-item>
                <b-nav-item v-if="$can('VIEW_FORMS')" :to="{name:'forms'}"><fa-icon icon="file-alt"/> Forms</b-nav-item>
                <b-nav-item v-if="$can('VIEW_SYNC')" :to="{name:'sync'}"><fa-icon icon="sync"/> Sync</b-nav-item>
                <b-nav-item v-if="$can('VIEW_BACKUPS')" :to="{name:'backups'}"><fa-icon icon="business-time"/> Backups</b-nav-item>
                <b-nav-item v-if="$can('VIEW_USERS')" :to="{name:'users'}"><fa-icon icon="user"/> Users</b-nav-item>
                <b-nav-item v-if="$can('VIEW_ROLES')" :to="{name:'roles'}"><fa-icon icon="users"/> Roles</b-nav-item>
                <b-nav-item v-if="$can('VIEW_FIELDWORKERS')" :to="{name:'fieldworkers'}"><fa-icon icon="user"/> Fieldworkers</b-nav-item>
            </b-navbar-nav>
            <b-navbar-nav v-if="$user.username" >
                <b-nav-item-dropdown right>
                    <template slot="button-content">
                        <fa-icon icon="user" /> {{$user.username}}
                    </template>
                    <b-dropdown-item @click="$emit('logout')">Logout</b-dropdown-item>
                </b-nav-item-dropdown>
            </b-navbar-nav>
        </b-collapse>

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
    .navbar-nav .nav-item a:focus {
        outline: 0;
    }
</style>