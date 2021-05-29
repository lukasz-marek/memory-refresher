package org.lmarek.memory.refresher.document.refresh

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentLoader
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.find.PathsReadOnlyRepository
import org.lmarek.memory.refresher.document.register.PathsWriteOnlyRepository
import java.io.File

class RefreshDocumentsServiceImpl(
    private val readOnlyRepository: PathsReadOnlyRepository,
    private val writeOnlyRepository: PathsWriteOnlyRepository,
    private val documentLoader: DocumentLoader
) : RefreshDocumentsService {

    override suspend fun refreshAll(): ReceiveChannel<RefreshResult> {
        val allDocuments = readOnlyRepository.listAll()
        val output = Channel<RefreshResult>(Channel.UNLIMITED)
        coroutineScope {
            launch {
                allDocuments.collect { path ->
                    val refreshedDocument = loadDocument(path)
                    if (refreshedDocument != null) {
                        writeOnlyRepository.register(refreshedDocument)
                    } else {
                        writeOnlyRepository.unregister(path)
                    }
                    val refreshType = if (refreshedDocument == null) RefreshType.DELETE else RefreshType.RELOAD
                    output.send(RefreshResult(path, refreshType))
                }
            }
        }
        return output
    }

    private suspend fun loadDocument(path: DocumentPath): Document? {
        return withContext(Dispatchers.IO) {
            try {
                documentLoader.load(File(path.value))
            } catch (exception: DocumentLoader.DocumentLoaderException) {
                null
            }
        }
    }
}