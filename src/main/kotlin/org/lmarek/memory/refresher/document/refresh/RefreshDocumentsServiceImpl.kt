package org.lmarek.memory.refresher.document.refresh

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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

    private val channelCapacity = 10

    @ExperimentalCoroutinesApi
    override suspend fun refreshAll(): Flow<RefreshResult> {
        return flow {
            coroutineScope {
                val allDocuments = readOnlyRepository.listAll()
                val pathsToDelete = Channel<DocumentPath>(channelCapacity)
                val pathsToReload = Channel<Document>(channelCapacity)
                launch {
                    splitDocumentPaths(allDocuments, pathsToDelete, pathsToReload)
                }
                val deletionResults = Channel<RefreshResult>(channelCapacity)
                val reloadResults = Channel<RefreshResult>(channelCapacity)
                launch {
                    writeOnlyRepository.unregister(pathsToDelete.consumeAsFlow().onEach {
                        deletionResults.send(RefreshResult(it, RefreshType.DELETE))
                    })
                    deletionResults.close()
                }
                launch {
                    writeOnlyRepository.register(pathsToReload.consumeAsFlow().onEach {
                        reloadResults.send(RefreshResult(it.path, RefreshType.RELOAD))
                    })
                    reloadResults.close()
                }
                merge(deletionResults.consumeAsFlow(), reloadResults.consumeAsFlow()).collect {
                    emit(it)
                }
            }
        }
    }

    private suspend fun splitDocumentPaths(
        allDocuments: Flow<DocumentPath>,
        toDelete: Channel<DocumentPath>,
        toReload: Channel<Document>
    ) {
        coroutineScope {
            allDocuments.collect { path ->
                launch {
                    val document = loadDocument(path)
                    if (document == null)
                        toDelete.send(path)
                    else
                        toReload.send(document)
                }
            }
        }
        toDelete.close()
        toReload.close()
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