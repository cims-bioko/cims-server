<template>
    <div class="row">
        <div class="col-lg-4 mb-4">
            <div class="card">
                <div class="card-body">
                    <h1 class="card-title"><fa-icon icon="file-alt"/> Forms</h1>
                    <p class="card-text">
                        Upload and publish ODK-based data collection forms for use with the mobile app.
                    </p>
                    <router-link :to="{name:'forms'}" class="btn btn-info" href="#">Go to Forms</router-link>
                </div>
            </div>
        </div>
        <div class="col-lg-4 mb-4">
            <div class="card">
                <div class="card-body">
                    <h1 class="card-title"><fa-icon icon="sync"/> Sync</h1>
                    <p class="card-text">
                        Inspect and interact with the process that provides automatic data updates for the mobile app.
                    </p>
                    <router-link :to="{name:'sync'}" class="btn btn-info" href="#">Go to Sync</router-link>
                </div>
            </div>
        </div>
        <div class="col-lg-4 mb-4">
            <div class="card">
                <div class="card-body">
                    <h1 class="card-title"><fa-icon icon="business-time" /> Backups</h1>
                    <p class="card-text">
                        Create and manage historical snapshots via backups of the entire CIMS Server database.
                    </p>
                    <router-link :to="{name:'backups'}" class="btn btn-info" href="#">Go to Backups</router-link>
                </div>
            </div>
        </div>
        <div class="col-lg-4 mb-4">
            <div class="card">
                <div class="card-body">
                    <h1 class="card-title"><fa-icon icon="user" /> Users</h1>
                    <p class="card-text">
                        Create and manage user accounts for CIMS Server (supervisor accounts for the mobile app).
                    </p>
                    <router-link :to="{name:'users'}" class="btn btn-info" href="#">Go to Users</router-link>
                </div>
            </div>
        </div>
        <div class="col-lg-4 mb-4">
            <div class="card">
                <div class="card-body">
                    <h1 class="card-title"><fa-icon icon="users" /> Roles</h1>
                    <p class="card-text">
                        Create and managed security roles for CIMS Server. Privileges are granted to users by roles.
                    </p>
                    <router-link :to="{name:'roles'}" class="btn btn-info" href="#">Go to Roles</router-link>
                </div>
            </div>
        </div>
        <div class="col-lg-4 mb-4">
            <div class="card">
                <div class="card-body">
                    <h1 class="card-title"><fa-icon icon="user" /> Fieldworkers</h1>
                    <p class="card-text">
                        Create and manage off-line user accounts for fieldworkers collecting data using the mobile app.
                    </p>
                    <router-link :to="{name:'fieldworkers'}" class="btn btn-info" href="#">Go to Fieldworkers</router-link>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'home'
    }
</script>