<template>
    <b-form @submit.stop.prevent="search">
        <b-input-group>
            <b-form-input :value="value" @input="$emit('input', $event)" placeholder="Search by name or fieldworker id"/>
            <b-input-group-append>
                <b-button type="submit" variant="outline-primary"><fa-icon icon="search"/></b-button>
            </b-input-group-append>
        </b-input-group>
    </b-form>
</template>

<script>
    export default {
        name: 'search-box',
        props: {
            value: {type: String}
        },
        methods: {
            search() {
                this.$emit('search', this.value)
            }
        }
    }
</script>