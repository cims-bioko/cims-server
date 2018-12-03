<template>
    <div id="app">
        <app-header @logout="logout"/>
        <app-content />
        <app-footer />
    </div>
</template>

<script>
    import AppHeader from './components/AppHeader'
    import AppContent from './components/AppContent'
    import AppFooter from './components/AppFooter'
    export default {
        name: 'app',
        methods: {
            logout() {
                window.location = '/logout'
            }
        },
        components: {
            AppHeader,
            AppContent,
            AppFooter
        }
    }
</script>

<style>
    html, body {
        height: 100%;
    }
</style>