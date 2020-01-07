<template>
    <b-modal ref="modal" v-model="visible" @show="init" @ok.prevent="submit" no-enforce-focus>
        <template slot="modal-title">{{$t('campaigns.createmodal.title')}}</template>
        <template v-for="(error, index) of errors">
            <b-alert variant="danger" :key="index" :show="5">{{error}}</b-alert>
        </template>
        <b-form ref="form" @submit.stop.prevent novalidate>
            <b-form-row>
                <b-form-group :label="$t('campaigns.name')" class="col-12 col-sm-6" :invalid-feedback="nameError" :state="nameState">
                    <b-form-input v-model="name" :state="nameState" @input="validate"/>
                </b-form-group>
                <b-form-group :label="$t('campaigns.description')" class="col-12 col-sm-6" :invalid-feedback="descriptionError" :state="descriptionState">
                    <b-form-input v-model="description" :state="descriptionState" @input="validate"/>
                </b-form-group>
            </b-form-row>
            <b-form-row>
                <b-form-group :label="$t('campaigns.start')" :invalid-feedback="startError" :state="startState">
                    <b-form-input type="date" v-model="start" :state="startState" @input="validate"/>
                </b-form-group>
                <b-form-group :label="$t('campaigns.end')" class="col-12 col-sm-6" :invalid-feedback="endError" :state="endState">
                    <b-form-input type="date" v-model="end" :state="endState" @input="validate"/>
                </b-form-group>
            </b-form-row>
            <b-form-row>
                <b-form-group :label="$t('campaigns.forms')" :invalid-feedback="formsError" :state="formsState">
                    <b-form-select multiple :options="availableForms" v-model="forms" />
                </b-form-group>
            </b-form-row>
            <b-form-row>
                <b-form-group :label="$t('campaigns.devices')" :invalid-feedback="devicesError" :state="devicesState">
                    <b-form-select multiple :options="availableDevices" v-model="devices" />
                </b-form-group>
            </b-form-row>
            <b-form-checkbox v-model="disabled">{{$t('campaigns.disabled')}}</b-form-checkbox>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="plus"/> {{$t('campaigns.create')}}</template>
        <template slot="modal-cancel">{{$t('modal.cancel')}}</template>
    </b-modal>
</template>

<script>
    import {BModal, BAlert, BForm, BFormGroup, BFormInput, BFormCheckbox, BFormRow, BFormSelect} from 'bootstrap-vue'
    import {isValidDate} from "../../dates";
    export default {
        name: 'campaign-create-dialog',
        data() {
            return {
                visible: false,
                name: '',
                description: '',
                start: null,
                end: null,
                disabled: null,
                forms: [],
                availableForms: [],
                devices: [],
                availableDevices: [],
                validated: null,
                errors: [],
                fieldErrors: {}
            }
        },
        methods: {
            async init() {
                this.clearData()
                const loadForms = this.loadAvailableForms(), loadDevices = this.loadAvailableDevices()
                await loadForms;
                await loadDevices;
            },
            clearData() {
                this.name = ''
                this.description = ''
                this.start = null
                this.end = null
                this.forms = []
                this.availableForms = []
                this.devices = []
                this.availableDevices = []
                this.disabled = null
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
                    this.validateDescription()
                    this.validateDates()
                    this.validated = Object.keys(this.fieldErrors).length === 0
                }
                return this.validated
            },
            validateName() {
                if (!this.name) {
                    this.addFieldError('name', this.$t('campaigns.namereq'))
                }
            },
            validateDescription() {
            },
            validateDates() {
                if (this.end && this.start) {
                    const startDate = new Date(this.start), endDate = new Date(this.end);
                    const startValid = isValidDate(startDate), endValid = isValidDate(endDate);
                    if (!startValid) {
                        this.addFieldError('start', this.$t('campaigns.baddate'))
                    }
                    if (!endValid) {
                        this.addFieldError('end', this.$t('campaigns.baddate'))
                    }
                    if (startValid && endValid && startDate.getTime() >= endDate.getTime()) {
                        this.addFieldError('start', this.$t('campaigns.endafterstart'))
                    }
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
                    start: this.start,
                    end: this.end,
                    disabled: this.disabled,
                    forms: this.forms.map(v => { let [id, version] = v.split('|'); return {id: id, version: version}; }),
                    devices: this.devices
                }
            },
            async submit(e) {
                if (this.validate()) {
                    try {
                        let response = await this.$xhr.post('/campaigns', this.buildData())
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
            async loadAvailableForms() {
                let response = await this.$xhr.get(`/campaign/availableForms`)
                this.availableForms = response.data.map(id => ({value: `${id.id}|${id.version}`, text: `${id.id} (${id.version})`}));
            },
            async loadAvailableDevices() {
                let response = await this.$xhr.get(`/campaign/availableDevices`)
                this.availableDevices = response.data.map(device => ({value: `${device.uuid}`, text: `${device.name} (${device.description})`}));
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
            startState() {
                return this.validated == null? null : !this.startError
            },
            startError() {
                return (this.fieldErrors.start || []).join(' ')
            },
            endState() {
                return this.validated == null? null : !this.endError
            },
            endError() {
                return (this.fieldErrors.end || []).join(' ')
            },
            formsState() {
                return this.validated == null? null : !this.formsError
            },
            formsError() {
                return (this.fieldErrors.forms || []).join(' ')
            },
            devicesState() {
                return this.validated == null? null : !this.devicesError
            },
            devicesError() {
                return (this.fieldErrors.devices || []).join(' ')
            }
        },
        components: {
            BModal, BAlert, BForm, BFormGroup, BFormInput, BFormRow, BFormCheckbox, BFormSelect
        }
    }
</script>
