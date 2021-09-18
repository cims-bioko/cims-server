package com.github.cimsbioko.server.scripting

interface SubmissionRecord {
    val schema: String
    val table: String
    val id: String
    val fields: Map<String, Any>
    val parent: SubmissionRecord?
    val refs: Map<String, SubmissionRecord>
}

