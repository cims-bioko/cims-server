<template>
    <b-modal ref="modal" @show="reset" @ok.prevent="submit" :ok-disabled="inProgress">
        <template slot="modal-title">Upload Form</template>
        <b-alert variant="danger" v-if="error" :show="5">{{error}}</b-alert>
        <b-alert variant="success" v-if="message" :show="5">{{message}}</b-alert>
        <b-progress v-if="inProgress" :value="progress" striped animated/>
        <b-form ref="form" v-show="!inProgress" @submit.stop.prevent novalidate>
            <b-form-group label="Upload Type">
                <b-form-radio-group button-variant="outline-secondary" buttons
                                    v-model="selectedUploadType" :options="uploadTypes" @change="validated=null"/>
            </b-form-group>
            <b-form-group label="XML" :invalid-feedback="xmlError" :state="xmlState">
                <b-form-file ref="xmlInput" v-model="xmlFile" :disabled="!isXmlUpload"
                        accept="text/xml,application/xml" :state="xmlState"/>
            </b-form-group>
            <b-form-group label="XLSForm" :invalid-feedback="xlsformError" :state="xlsformState">
                <b-form-file ref="xlsformInput" v-model="xlsformFile" :disabled="!isXlsformUpload"
                        accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" :state="xlsformState"/>
            </b-form-group>
            <b-form-group label="Media Files">
                <b-form-file ref="mediaFilesInput" v-model="mediaFiles" multiple/>
            </b-form-group>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="upload"/> Upload</template>
        <template slot="modal-cancel">Close</template>
    </b-modal>
</template>

<script>
    import axios from 'axios'
    export default {
        name: 'form-upload-dialog',
        data() {
            return {
                selectedUploadType: 'xml',
                uploadTypes: [
                    { text: 'XML', value: 'xml' },
                    { text: 'XML + XLSForm', value: 'xml+xlsform' },
                    { text: 'XLSForm', value: 'xlsform'}
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
                this.selectedUploadType = 'xml'
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
                    this.addFieldError('form_def_file', 'XML form is required.')
                }
                if (this.isXlsformUpload && !this.xlsformFile) {
                    this.addFieldError('xlsform_def_file', 'XLSForm is required.')
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
                    let rsp = await axios.post(this.uploadAction, formData, {
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
                this.message = 'Upload succeeded'
                this.$emit('formUploaded')
            },
            uploadError(err) {
                if (err.response.status === 400) {
                    this.error = 'Upload failed'
                }
            }
        },
        computed: {
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
        }
    }
</script>

<style scoped>
    .b-form-file >>> .custom-file-label {
        overflow: hidden;
    }
</style>