<template>
    <b-modal ref="modal" @show="clearData" @hidden="clearData" @ok.prevent="submit">
        <template slot="modal-title">Create Backup</template>
        <template v-for="(error, index) of errors">
            <b-alert variant="danger" :key="index" :show="5">{{error}}</b-alert>
        </template>
        <b-form ref="form" @submit.stop.prevent novalidate>
            <b-form-group label="Name" :invalid-feedback="nameError" :state="nameState">
                <b-form-input v-model="name" :state="nameState" @input="validate"/>
            </b-form-group>
            <b-form-group label="Description" :invalid-feedback="descriptionError" :state="descriptionState">
                <b-form-input v-model="description" :state="descriptionState" @input="validate"/>
            </b-form-group>
        </b-form>
        <template slot="modal-ok"><fa-icon icon="plus"/> Create</template>
    </b-modal>
</template>

<script>
    export default {
        name: 'backup-create-dialog',
        data() {
            return {
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
                this.errors = []
                this.fieldErrors = {}
                this.validateName()
                this.validateDescription()
                this.validated = Object.keys(this.fieldErrors).length === 0
                return this.validated
            },
            validateName() {
                if (!this.name) {
                    this.addFieldError('name', 'Name is required.')
                } else if (!this.name.match(/^[a-z][a-z0-9_]*$/)) {
                    this.addFieldError('name', 'Name must consist of lowercase letters, digits and \'_\'' +
                        ' and must start with a letter.')
                }
            },
            validateDescription() {
                if (!this.description) {
                    this.addFieldError('description', 'Description is required.')
                }
            },
            addFieldError(field, error) {
                this.fieldErrors[field] = this.fieldErrors[field] || []
                this.fieldErrors[field].push(error)
            },
            async submit(e) {
                if (this.validate()) {
                    try {
                        let data = {name: this.name, description: this.description}
                        let response = await this.$xhr.post('/backups', data)
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
                return (this.fieldErrors['name'] || []).join(' ')
            },
            descriptionState() {
                return this.validated == null? null : !this.descriptionError
            },
            descriptionError() {
                return (this.fieldErrors['description'] || []).join(' ')
            }
        }
    }
</script>
