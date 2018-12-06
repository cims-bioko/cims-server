<template>
    <b-modal ref="modal" v-model="visible" @show="initData" @ok.prevent="submit">
        <template slot="modal-title">{{$t('roles.editmodal.title')}}</template>
        <template v-for="(error, index) of errors">
            <b-alert variant="danger" :key="index" :show="5">{{error}}</b-alert>
        </template>
        <b-form ref="form" @submit.stop.prevent novalidate>
            <b-form-group :label="$t('roles.name')" :invalid-feedback="nameError" :state="nameState">
                <b-form-input v-model="scratch.name" :state="nameState" @input="validate"/>
            </b-form-group>
            <b-form-group :label="$t('roles.description')" :invalid-feedback="descriptionError" :state="descriptionState">
                <b-form-input v-model="scratch.description" :state="descriptionState" @input="validate"/>
            </b-form-group>
            <b-form-group :label="$t('roles.privileges')" :invalid-feedback="privilegesError" :state="privilegesState">
                <b-form-select multiple v-model="scratch.privileges" :options="availablePrivileges" :state="privilegesState" @input="validate"/>
            </b-form-group>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="edit"/> {{$t('roles.update')}}</template>
    </b-modal>
</template>

<script>
    export default {
        name: 'role-edit-dialog',
        props: {
            uuid: {
                type: String,
                required: true
            },
            availablePrivileges: {
                type: Array,
                required: true
            }
        },
        data() {
            return {
                visible: false,
                scratch: {
                    name: '',
                    description: '',
                    privileges: []
                },
                validated: null,
                errors: [],
                fieldErrors: {}
            }
        },
        methods: {
            async initData() {
                await this.loadRole(this.uuid)
                this.validated = null
                this.errors = []
                this.fieldErrors = {}
            },
            show() {
                this.$refs.modal.show()
            },
            hide() {
                this.$refs.modal.hide();
            },
            validate() {
                if (this.visible) {
                    this.errors = []
                    this.fieldErrors = {}
                    this.validateName()
                    this.validatePrivileges()
                    this.validated = Object.keys(this.fieldErrors).length === 0
                }
                return this.validated
            },
            validateName() {
                if (!this.scratch.name) {
                    this.addFieldError('name', this.$t('roles.namereq'))
                }
            },
            validatePrivileges() {
                if (this.scratch.privileges.length <= 0) {
                    this.addFieldError('privileges', this.$t('roles.privreq'))
                }
            },
            addFieldError(field, error) {
                this.fieldErrors[field] = this.fieldErrors[field] || []
                this.fieldErrors[field].push(error)
            },
            buildData() {
                let s = this.scratch
                return {
                    name: s.name,
                    description: s.description,
                    privileges: s.privileges
                }
            },
            async submit(e) {
                if (this.validate()) {
                    try {
                        let response = await this.$xhr.put(`/role/${this.uuid}`, this.buildData())
                        this.hide()
                        this.$emit('ok', response.data)
                    } catch (err) {
                        if (err.response.status === 400) {
                            let data = err.response.data
                            this.errors = data.errors
                            this.fieldErrors = data.fieldErrors
                        }
                    }
                } else {
                    e.preventDefault()
                }
            },
            async loadRole(uuid) {
                let response = await this.$xhr.get(`/role/${uuid}`)
                this.scratch = response.data
            }
        },
        computed: {
            nameState() {
                return this.validated == null? null : !this.nameError
            },
            nameError() {
                return (this.fieldErrors.name || []).join(' ')
            },
            descriptionState() {
                return this.validated == null? null : !this.descriptionError
            },
            descriptionError() {
                return (this.fieldErrors.description || []).join(' ')
            },
            privilegesState() {
                return this.validated == null? null : !this.privilegesError
            },
            privilegesError() {
                return (this.fieldErrors.privileges || []).join(' ')
            }
        }
    }
</script>
