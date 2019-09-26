<template>
    <b-modal ref="modal" @show="reset" @ok.prevent="submit" :ok-disabled="inProgress">
        <template slot="modal-title">{{$t('campaigns.uploadmodal.title', [name])}}</template>
        <b-alert variant="danger" v-if="error" :show="5">{{error}}</b-alert>
        <b-alert variant="success" v-if="message" :show="5">{{message}}</b-alert>
        <b-progress v-if="inProgress" :value="progress" striped animated/>
        <b-form ref="form" v-show="!inProgress" @submit.stop.prevent novalidate>
            <b-form-group :label="$t('campaigns.zip')" :invalid-feedback="zipError" :state="zipState">
                <b-form-file ref="zipInput" v-model="zipFile" accept="application/zip" :state="zipState"/>
            </b-form-group>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="upload"/> {{$t('campaigns.upload')}}</template>
        <template slot="modal-cancel">{{$t('campaigns.uploadmodal.close')}}</template>
    </b-modal>
</template>

<script>
    import {BModal, BAlert, BProgress, BForm, BFormGroup, BFormFile} from 'bootstrap-vue'
    export default {
        name: 'campaign-upload-dialog',
        props: {
            name: {
                type: String,
                required: true
            }
        },
        data() {
            return {
                validated: null,
                message: null,
                error: null,
                fieldErrors: {},
                progress: null,
                zipFile: null
            }
        },
        methods: {
            reset() {
                this.validated = null
                this.message = null
                this.error = null
                this.fieldErrors = {}
                this.progress = null
                this.$refs.zipInput.reset()
            },
            show() {
                this.$refs.modal.show()
            },
            hide() {
                this.$refs.modal.hide()
            },
            buildFormData() {
                let data = new FormData()
                if (this.zipFile) {
                    data.append('campaign_file', this.zipFile, this.zipFile.name)
                }
                return data;
            },
            validate() {
                this.error = null
                this.message = null
                this.fieldErrors = {}
                if (!this.zipFile) {
                    this.addFieldError('campaign_file', this.$t('campaigns.zipreq'))
                }
                this.validated = Object.keys(this.fieldErrors).length === 0
                return this.validated
            },
            addFieldError(field, error) {
                this.fieldErrors[field] = this.fieldErrors[field] || []
                this.fieldErrors[field].push(error)
            },
            submit() {
                if (this.validate()) {
                    this.upload(this.buildFormData())
                }
            },
            async upload(formData) {
                try {
                    let rsp = await this.$xhr.post('/campaign', formData, {
                        onUploadProgress: (pe) => {
                            this.progress = pe.lengthComputable ? Math.round((pe.loaded * 100) / pe.total) : 100
                        }
                    })
                    this.uploadSuccess(rsp.data)
                } catch (err) {
                    this.uploadError(err)
                }
                this.progress = null
            },
            uploadSuccess() {
                this.hide()
                this.$emit('ok', {"messages": [this.$t('campaigns.success')]})
            },
            uploadError(err) {
                let rsp = err.response
                if (rsp.status >= 400) {
                    let msg = this.$t('campaigns.failed')
                    if (rsp.status === 400 && rsp.data.output) {
                        let output = rsp.data.output
                        msg += output.reduce((acc,cur) => acc + '\n' + cur.message, ':\n')
                    }
                    this.error = msg
                }
            }
        },
        computed: {
            inProgress() {
                return this.progress != null
            },
            zipState() {
                return this.validated != null && !this.zipError
            },
            zipError() {
                return (this.fieldErrors['campaign_file'] || []).join(' ')
            }
        },
        components: {
            BModal, BAlert, BProgress, BForm, BFormGroup, BFormFile
        }
    }
</script>

<style scoped>
    .b-form-file >>> .custom-file-label {
        overflow: hidden;
    }
</style>