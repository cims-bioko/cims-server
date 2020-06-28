<template>
    <b-container>
        <b-row>
            <b-col>
                <b-breadcrumb>
                    <b-breadcrumb-item to="/campaigns">{{$t('nav.campaigns')}}</b-breadcrumb-item>
                    <b-breadcrumb-item active>{{name}}</b-breadcrumb-item>
                </b-breadcrumb>
            </b-col>
        </b-row>
        <b-row class="align-items-center">
            <b-col class="col-auto">
                <h1>
                    <fa-icon icon="shuttle-van"/> {{$t('campaigns.title', [name])}}
                </h1>
            </b-col>
        </b-row>
        <b-row>
            <b-col>
                <router-view></router-view>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import {BBreadcrumb, BBreadcrumbItem, BCol, BContainer, BRow} from 'bootstrap-vue'

    export default {
        name: 'campaign',
        props: {
            campaign: {
                type: String,
                required: true
            }
        },
        data() {
            return {
                name: null,
                description: null
            }
        },
        methods: {
            async load() {
                let rsp = await this.$xhr.get(`/campaign/${this.campaign}`)
                let data = rsp.data
                this.name = data.name
                this.description = data.description
            },
        },
        mounted() {
            this.load()
        },
        components: {
            BContainer, BRow, BCol, BBreadcrumb, BBreadcrumbItem
        }
    }
</script>