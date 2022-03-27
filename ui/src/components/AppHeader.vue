<template>
    <b-navbar toggleable="md" type="dark" variant="info" class="bg-dark navbar-expand-lg fixed-top" role="navigation">
        <b-navbar-brand href="#">
            <img src="/img/logo.svg" class="img-fluid"/> {{$t('app.name')}}
        </b-navbar-brand>
        <b-navbar-toggle target="nav_collapse" />
        <b-collapse is-nav id="nav_collapse">
            <b-navbar-nav class="mr-auto" v-if="$user.username">
                <b-nav-item :to="{name:'home'}" exact><fa-icon icon="home"/> {{$t('nav.home')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_FORMS')" :to="{name:'forms'}"><fa-icon icon="file-alt"/> {{$t('nav.forms')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_BACKUPS')" :to="{name:'backups'}"><fa-icon icon="business-time"/> {{$t('nav.backups')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_USERS')" :to="{name:'users'}"><fa-icon icon="user"/> {{$t('nav.users')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_ROLES')" :to="{name:'roles'}"><fa-icon icon="users"/> {{$t('nav.roles')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_FIELDWORKERS')" :to="{name:'fieldworkers'}"><fa-icon icon="user"/> {{$t('nav.fieldworkers')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_DEVICES')" :to="{name:'devices'}"><fa-icon icon="tablet-alt"/> {{$t('nav.devices')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_CAMPAIGNS')" :to="{name:'campaigns'}"><fa-icon icon="shuttle-van"/> {{$t('nav.campaigns')}}</b-nav-item>
            </b-navbar-nav>
            <b-navbar-nav v-if="$user.username" >
                <b-nav-item-dropdown right>
                    <template slot="button-content">
                        <fa-icon icon="user" /> {{$user.username}}
                    </template>
                    <b-dropdown-item @click="$emit('logout')">{{$t('nav.logout')}}</b-dropdown-item>
                    <b-dropdown-item v-if="$can('REBUILD_INDEX')" :to="{name:'rebuildindex'}">{{$t('nav.rebuildindex')}}</b-dropdown-item>
                </b-nav-item-dropdown>
            </b-navbar-nav>
        </b-collapse>
    </b-navbar>
</template>

<script>
    import {
        BNavbar, BNavbarNav, BNavbarBrand, BNavbarToggle, BCollapse, BDropdownItem, BNavItem, BNavItemDropdown
    } from 'bootstrap-vue'
    export default {
        name: 'app-header',
        components: {
            BNavbar, BNavbarNav, BNavbarBrand, BNavbarToggle, BCollapse, BNavItem, BNavItemDropdown, BDropdownItem
        }
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