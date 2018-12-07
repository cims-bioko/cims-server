<template>
    <b-modal ref="modal" v-model="visible" @show="initData" @ok.prevent="submit" no-enforce-focus>
        <template slot="modal-title">{{$t('users.editmodal.title')}}</template>
        <template v-for="(error, index) of errors">
            <b-alert variant="danger" :key="index" :show="5">{{error}}</b-alert>
        </template>
        <b-form ref="form" @submit.stop.prevent novalidate>
            <b-form-row>
                <b-form-group :label="$t('users.firstName')" class="col-12 col-sm-6" :invalid-feedback="firstNameError" :state="firstNameState">
                    <b-form-input v-model="scratch.firstName" :state="firstNameState" @input="validate"/>
                </b-form-group>
                <b-form-group :label="$t('users.lastName')" class="col-12 col-sm-6" :invalid-feedback="lastNameError" :state="lastNameState">
                    <b-form-input v-model="scratch.lastName" :state="lastNameState" @input="validate"/>
                </b-form-group>
            </b-form-row>
            <b-form-group :label="$t('users.description')" :invalid-feedback="descriptionError" :state="descriptionState">
                <b-form-input v-model="scratch.description" :state="descriptionState" @input="validate"/>
            </b-form-group>
            <b-form-row>
                <b-form-group :label="$t('users.username')" class="col-12 col-sm-6" :invalid-feedback="usernameError" :state="usernameState">
                    <b-form-input v-model="scratch.username" :state="usernameState" @input="validate"/>
                </b-form-group>
                <b-form-group :label="$t('users.roles')" class="col-12 col-sm-6" :invalid-feedback="rolesError" :state="rolesState">
                    <b-form-select multiple v-model="scratch.roles" :options="availableRoles" :state="rolesState" @input="validate"/>
                </b-form-group>
            </b-form-row>
            <b-form-row>
                <b-form-group :label="$t('users.password')" class="col-12 col-sm-6" :invalid-feedback="passwordError" :state="passwordState">
                    <b-form-input type="password" v-model="scratch.password" :state="passwordState" @input="validate"/>
                </b-form-group>
                <b-form-group :label="$t('users.passwordConfirmed')" class="col-12 col-sm-6" :invalid-feedback="passwordConfirmedError" :state="passwordConfirmedState">
                    <b-form-input type="password" v-model="scratch.passwordConfirmed" :state="passwordConfirmedState" @input="validate"/>
                </b-form-group>
            </b-form-row>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="edit"/> {{$t('users.update')}}</template>
        <template slot="modal-cancel">{{$t('modal.cancel')}}</template>
    </b-modal>
</template>

<script>
    import bModal from 'bootstrap-vue/es/components/modal/modal'
    import bAlert from 'bootstrap-vue/es/components/alert/alert'
    import bForm from 'bootstrap-vue/es/components/form/form'
    import bFormGroup from 'bootstrap-vue/es/components/form-group/form-group'
    import bFormInput from 'bootstrap-vue/es/components/form-input/form-input'
    import bFormRow from 'bootstrap-vue/es/components/form/form-row'
    import bFormSelect from 'bootstrap-vue/es/components/form-select/form-select'
    export default {
        name: 'user-edit-dialog',
        props: {
            uuid: {
                type: String,
                required: true
            },
            availableRoles: {
                type: Array,
                required: true
            }
        },
        data() {
            return {
                visible: false,
                scratch: {
                    firstName: '',
                    lastName: '',
                    description: '',
                    username: '',
                    roles: [],
                    password: '',
                    passwordConfirmed: ''
                },
                validated: null,
                errors: [],
                fieldErrors: {}
            }
        },
        methods: {
            async initData() {
                await this.loadUser(this.uuid)
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
                    this.validateUsername()
                    this.validateRoles()
                    this.validatePassword()
                    this.validated = Object.keys(this.fieldErrors).length === 0
                }
                return this.validated
            },
            validateUsername() {
                if (!this.scratch.username) {
                    this.addFieldError('username', this.$t('users.userreq'))
                }
            },
            validateRoles() {
                if (this.scratch.roles.length <= 0) {
                    this.addFieldError('roles', this.$t('users.rolereq'))
                }
            },
            validatePassword() {
                let password = this.scratch.password, passwordConfirmed = this.scratch.passwordConfirmed
                if (password) {
                    if (password !== passwordConfirmed) {
                        this.addFieldError('passwordConfirmed', this.$t('users.passwdmatch'))
                    }
                    if (password.length < 8) {
                        this.addFieldError('password', this.$t('users.passwdshort'))
                    } else if (password.length > 255) {
                        this.addFieldError('password', this.$t('users.passwdlong'))
                    }
                }
            },
            addFieldError(field, error) {
                this.fieldErrors[field] = this.fieldErrors[field] || []
                this.fieldErrors[field].push(error)
            },
            buildData() {
                let s = this.scratch
                return {
                    firstName: s.firstName,
                    lastName: s.lastName,
                    description: s.description,
                    username: s.username,
                    roles: s.roles,
                    password: s.password
                }
            },
            async submit(e) {
                if (this.validate()) {
                    try {
                        let response = await this.$xhr.put(`/user/${this.uuid}`, this.buildData())
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
            async loadUser(uuid) {
                let response = await this.$xhr.get(`/user/${uuid}`)
                this.scratch = response.data
            }
        },
        computed: {
            firstNameState() {
                return this.validated == null? null : !this.firstNameError
            },
            firstNameError() {
                return (this.fieldErrors.firstName || []).join(' ')
            },
            lastNameState() {
                return this.validated == null? null : !this.lastNameError
            },
            lastNameError() {
                return (this.fieldErrors.lastName || []).join(' ')
            },
            descriptionState() {
                return this.validated == null? null : !this.descriptionError
            },
            descriptionError() {
                return (this.fieldErrors.description || []).join(' ')
            },
            usernameState() {
                return this.validated == null? null : !this.usernameError
            },
            usernameError() {
                return (this.fieldErrors.username|| []).join(' ')
            },
            rolesState() {
                return this.validated == null? null : !this.rolesError
            },
            rolesError() {
                return (this.fieldErrors.roles || []).join(' ')
            },
            passwordState() {
                return this.validated == null? null : !this.passwordError
            },
            passwordError() {
                return (this.fieldErrors.password || []).join(' ')
            },
            passwordConfirmedState() {
                return this.validated == null? null : !this.passwordConfirmedError
            },
            passwordConfirmedError() {
                return (this.fieldErrors.passwordConfirmed || []).join(' ')
            }
        },
        components: {
            bModal, bAlert, bForm, bFormGroup, bFormInput, bFormRow, bFormSelect
        }
    }
</script>
