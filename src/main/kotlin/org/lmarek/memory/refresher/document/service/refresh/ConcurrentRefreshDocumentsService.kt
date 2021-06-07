package org.lmarek.memory.refresher.document.service.refresh

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.repository.read.PathsReadOnlyRepository
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.service.loader.DocumentLoader
import java.io.File

class ConcurrentRefreshDocumentsService(
    private val readOnlyRepository: PathsReadOnlyRepository,
    private val writeOnlyRepository: PathsWriteOnlyRepository,
    private val documentLoader: DocumentLoader
) : RefreshDocumentsService {

    private val channelCapacity = 10

    @ExperimentalCoroutinesApi
    override suspend fun refreshAll(): Flow<RefreshResult> = channelFlow {
        coroutineScope {
            val allDocuments = readOnlyRepository.listAll()

            val pathsToDelete = Channel<DocumentPath>(channelCapacity)
            val pathsToReload = Channel<Document>(channelCapacity)

            launch { splitDocumentPaths(allDocuments, pathsToDelete, pathsToReload) }
            launch { delete(pathsToDelete).collect { send(it) } }
            launch { reload(pathsToReload).collect { send(it) } }
        }
    }

    @ExperimentalCoroutinesApi
    private fun delete(pathsToDelete: Channel<DocumentPath>): Flow<RefreshResult> = channelFlow {
        coroutineScope {
            launch {
                writeOnlyRepository.delete(pathsToDelete.consumeAsFlow().onEach {
                    send(RefreshResult(it, RefreshType.DELETE))
                })
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun reload(pathsToReload: Channel<Document>): Flow<RefreshResult> = channelFlow {
        coroutineScope {
            launch {
                writeOnlyRepository.save(pathsToReload.consumeAsFlow().onEach {
                    send(RefreshResult(it.path, RefreshType.RELOAD))
                })
            }
        }
    }

    private suspend fun splitDocumentPaths(
        allDocuments: Flow<DocumentPath>,
        toDelete: SendChannel<DocumentPath>,
        toReload: SendChannel<Document>
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

    private suspend fun loadDocument(path: DocumentPath): Document? = withContext(Dispatchers.IO) {
        try {
            documentLoader.load(File(path.value))
        } catch (exception: DocumentLoader.DocumentLoaderException) {
            null
        }
    }

}