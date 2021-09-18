package com.github.cimsbioko.server.service

import com.github.cimsbioko.server.config.CachingConfig.FORM_METADATA_CACHE
import com.github.cimsbioko.server.dao.FormRepository
import com.github.cimsbioko.server.domain.FormId
import org.jdom2.Document
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable

interface Schema {
    val root: String
    val fields: List<Field>
    val repeats: List<Field>
    val selects: List<Field>
}

data class SchemaImpl(
    override val root: String,
    override val fields: List<Field>
) : Schema {
    override val repeats = fields.findRecursive { field -> field.type == "REPEAT" }
    override val selects = fields.findRecursive { field -> field.type == "SELECT_ONE" || field.type == "SELECT" }
}

fun List<Field>.findRecursive(matches: (Field) -> Boolean): List<Field> = flatMap { field ->
    listOfNotNull(field.takeIf(matches)) + (field.takeUnless { it.isLeaf }?.children?.findRecursive(matches) ?: emptyList())
}

interface Field {
    val name: String
    val path: String
    val order: Int
    val type: String
    val children: List<Field>
    val isLeaf: Boolean
        get() = children.isEmpty()
}

data class FieldImpl(
    override val name: String,
    override val path: String,
    override val order: Int,
    override val type: String,
    override val children: MutableList<FieldImpl> = mutableListOf()
) : Field

private fun FormField.toFieldImpl() = FieldImpl(
    name = name,
    path = path,
    order = order,
    type = type.name
)

interface SchemaExtractor {
    fun extractSchema(doc: Document): Schema
}

private inline fun <E> ArrayDeque<E>.dropLast(predicate: (E) -> Boolean) {
    while (lastOrNull()?.let { predicate(it) } == true) removeLast()
}

class SchemaExtractorImpl(
    private val xmlExtractor: FormXmlExtractor = FormXmlExtractorImpl(),
    private val fieldExtractor: FormFieldExtractor = FormFieldExtractorImpl()
) : SchemaExtractor {
    override fun extractSchema(doc: Document): Schema {
        val resultFields: MutableList<FieldImpl> = mutableListOf()
        val parents: ArrayDeque<FieldImpl> = ArrayDeque()
        for (field in fieldExtractor.extractFields(doc)) {
            val f = field.toFieldImpl()
            parents.dropLast { !f.path.startsWith(it.path) }
            parents.lastOrNull()?.children?.add(f) ?: resultFields.add(f)
            when (field.type) {
                Type.REPEAT, Type.STRUCTURE -> {
                    parents.addLast(f)
                }
                else -> {
                }
            }
        }
        val rootName = with(xmlExtractor) { extractModel(doc)?.let { extractMainInstance(it) }?.name ?: "" }
        return SchemaImpl(rootName, resultFields)
    }
}

interface FormMetadataService {
    fun getFormSchema(id: FormId): Schema?
    fun getRepeats(id: FormId): List<Array<String>>
    fun invalidateMetadata(id: FormId)
}

open class FormMetadataServiceImpl(
    private val formRepo: FormRepository,
    private val schemaExtractor: SchemaExtractor
) : FormMetadataService {

    companion object {
        private val log = LoggerFactory.getLogger(FormMetadataServiceImpl::class.java)
    }

    @Cacheable(value = [FORM_METADATA_CACHE], key = "{#id.id,#id.version}")
    override fun getFormSchema(id: FormId): Schema? =
        formRepo.findById(id)
            .map { schemaExtractor.extractSchema(it.xml) }
            .orElse(null)

    @Cacheable(value = [FORM_METADATA_CACHE], key = "{#id.id,#id.version}")
    override fun getRepeats(id: FormId): List<Array<String>> {
        return getFormSchema(id)
            ?.let { schema ->
                schema.repeats
                    .filter { field -> field.path.startsWith('/') }
                    .map { field -> arrayOf(schema.root, *field.path.substring(1).split("/").toTypedArray()) }
            }.orEmpty()
    }

    @CacheEvict(value = [FORM_METADATA_CACHE], key = "{#id.id,#id.version}")
    override fun invalidateMetadata(id: FormId) {
        log.info("invalidating metadata for form $id")
    }
}

interface ScriptableFormMetadataService {
    fun getFormSchema(id: String, version: String): Schema?
}

class ScriptableFormMetadataServiceImpl(private val metadataService: FormMetadataService) : ScriptableFormMetadataService {
    override fun getFormSchema(id: String, version: String): Schema? = metadataService.getFormSchema(FormId(id, version))
}