<template>
    <b-modal ref="modal" @show="reset" @ok.prevent="submit" :ok-disabled="inProgress">
        <template slot="modal-title">{{$t('forms.deletemodal.title')}}</template>
        <b-alert variant="danger" v-if="error" :show="5">{{error}}</b-alert>
        <b-progress v-if="inProgress" :value="100" striped animated/>
        {{$t('forms.deletemodal.desc', [id, version])}}
        <template slot="modal-ok"><fa-icon icon="trash-alt"/> {{$t('forms.deletemodal.delete')}}</template>
        <template slot="modal-cancel">{{$t('forms.deletemodal.cancel')}}</template>
    </b-modal>
</template>

<script>
    import {BModal, BAlert, BProgress} from 'bootstrap-vue'
    export default {
        name: 'form-delete-dialog',
        props: {
            id: {
                type: String,
                required: true
            },
            version: {
                type: String,
                required: true
            }
        },
        data() {
            return {
                error: null,
                inProgress: false
            }
        },
        methods: {
            reset() {
                this.error = null
                this.inProgress = false
            },
            show() {
                this.$refs.modal.show()
            },
            hide() {
                this.$refs.modal.hide()
            },
            async submit() {
                try {
                    this.inProgress = true
                    let rsp = await this.$xhr.delete(`/forms/${this.id}/${this.version}`)
                    this.inProgress = false
                    this.hide()
                    this.$emit('ok', rsp.data)
                } catch (err) {
                    this.error = err
                }
            },
        },
        components: {
            BModal, BAlert, BProgress
        }
    }
</script>

<style scoped>
</style>