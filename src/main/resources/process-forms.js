var formService, batchSize = 300;

function setFormService(fs) {
    formService = fs;
}

function processForms() {
    var forms = formService.getUnprocessed(batchSize), processed = 0;
    for (var f = 0; f < forms.length; f++) {
        process(forms[f]);
        processed += 1;
    }
    return processed;
}

function process(submission) {
    var form = JSON.parse(submission.json);
    print(form.data.meta.instanceID);
    formService.markProcessed(submission);
}


