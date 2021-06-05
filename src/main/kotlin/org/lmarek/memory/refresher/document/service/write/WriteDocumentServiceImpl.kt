package org.lmarek.memory.refresher.document.service.write

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.service.loader.DocumentLoader
import java.io.File

class WriteDocumentServiceImpl(
    private val pathsWriteOnlyRepository: PathsWriteOnlyRepository,
    private val documentLoader: DocumentLoader
) : WriteDocumentService {
    override suspend fun write(path: DocumentPath) {
        val document = loadFile(path, OnMissing.THROW)
        pathsWriteOnlyRepository.register(document!!) // non-null when throws
    }

    override suspend fun write(paths: Flow<DocumentPath>, onMissing: OnMissing) {
        val documents = paths.map { it to loadFile(it, onMissing) }
        writeChanges(documents)
    }

    private suspend fun writeChanges(documents: Flow<Pair<DocumentPath, Document?>>) {
        coroutineScope {
            val toRemove = Channel<DocumentPath>()
            val toSave = Channel<Document>()
            launch { pathsWriteOnlyRepository.unregister(toRemove.consumeAsFlow()) }
            launch { pathsWriteOnlyRepository.register(toSave.consumeAsFlow()) }
            documents.collect { (path, document) ->
                if (document != null)
                    toSave.send(document)
                else
                    toRemove.send(path)
            }
            toRemove.close()
            toSave.close()
        }
    }

    private suspend fun loadFile(path: DocumentPath, onMissing: OnMissing): Document? =
        try {
            documentLoader.load(File(path.value))
        } catch (loaderException: DocumentLoader.DocumentLoaderException) {
            when (onMissing) {
                OnMissing.REMOVE -> null
                OnMissing.THROW -> throw WriteDocumentService.WriteDocumentServiceException(loaderException)
            }
        }
}