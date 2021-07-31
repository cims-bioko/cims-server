package com.github.cimsbioko.server.service.impl

import com.github.cimsbioko.server.scripting.SubmissionRecord

interface IdNamingStrategy {
    fun safeIdName(name: String): String
}

class PostgresqlIdNamingStrategy : IdNamingStrategy {
    companion object {
        val ID_REGEX = """^[\p{L}_][\p{L}\p{M}_0-9${'$'}]*$""".toRegex()
        val RESERVED_WORDS = setOf(
            "A", "ABORT", "ABS", "ABSENT", "ABSOLUTE", "ACCESS", "ACCORDING", "ACTION", "ADA", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALL",
            "ALLOCATE", "ALSO", "ALTER", "ALWAYS", "ANALYSE", "ANALYZE", "AND", "ANY", "ARE", "ARRAY", "ARRAY_AGG", "ARRAY_MAX_CARDINALITY",
            "AS", "ASC", "ASENSITIVE", "ASSERTION", "ASSIGNMENT", "ASYMMETRIC", "AT", "ATOMIC", "ATTRIBUTE", "ATTRIBUTES", "AUTHORIZATION",
            "AVG", "BACKWARD", "BASE64", "BEFORE", "BEGIN", "BEGIN_FRAME", "BEGIN_PARTITION", "BERNOULLI", "BETWEEN", "BIGINT", "BINARY",
            "BIT", "BIT_LENGTH", "BLOB", "BLOCKED", "BOM", "BOOLEAN", "BOTH", "BREADTH", "BY", "C", "CACHE", "CALL", "CALLED",
            "CARDINALITY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CATALOG_NAME", "CEIL", "CEILING", "CHAIN", "CHAR",
            "CHARACTER", "CHARACTERISTICS", "CHARACTERS", "CHARACTER_LENGTH", "CHARACTER_SET_CATALOG", "CHARACTER_SET_NAME",
            "CHARACTER_SET_SCHEMA", "CHAR_LENGTH", "CHECK", "CHECKPOINT", "CLASS", "CLASS_ORIGIN", "CLOB", "CLOSE", "CLUSTER", "COALESCE",
            "COBOL", "COLLATE", "COLLATION", "COLLATION_CATALOG", "COLLATION_NAME", "COLLATION_SCHEMA", "COLLECT", "COLUMN", "COLUMNS",
            "COLUMN_NAME", "COMMAND_FUNCTION", "COMMAND_FUNCTION_CODE", "COMMENT", "COMMENTS", "COMMIT", "COMMITTED", "CONCURRENTLY",
            "CONDITION", "CONDITION_NUMBER", "CONFIGURATION", "CONFLICT", "CONNECT", "CONNECTION", "CONNECTION_NAME", "CONSTRAINT",
            "CONSTRAINTS", "CONSTRAINT_CATALOG", "CONSTRAINT_NAME", "CONSTRAINT_SCHEMA", "CONSTRUCTOR", "CONTAINS", "CONTENT", "CONTINUE",
            "CONTROL", "CONVERSION", "CONVERT", "COPY", "CORR", "CORRESPONDING", "COST", "COUNT", "COVAR_POP", "COVAR_SAMP", "CREATE",
            "CROSS", "CSV", "CUBE", "CUME_DIST", "CURRENT", "CURRENT_CATALOG", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP",
            "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_ROW", "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP",
            "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CURSOR_NAME", "CYCLE", "DATA", "DATABASE", "DATALINK", "DATE",
            "DATETIME_INTERVAL_CODE", "DATETIME_INTERVAL_PRECISION", "DAY", "DB", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT",
            "DEFAULTS", "DEFERRABLE", "DEFERRED", "DEFINED", "DEFINER", "DEGREE", "DELETE", "DELIMITER", "DELIMITERS", "DENSE_RANK",
            "DEPTH", "DEREF", "DERIVED", "DESC", "DESCRIBE", "DESCRIPTOR", "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY", "DISABLE",
            "DISCARD", "DISCONNECT", "DISPATCH", "DISTINCT", "DLNEWCOPY", "DLPREVIOUSCOPY", "DLURLCOMPLETE", "DLURLCOMPLETEONLY",
            "DLURLCOMPLETEWRITE", "DLURLPATH", "DLURLPATHONLY", "DLURLPATHWRITE", "DLURLSCHEME", "DLURLSERVER", "DLVALUE", "DO", "DOCUMENT",
            "DOMAIN", "DOUBLE", "DROP", "DYNAMIC", "DYNAMIC_FUNCTION", "DYNAMIC_FUNCTION_CODE", "EACH", "ELEMENT", "ELSE", "EMPTY",
            "ENABLE", "ENCODING", "ENCRYPTED", "END", "END-EXEC", "END_FRAME", "END_PARTITION", "ENFORCED", "ENUM", "EQUALS", "ESCAPE",
            "EVENT", "EVERY", "EXCEPT", "EXCEPTION", "EXCLUDE", "EXCLUDING", "EXCLUSIVE", "EXEC", "EXECUTE", "EXISTS", "EXP", "EXPLAIN",
            "EXPRESSION", "EXTENSION", "EXTERNAL", "EXTRACT", "FALSE", "FAMILY", "FETCH", "FILE", "FILTER", "FINAL", "FIRST", "FIRST_VALUE",
            "FLAG", "FLOAT", "FLOOR", "FOLLOWING", "FOR", "FORCE", "FOREIGN", "FORTRAN", "FORWARD", "FOUND", "FRAME_ROW", "FREE", "FREEZE",
            "FROM", "FS", "FULL", "FUNCTION", "FUNCTIONS", "FUSION", "G", "GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT",
            "GRANTED", "GREATEST", "GROUP", "GROUPING", "GROUPS", "HANDLER", "HAVING", "HEADER", "HEX", "HIERARCHY", "HOLD", "HOUR", "ID",
            "IDENTITY", "IF", "IGNORE", "ILIKE", "IMMEDIATE", "IMMEDIATELY", "IMMUTABLE", "IMPLEMENTATION", "IMPLICIT", "IMPORT", "IN",
            "INCLUDING", "INCREMENT", "INDENT", "INDEX", "INDEXES", "INDICATOR", "INHERIT", "INHERITS", "INITIALLY", "INLINE", "INNER",
            "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INSTANCE", "INSTANTIABLE", "INSTEAD", "INT", "INTEGER", "INTEGRITY", "INTERSECT",
            "INTERSECTION", "INTERVAL", "INTO", "INVOKER", "IS", "ISNULL", "ISOLATION", "JOIN", "K", "KEY", "KEY_MEMBER", "KEY_TYPE",
            "LABEL", "LAG", "LANGUAGE", "LARGE", "LAST", "LAST_VALUE", "LATERAL", "LEAD", "LEADING", "LEAKPROOF", "LEAST", "LEFT", "LENGTH",
            "LEVEL", "LIBRARY", "LIKE", "LIKE_REGEX", "LIMIT", "LINK", "LISTEN", "LN", "LOAD", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP",
            "LOCATION", "LOCATOR", "LOCK", "LOCKED", "LOGGED", "LOWER", "M", "MAP", "MAPPING", "MATCH", "MATCHED", "MATERIALIZED", "MAX",
            "MAXVALUE", "MAX_CARDINALITY", "MEMBER", "MERGE", "MESSAGE_LENGTH", "MESSAGE_OCTET_LENGTH", "MESSAGE_TEXT", "METHOD", "MIN",
            "MINUTE", "MINVALUE", "MOD", "MODE", "MODIFIES", "MODULE", "MONTH", "MORE", "MOVE", "MULTISET", "MUMPS", "NAME", "NAMES",
            "NAMESPACE", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NESTING", "NEW", "NEXT", "NFC", "NFD", "NFKC", "NFKD", "NIL", "NO",
            "NONE", "NORMALIZE", "NORMALIZED", "NOT", "NOTHING", "NOTIFY", "NOTNULL", "NOWAIT", "NTH_VALUE", "NTILE", "NULL", "NULLABLE",
            "NULLIF", "NULLS", "NUMBER", "NUMERIC", "OBJECT", "OCCURRENCES_REGEX", "OCTETS", "OCTET_LENGTH", "OF", "OFF", "OFFSET", "OIDS",
            "OLD", "ON", "ONLY", "OPEN", "OPERATOR", "OPTION", "OPTIONS", "OR", "ORDER", "ORDERING", "ORDINALITY", "OTHERS", "OUT", "OUTER",
            "OUTPUT", "OVER", "OVERLAPS", "OVERLAY", "OVERRIDING", "OWNED", "OWNER", "P", "PAD", "PARAMETER", "PARAMETER_MODE",
            "PARAMETER_NAME", "PARAMETER_ORDINAL_POSITION", "PARAMETER_SPECIFIC_CATALOG", "PARAMETER_SPECIFIC_NAME",
            "PARAMETER_SPECIFIC_SCHEMA", "PARSER", "PARTIAL", "PARTITION", "PASCAL", "PASSING", "PASSTHROUGH", "PASSWORD", "PATH",
            "PERCENT", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERCENT_RANK", "PERIOD", "PERMISSION", "PLACING", "PLANS", "PLI", "POLICY",
            "PORTION", "POSITION", "POSITION_REGEX", "POWER", "PRECEDES", "PRECEDING", "PRECISION", "PREPARE", "PREPARED", "PRESERVE",
            "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURAL", "PROCEDURE", "PROGRAM", "PUBLIC", "QUOTE", "RANGE", "RANK", "READ", "READS",
            "REAL", "REASSIGN", "RECHECK", "RECOVERY", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "REFRESH", "REGR_AVGX", "REGR_AVGY",
            "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "REINDEX", "RELATIVE", "RELEASE",
            "RENAME", "REPEATABLE", "REPLACE", "REPLICA", "REQUIRING", "RESET", "RESPECT", "RESTART", "RESTORE", "RESTRICT", "RESULT",
            "RETURN", "RETURNED_CARDINALITY", "RETURNED_LENGTH", "RETURNED_OCTET_LENGTH", "RETURNED_SQLSTATE", "RETURNING", "RETURNS",
            "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", "ROUTINE_CATALOG", "ROUTINE_NAME", "ROUTINE_SCHEMA", "ROW",
            "ROWS", "ROW_COUNT", "ROW_NUMBER", "RULE", "SAVEPOINT", "SCALE", "SCHEMA", "SCHEMA_NAME", "SCOPE", "SCOPE_CATALOG",
            "SCOPE_NAME", "SCOPE_SCHEMA", "SCROLL", "SEARCH", "SECOND", "SECTION", "SECURITY", "SELECT", "SELECTIVE", "SELF", "SENSITIVE",
            "SEQUENCE", "SEQUENCES", "SERIALIZABLE", "SERVER", "SERVER_NAME", "SESSION", "SESSION_USER", "SET", "SETOF", "SETS", "SHARE",
            "SHOW", "SIMILAR", "SIMPLE", "SIZE", "SKIP", "SMALLINT", "SNAPSHOT", "SOME", "SOURCE", "SPACE", "SPECIFIC", "SPECIFICTYPE",
            "SPECIFIC_NAME", "SQL", "SQLCODE", "SQLERROR", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQRT", "STABLE", "STANDALONE",
            "START", "STATE", "STATEMENT", "STATIC", "STATISTICS", "STDDEV_POP", "STDDEV_SAMP", "STDIN", "STDOUT", "STORAGE", "STRICT",
            "STRIP", "STRUCTURE", "STYLE", "SUBCLASS_ORIGIN", "SUBMULTISET", "SUBSTRING", "SUBSTRING_REGEX", "SUCCEEDS", "SUM", "SYMMETRIC",
            "SYSID", "SYSTEM", "SYSTEM_TIME", "SYSTEM_USER", "T", "TABLE", "TABLES", "TABLESAMPLE", "TABLESPACE", "TABLE_NAME", "TEMP",
            "TEMPLATE", "TEMPORARY", "TEXT", "THEN", "TIES", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TOKEN",
            "TOP_LEVEL_COUNT", "TRAILING", "TRANSACTION", "TRANSACTIONS_COMMITTED", "TRANSACTIONS_ROLLED_BACK", "TRANSACTION_ACTIVE",
            "TRANSFORM", "TRANSFORMS", "TRANSLATE", "TRANSLATE_REGEX", "TRANSLATION", "TREAT", "TRIGGER", "TRIGGER_CATALOG", "TRIGGER_NAME",
            "TRIGGER_SCHEMA", "TRIM", "TRIM_ARRAY", "TRUE", "TRUNCATE", "TRUSTED", "TYPE", "TYPES", "UESCAPE", "UNBOUNDED", "UNCOMMITTED",
            "UNDER", "UNENCRYPTED", "UNION", "UNIQUE", "UNKNOWN", "UNLINK", "UNLISTEN", "UNLOGGED", "UNNAMED", "UNNEST", "UNTIL", "UNTYPED",
            "UPDATE", "UPPER", "URI", "USAGE", "USER", "USER_DEFINED_TYPE_CATALOG", "USER_DEFINED_TYPE_CODE", "USER_DEFINED_TYPE_NAME",
            "USER_DEFINED_TYPE_SCHEMA", "USING", "VACUUM", "VALID", "VALIDATE", "VALIDATOR", "VALUE", "VALUES", "VALUE_OF", "VARBINARY",
            "VARCHAR", "VARIADIC", "VARYING", "VAR_POP", "VAR_SAMP", "VERBOSE", "VERSION", "VERSIONING", "VIEW", "VIEWS", "VOLATILE",
            "WHEN", "WHENEVER", "WHERE", "WHITESPACE", "WIDTH_BUCKET", "WINDOW", "WITH", "WITHIN", "WITHOUT", "WORK", "WRAPPER", "WRITE",
            "XML", "XMLAGG", "XMLATTRIBUTES", "XMLBINARY", "XMLCAST", "XMLCOMMENT", "XMLCONCAT", "XMLDECLARATION", "XMLDOCUMENT",
            "XMLELEMENT", "XMLEXISTS", "XMLFOREST", "XMLITERATE", "XMLNAMESPACES", "XMLPARSE", "XMLPI", "XMLQUERY", "XMLROOT", "XMLSCHEMA",
            "XMLSERIALIZE", "XMLTABLE", "XMLTEXT", "XMLVALIDATE", "YEAR", "YES", "ZONE"
        )
    }

    private fun isUnquotedIdentifier(col: String): Boolean = ID_REGEX.matches(col)

    override fun safeIdName(name: String): String =
        if (isUnquotedIdentifier(name) && name.toUpperCase() !in RESERVED_WORDS) name
        else '"' + name.replace("\"", "\"\"") + '"'
}

interface ParamNamingStrategy {
    fun safeParamName(name: String): String
}

class JdbcParamNamingStrategy : ParamNamingStrategy {
    companion object {
        val NON_PARAM_CHARS_REGEX = """[^\p{L}\p{M}_0-9${'$'}]""".toRegex()
    }

    override fun safeParamName(name: String) = name.replace(NON_PARAM_CHARS_REGEX, "_")
}

interface ExportSQLBuilder {
    fun createTableDdl(submission: SubmissionRecord): String
    fun exportSubmissionSql(submission: SubmissionRecord): String
    fun exportSubmissionParams(submission: SubmissionRecord): Map<String, Any>
}

class ExportSQLBuilderImpl(
    private val idNamingStrategy: IdNamingStrategy,
    private val paramNamingStrategy: ParamNamingStrategy
) : ExportSQLBuilder {

    companion object {
        const val ID_COL = "_id"
        const val PARENT_COL = "_parent"
    }

    override fun createTableDdl(submission: SubmissionRecord): String {
        return with(submission) {
            buildString {
                append("create table if not exists cims.${idNamingStrategy.safeIdName(table)} (")
                append("$ID_COL varchar primary key")
                for (key in fields.keys) {
                    append(",")
                    append("${idNamingStrategy.safeIdName(key)} varchar")
                }
                parent?.also { parent ->
                    append(",")
                    append(
                        "$PARENT_COL varchar not null references cims.${idNamingStrategy.safeIdName(parent.table)}($ID_COL) on delete cascade"
                    )
                }
                refs.forEach { (name, ref) ->
                    append(",")
                    append(
                        "${idNamingStrategy.safeIdName(name)} varchar references cims.${idNamingStrategy.safeIdName(ref.table)}($ID_COL) on delete set null"
                    )
                }
                append(")")
            }
        }
    }

    override fun exportSubmissionSql(submission: SubmissionRecord): String {
        return with(submission) {
            val cols = ArrayList<String>().apply {
                addAll(fields.keys)
                if (parent != null) add(PARENT_COL)
                addAll(refs.keys)
            }
            buildString {
                append("insert into cims.${idNamingStrategy.safeIdName(table)} ($ID_COL")
                for (col in cols) {
                    append(",")
                    append(idNamingStrategy.safeIdName(col))
                }
                append(") values (:$ID_COL")
                for (col in cols) {
                    append(",")
                    append(":${paramNamingStrategy.safeParamName(col)}")
                }
                append(") on conflict ($ID_COL) do update set ")
                for ((index, col) in cols.withIndex()) {
                    append("${idNamingStrategy.safeIdName(col)} = excluded.${idNamingStrategy.safeIdName(col)}")
                    if (index != cols.indices.last) append(",")
                }
            }
        }
    }

    override fun exportSubmissionParams(submission: SubmissionRecord): Map<String, Any> = LinkedHashMap<String, Any>().apply {
        put(ID_COL, submission.id)
        putAll(submission.fields)
        submission.parent?.let { put(PARENT_COL, it.id) }
        submission.refs.forEach { put(it.key, it.value) }
    }.map { (key, value) -> paramNamingStrategy.safeParamName(key) to value }.toMap()
}