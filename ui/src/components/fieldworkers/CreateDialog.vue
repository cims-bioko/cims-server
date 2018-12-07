<template>
    <b-modal ref="modal" v-model="visible" @show="clearData" @ok.prevent="submit" no-enforce-focus>
        <template slot="modal-title">{{$t('fieldworkers.createmodal.title')}}</template>
        <template v-for="(error, index) of errors">
            <b-alert variant="danger" :key="index" :show="5">{{error}}</b-alert>
        </template>
        <b-form ref="form" @submit.stop.prevent novalidate>
            <b-form-row>
                <b-form-group :label="$t('fieldworkers.firstName')" class="col-12 col-sm-4" :invalid-feedback="firstNameError" :state="firstNameState">
                    <b-form-input v-model="firstName" :state="firstNameState" @input="nameUpdated"/>
                </b-form-group>
                <b-form-group :label="$t('fieldworkers.lastName')" class="col-12 col-sm-5" :invalid-feedback="lastNameError" :state="lastNameState">
                    <b-form-input v-model="lastName" :state="lastNameState" @input="nameUpdated"/>
                </b-form-group>
                <b-form-group :label="$t('fieldworkers.id')" class="col-sm-3" :invalid-feedback="extIdError" :state="extIdState">
                    <b-input-group :prepend="idPrefix">
                        <b-form-input v-model="extId" :state="extIdState" @input="validate" @change="idUpdated"/>
                    </b-input-group>
                </b-form-group>
            </b-form-row>
            <b-form-row>
                <b-form-group :label="$t('fieldworkers.password')" class="col-12 col-sm-6" :invalid-feedback="passwordError" :state="passwordState">
                    <b-form-input type="password" v-model="password" :state="passwordState" @input="validate"/>
                </b-form-group>
                <b-form-group :label="$t('fieldworkers.passwordConfirmed')" class="col-12 col-sm-6" :invalid-feedback="passwordConfirmedError" :state="passwordConfirmedState">
                    <b-form-input type="password" v-model="passwordConfirmed" :state="passwordConfirmedState" @input="validate"/>
                </b-form-group>
            </b-form-row>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="plus"/> {{$t('fieldworkers.create')}}</template>
        <template slot="modal-cancel">{{$t('modal.cancel')}}</template>
    </b-modal>
</template>

<script>
    export default {
        name: 'fieldworker-create-dialog',
        data() {
            return {
                visible: false,
                firstName: '',
                lastName: '',
                extId: '',
                idEdited: false,
                password: '',
                passwordConfirmed: '',
                validated: null,
                errors: [],
                fieldErrors: {},
                idPrefix: 'FW'
            }
        },
        methods: {
            clearData() {
                this.firstName = ''
                this.lastName = ''
                this.extId = ''
                this.idEdited = false
                this.password = ''
                this.passwordConfirmed = ''
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
            nameUpdated() {
                if (this.visible && !this.idEdited) {
                    this.extId = this.derivedId
                }
            },
            idUpdated() {
              this.idEdited = true
            },
            validate() {
                if (this.visible) {
                    this.errors = []
                    this.fieldErrors = {}
                    this.validateFirstName()
                    this.validateLastName()
                    this.validateExtId()
                    this.validatePassword()
                    this.validated = Object.keys(this.fieldErrors).length === 0
                }
                return this.validated
            },
            validateFirstName() {
                if (!this.firstName) {
                    this.addFieldError('firstName', this.$t('fieldworkers.fnamereq'))
                }
            },
            validateLastName() {
                if (!this.lastName) {
                    this.addFieldError('lastName', this.$t('fieldworkers.lnamereq'))
                }
            },
            validateExtId() {
                if (!this.extId) {
                    this.addFieldError('extId', this.$t('fieldworkers.idreq'))
                } else if (!this.extId.match(/^[A-Z][A-Z][1-9][0-9]*$/)) {
                    this.addFieldError('extId', this.$t('fieldworkers.idfmt'))
                }
            },
            validatePassword() {
                if (!this.password) {
                    this.addFieldError('password', this.$t('fieldworkers.passwdreq'))
                } else {
                    if (this.password !== this.passwordConfirmed) {
                        this.addFieldError('passwordConfirmed', this.$t('fieldworkers.passwdmatch'))
                    }
                    if (this.password.length < 8) {
                        this.addFieldError('password', this.$t('fieldworkers.passwdshort'))
                    } else if (this.password.length > 255) {
                        this.addFieldError('password', this.$t('fieldworkers.passwdlong'))
                    }
                }
            },
            addFieldError(field, error) {
                this.fieldErrors[field] = this.fieldErrors[field] || []
                this.fieldErrors[field].push(error)
            },
            buildData() {
                return {
                    firstName: this.firstName,
                    lastName: this.lastName,
                    extId: `${this.idPrefix}${this.extId}`,
                    password: this.password
                }
            },
            async submit(e) {
                if (this.validate()) {
                    try {
                        let response = await this.$xhr.post('/fieldworkers', this.buildData())
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
            extIdState() {
                return this.validated == null? null : !this.extIdError
            },
            extIdError() {
                return (this.fieldErrors.extId || []).join(' ')
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
            },
            derivedId() {
                let initial = (s) => (s || 'X').charAt(0)
                return `${initial(this.firstName)}${initial(this.lastName)}1`
            }
        }
    }
</script>
