<template>
    <b-modal ref="modal" v-model="visible" @show="clearData" @ok.prevent="submit">
        <template slot="modal-title">{{$t('roles.createmodal.title')}}</template>
        <template v-for="(error, index) of errors">
            <b-alert variant="danger" :key="index" :show="5">{{error}}</b-alert>
        </template>
        <b-form ref="form" @submit.stop.prevent novalidate>
            <b-form-group :label="$t('roles.name')" :invalid-feedback="nameError" :state="nameState">
                <b-form-input v-model="name" :state="nameState" @input="validate"/>
            </b-form-group>
            <b-form-group :label="$t('roles.description')" :invalid-feedback="descriptionError" :state="descriptionState">
                <b-form-input v-model="description" :state="descriptionState" @input="validate"/>
            </b-form-group>
            <b-form-group :label="$t('roles.privileges')" :invalid-feedback="privilegesError" :state="privilegesState">
                <b-form-select multiple v-model="privileges" :options="availablePrivileges" :state="privilegesState" @input="validate"/>
            </b-form-group>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="plus"/> {{$t('roles.create')}}</template>
        <template slot="modal-cancel">{{$t('modal.cancel')}}</template>
    </b-modal>
</template>

<script>
    import {BModal, BAlert, BForm, BFormGroup, BFormInput, BFormSelect} from 'bootstrap-vue'
    export default {
        name: 'role-create-dialog',
        props: {
            availablePrivileges: {
                type: Array,
                required: true
            }
        },
        data() {
            return {
                visible: false,
                name: '',
                description: '',
                privileges: [],
                validated: null,
                errors: [],
                fieldErrors: {},
            }
        },
        methods: {
            clearData() {
                this.name = ''
                this.description = ''
                this.privileges = []
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
                if (!this.name) {
                    this.addFieldError('name', this.$t('roles.namereq'))
                }
            },
            validatePrivileges() {
                if (this.privileges.length <= 0) {
                    this.addFieldError('privileges', this.$t('roles.privreq'))
                }
            },
            addFieldError(field, error) {
                this.fieldErrors[field] = this.fieldErrors[field] || []
                this.fieldErrors[field].push(error)
            },
            buildData() {
                return {
                    name: this.name,
                    description: this.description,
                    privileges: this.privileges
                }
            },
            async submit(e) {
                if (this.validate()) {
                    try {
                        let response = await this.$xhr.post('/roles', this.buildData())
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
        },
        components: {
            BModal, BAlert, BForm, BFormGroup, BFormInput, BFormSelect
        }
    }
</script>
