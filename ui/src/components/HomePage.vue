<template>
    <b-container>
        <b-row>
            <b-col class="col-lg-4 mb-4" v-if="$can('VIEW_FORMS')">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="file-alt"/> {{$t('nav.forms')}}</h1>
                    <p class="card-text">{{$t('home.forms.desc')}}</p>
                    <b-button variant="info" :to="{name:'forms'}">{{$t('home.forms.btn')}}</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4" v-if="$can('VIEW_BACKUPS')">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="business-time" /> {{$t('nav.backups')}}</h1>
                    <p class="card-text">{{$t('home.backups.desc')}}</p>
                    <b-button variant="info" :to="{name:'backups'}">{{$t('home.backups.btn')}}</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4" v-if="$can('VIEW_USERS')">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="user" /> {{$t('nav.users')}}</h1>
                    <p class="card-text">{{$t('home.users.desc')}}</p>
                    <b-button variant="info" :to="{name:'users'}">{{$t('home.users.btn')}}</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4" v-if="$can('VIEW_ROLES')">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="users" /> {{$t('nav.roles')}}</h1>
                    <p class="card-text">{{$t('home.roles.desc')}}</p>
                    <b-button variant="info" :to="{name:'roles'}">{{$t('home.roles.btn')}}</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4" v-if="$can('VIEW_FIELDWORKERS')">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="user" /> {{$t('nav.fieldworkers')}}</h1>
                    <p class="card-text">{{$t('home.fieldworkers.desc')}}</p>
                    <b-button variant="info" :to="{name:'fieldworkers'}">{{$t('home.fieldworkers.btn')}}</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4" v-if="$can('VIEW_DEVICES')">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="tablet-alt" /> {{$t('nav.devices')}}</h1>
                    <p class="card-text">{{$t('home.devices.desc')}}</p>
                    <b-button variant="info" :to="{name:'devices'}">{{$t('home.devices.btn')}}</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4" v-if="$can('VIEW_CAMPAIGNS')">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="shuttle-van" /> {{$t('nav.campaigns')}}</h1>
                    <p class="card-text">{{$t('home.campaigns.desc')}}</p>
                    <b-button variant="info" :to="{name:'campaigns'}">{{$t('home.campaigns.btn')}}</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4" v-if="$can('VIEW_SUBMISSIONS')">
              <b-card>
                <h1 class="card-title"><fa-icon icon="shuttle-van" /> {{$t('nav.submissions')}}</h1>
                <p class="card-text">{{$t('home.submissions.desc')}}</p>
                <b-button variant="info" :to="{name:'submissions'}">{{$t('home.submissions.btn')}}</b-button>
              </b-card>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    import {BContainer, BRow, BCol, BCard, BButton} from 'bootstrap-vue'
    export default {
        name: 'home-page',
        components: {
            BContainer, BRow, BCol, BCard, BButton
        }
    }
</script>

<style scoped>
    .card-body p {
        min-height: 6rem;
    }
    .card-title {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
</style>