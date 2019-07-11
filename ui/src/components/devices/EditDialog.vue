<template>
    <b-modal ref="modal" v-model="visible" @show="initData" @ok.prevent="submit" no-enforce-focus>
        <template slot="modal-title">{{$t('devices.editmodal.title')}}</template>
        <template v-for="(error, index) of errors">
            <b-alert variant="danger" :key="index" :show="5">{{error}}</b-alert>
        </template>
        <b-form ref="form" @submit.stop.prevent novalidate>
            <b-form-row>
                <b-form-group :label="$t('devices.name')" class="col-12 col-sm-6" :invalid-feedback="nameError" :state="nameState">
                    <b-form-input v-model="scratch.name" :state="nameState" @input="validate"/>
                </b-form-group>
            </b-form-row>
            <b-form-group :label="$t('devices.description')" :invalid-feedback="descriptionError" :state="descriptionState">
                <b-form-input v-model="scratch.description" :state="descriptionState" @input="validate"/>
            </b-form-group>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="edit"/> {{$t('devices.update')}}</template>
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
    export default {
        name: 'device-edit-dialog',
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
                    name: '',
                    description: ''
                },
                validated: null,
                errors: [],
                fieldErrors: {}
            }
        },
        methods: {
            async initData() {
                await this.loadDevice(this.uuid)
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
                if (!this.scratch.name) {
                    this.addFieldError('name', this.$t('devices.namereq'))
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
                    description: s.description
                }
            },
            async submit(e) {
                if (this.validate()) {
                    try {
                        let response = await this.$xhr.put(`/device/${this.uuid}`, this.buildData())
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
            async loadDevice(uuid) {
                let response = await this.$xhr.get(`/device/${uuid}`)
                this.scratch = response.data
            }
        },
        computed: {
            nameState() {
                return this.validated == null? null : !this.nameError
            },
            nameError() {
                return (this.fieldErrors.name|| []).join(' ')
            },
            descriptionState() {
                return this.validated == null? null : !this.descriptionError
            },
            descriptionError() {
                return (this.fieldErrors.description || []).join(' ')
            }
        },
        components: {
            bModal, bAlert, bForm, bFormGroup, bFormInput, bFormRow
        }
    }
</script>
