package com.github.cimsbioko.server.service.impl

import com.github.cimsbioko.server.scripting.SubmissionRecord
import com.github.cimsbioko.server.service.SubmissionExportService
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.Transactional

open class SubmissionExportServiceImpl(
    private val sqlBuilder: ExportSQLBuilder,
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : SubmissionExportService {

    private val log = LoggerFactory.getLogger(SubmissionExportServiceImpl::class.java)

    private val currentSchema by lazy {
        var schema: String? = null
        jdbcTemplate.query("select current_schema") { resultSet -> schema = resultSet.getString(1) }
        schema ?: throw error("failed to retrieve current schema from database")
    }

    @Transactional
    override fun export(record: SubmissionRecord) {
        if (record.schema.equals(currentSchema, ignoreCase = true)) {
            log.warn("ignoring submission export to ${record.schema}.${record.table}, export into $currentSchema not allowed")
        } else {
            log.info("persisting record id: ${record.id}")
            with(sqlBuilder) {
                val tableDdl = createTableDdl(record)
                val exportSql = exportSubmissionSql(record)
                val parameters = exportSubmissionParams(record)
                with(jdbcTemplate) {
                    log.info("table: {}", tableDdl)
                    execute(tableDdl) { stmt -> stmt.execute() }
                    log.info("record: {}", exportSql)
                    val affected = update(exportSql, parameters)
                    log.info("$affected records affected")
                }
            }
        }
    }
}