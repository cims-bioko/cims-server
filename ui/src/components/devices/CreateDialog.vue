<template>
    <b-modal ref="modal" v-model="visible" @show="clearData" @ok.prevent="submit" no-enforce-focus>
        <template slot="modal-title">{{$t('devices.createmodal.title')}}</template>
        <template v-for="(error, index) of errors">
            <b-alert variant="danger" :key="index" :show="5">{{error}}</b-alert>
        </template>
        <b-form ref="form" @submit.stop.prevent novalidate>
            <b-form-row>
                <b-form-group :label="$t('devices.name')" class="col-12 col-sm-6" :invalid-feedback="nameError" :state="nameState">
                    <b-form-input v-model="name" :state="nameState" @input="validate"/>
                </b-form-group>
            </b-form-row>
            <b-form-group :label="$t('devices.description')" :invalid-feedback="descriptionError" :state="descriptionState">
                <b-form-input v-model="description" :state="descriptionState" @input="validate"/>
            </b-form-group>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="plus"/> {{$t('roles.create')}}</template>
        <template slot="modal-cancel">{{$t('modal.cancel')}}</template>
    </b-modal>
</template>

<script>

    import {BModal, BAlert, BForm, BFormGroup, BFormInput, BFormRow} from 'bootstrap-vue'
    export default {
        name: 'device-create-dialog',
        props: {
        },
        data() {
            return {
                visible: false,
                name: '',
                description: '',
                validated: null,
                errors: [],
                fieldErrors: {}
            }
        },
        methods: {
            clearData() {
                this.name = ''
                this.description = ''
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
                    this.validated = Object.keys(this.fieldErrors).length === 0
                }
                return this.validated
            },
            validateName() {
                if (!this.name) {
                    this.addFieldError('name', this.$t('devices.namereq'))
                }
            },
            addFieldError(field, error) {
                this.fieldErrors[field] = this.fieldErrors[field] || []
                this.fieldErrors[field].push(error)
            },
            buildData() {
                return {
                    name: this.name,
                    description: this.description
                }
            },
            async submit(e) {
                if (this.validate()) {
                    try {
                        let response = await this.$xhr.post('/devices', this.buildData())
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
            }
        },
        components: {
            BModal, BAlert, BForm, BFormGroup, BFormInput, BFormRow
        }
    }
</script>
