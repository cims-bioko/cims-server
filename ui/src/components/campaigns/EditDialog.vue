<template>
    <b-modal ref="modal" v-model="visible" @show="initData" @ok.prevent="submit" no-enforce-focus>
        <template slot="modal-title">{{$t('campaigns.editmodal.title')}}</template>
        <template v-for="(error, index) of errors">
            <b-alert variant="danger" :key="index" :show="5">{{error}}</b-alert>
        </template>
        <b-form ref="form" @submit.stop.prevent novalidate>
            <b-form-row>
                <b-form-group :label="$t('campaigns.name')" class="col-12 col-sm-6" :invalid-feedback="nameError" :state="nameState">
                    <b-form-input v-model="scratch.name" :state="nameState" @input="validate"/>
                </b-form-group>
                <b-form-group :label="$t('campaigns.description')" class="col-12 col-sm-6" :invalid-feedback="descriptionError" :state="descriptionState">
                    <b-form-input v-model="scratch.description" :state="descriptionState" @input="validate"/>
                </b-form-group>
            </b-form-row>
            <b-form-row>
                <b-form-group :label="$t('campaigns.start')" :invalid-feedback="startError" :state="startState">
                    <b-form-input type="date" v-model="scratch.start" :state="startState" @input="validate"/>
                </b-form-group>
                <b-form-group :label="$t('campaigns.end')" class="col-12 col-sm-6" :invalid-feedback="endError" :state="endState">
                    <b-form-input type="date" v-model="scratch.end" :state="endState" @input="validate"/>
                </b-form-group>
            </b-form-row>
            <b-form-row>
                <b-form-group :label="$t('campaigns.forms')" :invalid-feedback="formsError" :state="formsState">
                    <b-form-select multiple :options="availableForms" v-model="scratch.forms" />
                </b-form-group>
            </b-form-row>
            <b-form-row>
                <b-form-group :label="$t('campaigns.devices')" :invalid-feedback="devicesError" :state="devicesState">
                    <b-form-select multiple :options="availableDevices" v-model="scratch.devices" />
                </b-form-group>
            </b-form-row>
            <b-form-row>
                <b-form-checkbox v-model="scratch.disabled">{{$t('campaigns.disabled')}}</b-form-checkbox>
            </b-form-row>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="edit"/> {{$t('users.update')}}</template>
        <template slot="modal-cancel">{{$t('modal.cancel')}}</template>
    </b-modal>
</template>

<script>
    import {BModal, BAlert, BForm, BFormGroup, BFormInput, BFormCheckbox, BFormRow, BFormSelect} from 'bootstrap-vue'
    import {isValidDate} from "../../dates";
    export default {
        name: 'campaign-edit-dialog',
        props: {
            uuid: {
                type: String,
                required: true
            }
        },
        data() {
            return {
                visible: false,
                scratch: {
                    name: '',
                    description: '',
                    start: null,
                    end: null,
                    disabled: null,
                    forms: [],
                    devices: []
                },
                availableForms: [],
                availableDevices: [],
                validated: null,
                errors: [],
                fieldErrors: {}
            }
        },
        methods: {
            async initData() {
                const loadForms = this.loadAvailableForms()
                const loadDevices = this.loadAvailableDevices()
                const loadCampaign = this.loadCampaign(this.uuid)
                await loadForms
                await loadDevices
                await loadCampaign
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
                if (!this.scratch.name) {
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
                let s = this.scratch
                return {
                    name: s.name,
                    description: s.description,
                    start: s.start,
                    end: s.end,
                    disabled: s.disabled,
                    forms: s.forms.map(v => { let [id, version] = v.split('|'); return {id: id, version: version}; }),
                    devices: s.devices
                }
            },
            async submit(e) {
                if (this.validate()) {
                    try {
                        let response = await this.$xhr.put(`/campaign/${this.uuid}`, this.buildData())
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
            async loadCampaign(uuid) {
                let response = await this.$xhr.get(`/campaign/${uuid}`)
                let data = response.data
                this.scratch.disabled = data.disabled
                this.scratch.name = data.name
                this.scratch.description = data.description
                this.scratch.start = data.start? new Date(data.start).toISOString().slice(0,10) : null
                this.scratch.end = data.end? new Date(data.end).toISOString().slice(0,10) : null
                this.scratch.forms = data.forms.map(id => `${id.id}|${id.version}`)
            },
            async loadAvailableForms() {
                let response = await this.$xhr.get(`/campaign/availableForms`)
                this.availableForms = response.data.map(id => ({value: `${id.id}|${id.version}`, text: `${id.id} (${id.version})`}));
            },
            async loadAvailableDevices() {
                let response = await this.$xhr.get(`/campaign/${this.uuid}/availableDevices`)
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
            BModal, BAlert, BForm, BFormGroup, BFormInput, BFormCheckbox, BFormRow, BFormSelect
        }
    }
</script>
