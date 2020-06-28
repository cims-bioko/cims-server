<template>
    <b-modal ref="modal" @show="reset" @ok.prevent="submit" :ok-disabled="inProgress">
        <template slot="modal-title">{{$t('campaigns.reprocessmodal.title', [binding])}}</template>
        <b-alert variant="danger" v-if="error" :show="5">{{error}}</b-alert>
        <b-progress v-if="inProgress" :value="100" striped animated/>
        {{$t('campaigns.reprocessmodal.desc', [binding])}}
        <template slot="modal-ok"><fa-icon icon="cogs"/> {{$t('campaigns.reprocessmodal.reprocess')}}</template>
        <template slot="modal-cancel">{{$t('campaigns.reprocessmodal.cancel')}}</template>
    </b-modal>
</template>

<script>
    import {BModal, BAlert, BProgress} from 'bootstrap-vue'
    export default {
        name: 'reprocess-dialog',
        props: {
            campaign: {
                type: String,
                required: true
            },
            binding: {
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
                    let rsp = await this.$xhr.delete(`/campaign/${this.campaign}/binding/${this.binding}`)
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