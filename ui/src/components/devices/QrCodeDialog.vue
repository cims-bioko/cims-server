<template>
    <b-modal ref="modal" :title="$t('qrcode.title', [name])" ok-only>
        <b-img center :src="codeUrl" />
        <b-alert variant="warning" show>{{$t('qrcode.onetime')}}</b-alert>
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
        },
        components: {
            BModal, BImg, BAlert
        }
    }
</script>
