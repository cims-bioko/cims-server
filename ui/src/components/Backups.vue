<template>
    <b-container>
        <h1><fa-icon icon="business-time"/> Backups</h1>
        <b-table :items="items">
            <template slot="created" slot-scope="data">{{ data | formatDate }}</template>
        </b-table>
    </b-container>
</template>

<script>
    import axios from 'axios'
    export default {
        name: 'backups',
        data() {
            return {
                items: []
            }
        },
        mounted() {
            axios.get('/backups').then(rsp => this.items = rsp.data.content)
        }
    }
</script>
