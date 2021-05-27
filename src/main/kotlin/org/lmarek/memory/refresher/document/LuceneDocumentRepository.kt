package org.lmarek.memory.refresher.document

import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.Term
import org.apache.lucene.document.Document as LuceneDocument

private const val PATH_FIELD = "path"
private const val CONTENT_FIELD = "content"

class LuceneDocumentRepository(private val indexWriter: IndexWriter) : DocumentRepository {

    override fun save(documentToSave: DocumentToSave) {
        val luceneDocument = documentToSave.toLuceneDocument()
        val searchTerm = Term(PATH_FIELD, documentToSave.path)
        indexWriter.updateDocument(searchTerm, luceneDocument)
        indexWriter.commit()
    }

    private fun DocumentToSave.toLuceneDocument(): LuceneDocument {
        val luceneDocument = LuceneDocument()
        luceneDocument.add(TextField(CONTENT_FIELD, content, Field.Store.NO))
        luceneDocument.add(StringField(PATH_FIELD, path, Field.Store.YES))
        return luceneDocument
    }
}