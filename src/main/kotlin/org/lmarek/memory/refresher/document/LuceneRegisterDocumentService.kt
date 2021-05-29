package org.lmarek.memory.refresher.document

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.Term
import org.apache.lucene.document.Document as LuceneDocument

private const val PATH_FIELD = "path"
private const val CONTENT_FIELD = "content"

class LuceneRegisterDocumentService(private val indexWriter: IndexWriter) : RegisterDocumentService {

    override suspend fun register(document: Document) {
        withContext(Dispatchers.IO) {
            val luceneDocument = document.toLuceneDocument()
            val searchTerm = Term(PATH_FIELD, document.path.value)
            indexWriter.updateDocument(searchTerm, luceneDocument)
            yield()
            indexWriter.commit()
        }
    }

    override suspend fun unregister(path: DocumentPath) {
        withContext(Dispatchers.IO) {
            val searchTerm = Term(PATH_FIELD, path.value)
            indexWriter.deleteDocuments(searchTerm)
            yield()
            indexWriter.commit()
        }
    }

    private fun Document.toLuceneDocument(): LuceneDocument {
        val luceneDocument = LuceneDocument()
        luceneDocument.add(TextField(CONTENT_FIELD, content, Field.Store.NO))
        luceneDocument.add(StringField(PATH_FIELD, path.value, Field.Store.YES))
        return luceneDocument
    }
}