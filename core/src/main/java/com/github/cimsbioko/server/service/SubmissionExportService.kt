package com.github.cimsbioko.server.service

import com.github.cimsbioko.server.scripting.SubmissionRecord

interface SubmissionExportService {
    fun export(record: SubmissionRecord)
}