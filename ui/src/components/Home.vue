<template>
    <b-container>
        <b-row>
            <b-col class="col-lg-4 mb-4">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="file-alt"/> Forms</h1>
                    <p class="card-text">
                        Upload and publish ODK-based data collection forms for use with the mobile app.
                    </p>
                    <b-button variant="info" :to="{name:'forms'}">Go to Forms</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="sync"/> Sync</h1>
                    <p class="card-text">
                        Inspect and interact with the process that provides automatic data updates for the mobile app.
                    </p>
                    <b-button variant="info" :to="{name:'sync'}">Go to Sync</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="business-time" /> Backups</h1>
                    <p class="card-text">
                        Create and manage historical snapshots via backups of the entire CIMS Server database.
                    </p>
                    <b-button variant="info" :to="{name:'backups'}">Go to Backups</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="user" /> Users</h1>
                    <p class="card-text">
                        Create and manage user accounts for CIMS Server (supervisor accounts for the mobile app).
                    </p>
                    <b-button variant="info" :to="{name:'users'}">Go to Users</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="users" /> Roles</h1>
                    <p class="card-text">
                        Create and managed security roles for CIMS Server. Privileges are granted to users by roles.
                    </p>
                    <b-button variant="info" :to="{name:'roles'}">Go to Roles</b-button>
                </b-card>
            </b-col>
            <b-col class="col-lg-4 mb-4">
                <b-card>
                    <h1 class="card-title"><fa-icon icon="user" /> Fieldworkers</h1>
                    <p class="card-text">
                        Create and manage off-line user accounts for fieldworkers collecting data using the mobile app.
                    </p>
                    <b-button variant="info" :to="{name:'fieldworkers'}">Go to Fieldworkers</b-button>
                </b-card>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
    export default {
        name: 'home'
    }
</script>