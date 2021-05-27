package org.lmarek.memory.refresher.document

import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.Term
import org.apache.lucene.document.Document as LuceneDocument

private const val PATH_FIELD = "path"
private const val CONTENT_FIELD = "content"

class LuceneRegisterDocumentService(private val indexWriter: IndexWriter) : RegisterDocumentService {

    override fun register(document: Document) {
        val luceneDocument = document.toLuceneDocument()
        val searchTerm = Term(PATH_FIELD, document.path)
        indexWriter.updateDocument(searchTerm, luceneDocument)
        indexWriter.commit()
    }

    private fun Document.toLuceneDocument(): LuceneDocument {
        val luceneDocument = LuceneDocument()
        luceneDocument.add(TextField(CONTENT_FIELD, content, Field.Store.NO))
        luceneDocument.add(StringField(PATH_FIELD, path, Field.Store.YES))
        return luceneDocument
    }
}