<template>
    <b-form @submit.stop.prevent="search">
        <b-input-group>
            <b-form-input :value="value" @input="$emit('input', $event)" :placeholder="placeholder"/>
            <b-input-group-append>
                <b-button type="submit" variant="outline-primary"><fa-icon icon="search"/></b-button>
            </b-input-group-append>
        </b-input-group>
    </b-form>
</template>

<script>
    import bForm from 'bootstrap-vue/es/components/form/form'
    import bInputGroup from 'bootstrap-vue/es/components/input-group/input-group'
    import bFormInput from 'bootstrap-vue/es/components/form-input/form-input'
    import bInputGroupAppend from 'bootstrap-vue/es/components/input-group/input-group-append'
    import bButton from 'bootstrap-vue/es/components/button/button'
    export default {
        name: 'search-box',
        props: {
            value: {type: String},
            placeholder: {type: String}
        },
        methods: {
            search() {
                this.$emit('search', this.value)
            }
        },
        components: {
            bForm, bInputGroup, bFormInput, bInputGroupAppend, bButton
        }
    }
</script>