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
                <b-nav-item v-if="$can('VIEW_SYNC')" :to="{name:'sync'}"><fa-icon icon="sync"/> {{$t('nav.sync')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_BACKUPS')" :to="{name:'backups'}"><fa-icon icon="business-time"/> {{$t('nav.backups')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_USERS')" :to="{name:'users'}"><fa-icon icon="user"/> {{$t('nav.users')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_ROLES')" :to="{name:'roles'}"><fa-icon icon="users"/> {{$t('nav.roles')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_FIELDWORKERS')" :to="{name:'fieldworkers'}"><fa-icon icon="user"/> {{$t('nav.fieldworkers')}}</b-nav-item>
                <b-nav-item v-if="$can('VIEW_DEVICES')" :to="{name:'devices'}"><fa-icon icon="tablet-alt"/> {{$t('nav.devices')}}</b-nav-item>
            </b-navbar-nav>
            <b-navbar-nav v-if="$user.username" >
                <b-nav-item-dropdown right>
                    <template slot="button-content">
                        <fa-icon icon="user" /> {{$user.username}}
                    </template>
                    <b-dropdown-item @click="$emit('logout')">{{$t('nav.logout')}}</b-dropdown-item>
                </b-nav-item-dropdown>
            </b-navbar-nav>
        </b-collapse>
    </b-navbar>
</template>

<script>
    import bNavbar from 'bootstrap-vue/es/components/navbar/navbar'
    import bNavbarNav from 'bootstrap-vue/es/components/navbar/navbar-nav'
    import bNavbarBrand from 'bootstrap-vue/es/components/navbar/navbar-brand'
    import bNavbarToggle from 'bootstrap-vue/es/components/navbar/navbar-toggle'
    import bCollapse from 'bootstrap-vue/es/components/collapse/collapse'
    import bDropdownItem from 'bootstrap-vue/es/components/dropdown/dropdown-item'
    import bNavItem from 'bootstrap-vue/es/components/nav/nav-item'
    import bNavItemDropdown from 'bootstrap-vue/es/components/nav/nav-item-dropdown'
    export default {
        name: 'app-header',
        components: {
            bNavbar, bNavbarNav, bNavbarBrand, bNavbarToggle,
            bCollapse, bNavItem, bNavItemDropdown, bDropdownItem
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