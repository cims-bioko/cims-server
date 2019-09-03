<template>
    <b-modal ref="modal" :title="$t('qrcode.title', [name])" @cancel.prevent="forget">
        <b-img center :src="codeUrl" />
        <b-alert variant="warning" show>{{$t('qrcode.onetime')}}</b-alert>
        <template slot="modal-cancel">{{$t('qrcode.forget')}}</template>
        <template slot="modal-ok">{{$t('qrcode.hide')}}</template>
    </b-modal>
</template>

<script>
    import {BModal, BImg, BAlert} from 'bootstrap-vue'
    export default {
        name: 'qr-code-dialog',
        props: {
            name: {
                type: String,
                required: true
            },
            secret: {
                type: String,
                required: true
            }
        },
        computed: {
            codeUrl() {
                return `/mcfg?name=${this.name}&secret=${this.secret}`;
            }
        },
        methods: {
            show() {
                this.$refs.modal.show()
            },
            forget() {
                this.$emit('forget', this.name);
            }
        },
        components: {
            BModal, BImg, BAlert
        }
    }
</script>
