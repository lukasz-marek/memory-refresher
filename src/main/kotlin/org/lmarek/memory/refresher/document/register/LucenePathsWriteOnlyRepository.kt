package org.lmarek.memory.refresher.document.register

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.Term
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import org.apache.lucene.document.Document as LuceneDocument

private const val PATH_FIELD = "path"
private const val CONTENT_FIELD = "content"

class LucenePathsWriteOnlyRepository(private val indexWriter: IndexWriter) : PathsWriteOnlyRepository {

    override suspend fun register(document: Document) {
        withContext(Dispatchers.IO) {
            registerOne(document)
            yield()
            indexWriter.commit()
        }
    }

    override suspend fun register(documents: Flow<Document>) {
        withContext(Dispatchers.IO) {
            documents.collect {
                registerOne(it)
                yield()
            }
            indexWriter.commit()
        }
    }

    override suspend fun unregister(paths: Flow<DocumentPath>) {
        withContext(Dispatchers.IO) {
            paths.collect {
                unregisterOne(it)
                yield()
            }
            indexWriter.commit()
        }
    }

    override suspend fun unregister(path: DocumentPath) {
        withContext(Dispatchers.IO) {
            unregisterOne(path)
            yield()
            indexWriter.commit()
        }
    }

    private fun registerOne(document: Document) {
        val luceneDocument = document.toLuceneDocument()
        val searchTerm = Term(PATH_FIELD, document.path.value)
        indexWriter.updateDocument(searchTerm, luceneDocument)
    }

    private fun unregisterOne(path: DocumentPath) {
        val searchTerm = Term(PATH_FIELD, path.value)
        indexWriter.deleteDocuments(searchTerm)
    }

    private fun Document.toLuceneDocument(): LuceneDocument {
        val luceneDocument = LuceneDocument()
        luceneDocument.add(TextField(CONTENT_FIELD, content, Field.Store.NO))
        luceneDocument.add(StringField(PATH_FIELD, path.value, Field.Store.YES))
        return luceneDocument
    }
}