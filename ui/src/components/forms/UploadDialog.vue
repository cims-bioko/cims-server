<template>
    <b-modal ref="modal" @show="reset" @ok.prevent="submit" :ok-disabled="inProgress">
        <template slot="modal-title">{{$t('forms.uploadmodal.title')}}</template>
        <b-alert variant="danger" v-if="error" :show="5">{{error}}</b-alert>
        <b-alert variant="success" v-if="message" :show="5">{{message}}</b-alert>
        <b-progress v-if="inProgress" :value="progress" striped animated/>
        <b-form ref="form" v-show="!inProgress" @submit.stop.prevent novalidate>
            <b-form-group :label="$t('forms.uploadType')" v-if="permittedUploadTypes.length > 1">
                <b-form-radio-group button-variant="outline-secondary" buttons
                                    v-model="selectedUploadType" :options="permittedUploadTypes" @change="validated=null"/>
            </b-form-group>
            <b-form-group label="XML" :invalid-feedback="xmlError" :state="xmlState">
                <b-form-file ref="xmlInput" v-model="xmlFile" :disabled="!isXmlUpload"
                        accept="text/xml,application/xml" :state="xmlState"/>
            </b-form-group>
            <b-form-group label="XLSForm" :invalid-feedback="xlsformError" :state="xlsformState">
                <b-form-file ref="xlsformInput" v-model="xlsformFile" :disabled="!isXlsformUpload"
                        accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" :state="xlsformState"/>
            </b-form-group>
            <b-form-group :label="$t('forms.mediaFiles')">
                <b-form-file ref="mediaFilesInput" v-model="mediaFiles" multiple/>
            </b-form-group>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="upload"/> {{$t('forms.upload')}}</template>
        <template slot="modal-cancel">{{$t('forms.uploadmodal.close')}}</template>
    </b-modal>
</template>

<script>
    import bModal from 'bootstrap-vue/es/components/modal/modal'
    import bAlert from 'bootstrap-vue/es/components/alert/alert'
    import bProgress from 'bootstrap-vue/es/components/progress/progress'
    import bForm from 'bootstrap-vue/es/components/form/form'
    import bFormGroup from 'bootstrap-vue/es/components/form-group/form-group'
    import bFormRadioGroup from 'bootstrap-vue/es/components/form-radio/form-radio-group'
    import bFormFile from 'bootstrap-vue/es/components/form-file/form-file'
    export default {
        name: 'form-upload-dialog',
        data() {
            return {
                selectedUploadType: 'xlsform',
                uploadTypes: [
                    { text: 'XLSForm', value: 'xlsform'},
                    { text: 'XML + XLSForm', value: 'xml+xlsform' },
                    { text: 'XML', value: 'xml' }
                ],
                validated: null,
                message: null,
                error: null,
                fieldErrors: {},
                progress: null,
                xmlFile: null,
                xlsformFile: null,
                mediaFiles: []
            }
        },
        methods: {
            reset() {
                this.selectedUploadType = this.permittedUploadTypes[0].value
                this.validated = null
                this.message = null
                this.error = null
                this.fieldErrors = {}
                this.progress = null
                this.$refs.xmlInput.reset()
                this.$refs.xlsformInput.reset()
                this.$refs.mediaFilesInput.reset()
            },
            show() {
                this.$refs.modal.show()
            },
            hide() {
                this.$refs.modal.hide()
            },
            buildFormData() {
                let data = new FormData()
                if (this.isXmlUpload && this.xmlFile) {
                    data.append('form_def_file', this.xmlFile, this.xmlFile.name)
                }
                if (this.isXlsformUpload && this.xlsformFile) {
                    data.append('xlsform_def_file', this.xlsformFile, this.xlsformFile.name)
                }
                if (this.mediaFiles) {
                    this.mediaFiles.forEach(mf => data.append('media_file', mf, mf.name))
                }
                return data;
            },
            validate() {
                this.error = null
                this.message = null
                this.fieldErrors = {}
                if (this.isXmlUpload && !this.xmlFile) {
                    this.addFieldError('form_def_file', this.$t('forms.xmlreq'))
                }
                if (this.isXlsformUpload && !this.xlsformFile) {
                    this.addFieldError('xlsform_def_file', this.$t('forms.xlsreq'))
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
                    let rsp = await this.$xhr.post(this.uploadAction, formData, {
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
                this.reset()
                this.message = this.$t('forms.success')
                this.$emit('formUploaded')
            },
            uploadError(err) {
                let rsp = err.response
                if (rsp.status >= 400) {
                    let msg = this.$t('forms.failed')
                    if (rsp.status === 400 && rsp.data.output) {
                        let output = rsp.data.output
                        msg += output.reduce((acc,cur) => acc + '\n' + cur.message, ':\n')
                    }
                    this.error = msg
                }
            }
        },
        computed: {
            permittedUploadTypes() {
                return this.uploadTypes.filter(type =>
                    this.$can('FORM_UPLOAD') && type.value.startsWith('xml') ||
                    this.$can('FORM_UPLOAD_XLS') && type.value.startsWith('xls'))
            },
            isXmlUpload() {
                return (this.selectedUploadType || '').includes('xml')
            },
            isXlsformUpload() {
                return (this.selectedUploadType || '').includes('xlsform')
            },
            uploadAction() {
                return this.isXmlUpload? '/uploadXmlForm' : '/uploadXlsForm'
            },
            inProgress() {
                return this.progress != null
            },
            xmlState() {
                return this.validated != null && this.isXmlUpload? !this.xmlError : null
            },
            xmlError() {
                return (this.fieldErrors['form_def_file'] || []).join(' ')
            },
            xlsformState() {
                return this.validated != null && this.isXlsformUpload? !this.xlsformError : null
            },
            xlsformError() {
                return (this.fieldErrors['xlsform_def_file'] || []).join(' ')
            }
        },
        components: {
            bModal, bAlert, bProgress, bForm, bFormGroup, bFormRadioGroup, bFormFile
        }
    }
</script>

<style scoped>
    .b-form-file >>> .custom-file-label {
        overflow: hidden;
    }
</style>