<template>
  <b-container>
    <b-row>
      <b-col>
        <template v-for="(message, index) of messages">
          <b-alert variant="success" :show="15" :key="`message-${index}`" fade>{{message}}</b-alert>
        </template>
      </b-col>
    </b-row>
    <b-row class="align-items-center">
      <b-col class="col-auto">
        <h1><fa-icon icon="file-upload"/> {{$t('nav.submissions')}}</h1>
      </b-col>
      <b-col>
        <search-box :placeholder="$t('submissions.searchph')" v-model="searchQuery" @search="search" />
      </b-col>
    </b-row>
    <b-row v-if="totalItems > pageSize">
      <b-col>
        <b-pagination align="center" v-model="currentPage" :total-rows="totalItems" :per-page="pageSize" @change="loadPage"/>
      </b-col>
    </b-row>
    <b-row>
      <b-col>
        <b-table :items="items" :fields="fields" show-empty :empty-text="$t('table.empty')">
          <template slot="formId" slot-scope="data">
            {{ data.value === data.item.formBinding? data.value : $t("forms.nameFormat", [data.value, data.item.formBinding]) }}
          </template>
          <template slot="processedOk" slot-scope="data">
            <fa-icon :id="`processed-icon-${data.item.instanceId}`" :icon="data.value? 'check-circle' : 'times-circle'"/>
            <submission-error v-if="!data.value" :target="`processed-icon-${data.item.instanceId}`" :id="data.item.instanceId"/>
          </template>
          <template slot="submitted" slot-scope="data">{{data.value|formatDateTime}}</template>
          <template slot="actions" slot-scope="data">
            <b-button-group>
              <b-button v-if="$can('EDIT_SUBMISSIONS') && !data.item.deleted" variant="outline-primary" @click="launchEditor(data.item.instanceId)">
                <fa-icon icon="edit"/>
              </b-button>
              <b-button v-if="$can('DELETE_SUBMISSIONS') && !data.item.deleted" variant="outline-primary" @click="showDeleteDialog(data.index)">
                <fa-icon icon="trash-alt"/>
              </b-button>
              <b-button v-if="$can('REPROCESS_SUBMISSIONS')" variant="outline-primary" @click="showReprocessDialog(data.index)">
                <fa-icon icon="cogs"/>
              </b-button>
              <b-button variant="outline-primary" @click="data.toggleDetails"><fa-icon :icon="data.detailsShowing? 'angle-up' : 'angle-down'"/></b-button>
            </b-button-group>
            <submission-delete-dialog v-if="$can('DELETE_SUBMISSIONS')" :ref="`deleteDialog${data.index}`"
                                      :id="data.item.instanceId" @ok="deleteOk"/>
            <submission-reprocess-dialog v-if="$can('REPROCESS_SUBMISSIONS')" :ref="`reprocessDialog${data.index}`"
                                      :id="data.item.instanceId" @ok="reprocessOk"/>
          </template>
          <template slot="row-details" slot-scope="data">
            <submission-details :id="data.item.instanceId"/>
          </template>
        </b-table>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import {BContainer, BRow, BCol, BAlert, BButton, BPagination, BTable, BButtonGroup} from 'bootstrap-vue'
import SearchBox from '../SearchBox'
import SubmissionDetails from './SubmissionDetails'
import SubmissionDeleteDialog from "./DeleteDialog"
import SubmissionReprocessDialog from './ReprocessDialog'
import SubmissionError from "./SubmissionError"
export default {
  name: 'submissions-page',
  data() {
    return {
      fields: [
        {key: 'instanceId', label: this.$t('submissions.id'), tdClass: 'align-middle'},
        {key: 'formId', label: this.$t('submissions.form'), tdClass: 'align-middle'},
        {key: 'deviceId', label: this.$t('submissions.device'), tdClass: 'align-middle'},
        {key: 'submitted', label: this.$t('submissions.submitted'), tdClass: 'align-middle'},
        {key: 'processedOk', label: this.$t('submissions.processed'), tdClass: 'align-middle text-center'},
        {key: 'actions', label: this.$t('submissions.actions'), tdClass: 'align-middle'}
      ],
      errors: [],
      messages: [],
      searchQuery: '',
      totalItems: 0,
      pageSize: 0,
      currentPage: 1,
      items: []
    }
  },
  methods: {
    async loadPage(page) {
      let params = {p: page - 1}
      if (this.searchQuery) {
        params.q = this.searchQuery
      }
      let rsp = await this.$xhr.get('/submissions', {params: params})
      let data = rsp.data
      this.items = data.content
      this.totalItems = data.totalElements
      this.currentPage = data.pageable.pageNumber + 1
      this.pageSize = data.size
      if (this.items.length === 0 && this.currentPage > 1) {
        this.loadPage(this.currentPage - 1)
      }
    },
    reloadPage() {
      this.loadPage(this.currentPage)
    },
    showMessages(messages) {
      // ensures same message triggers new alert
      this.messages = []
      this.$nextTick(() => this.messages = messages || [])
    },
    showDeleteDialog(index) {
      this.$refs[`deleteDialog${index}`].show()
    },
    showReprocessDialog(index) {
      this.$refs[`reprocessDialog${index}`].show()
    },
    deleteOk(rsp) {
      this.showMessages(rsp.messages)
      this.reloadPage()
    },
    reprocessOk(rsp) {
      this.showMessages(rsp.messages)
      this.reloadPage()
    },
    search() {
      this.reloadPage()
    },
    async launchEditor(instanceId) {
      let rsp = await this.$xhr.post(`/submissions/${instanceId}/edit`)
      window.location.href = rsp.data.data.uri
    }
  },
  mounted() {
    this.reloadPage()
  },
  components: {
    BContainer, BRow, BCol, BAlert, BButton, BPagination, BTable, BButtonGroup, SearchBox, SubmissionDetails, SubmissionDeleteDialog,
    SubmissionError, SubmissionReprocessDialog
  }
}
</script>

<style scoped>
>>> .b-table tr.deleted td {
  text-decoration-line: line-through;
}
.fa-check-circle {
  color: green;
}
.fa-times-circle {
  color: red;
}
</style>
