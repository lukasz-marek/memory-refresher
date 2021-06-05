package org.lmarek.memory.refresher.document.service.refresh

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.repository.read.PathsReadOnlyRepository
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.service.loader.DocumentLoader
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
                launch { splitDocumentPaths(allDocuments, pathsToDelete, pathsToReload) }

                val results = merge(delete(pathsToDelete), reload(pathsToReload))
                results.collect { emit(it) }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun delete(pathsToDelete: Channel<DocumentPath>): Flow<RefreshResult> = channelFlow {
        coroutineScope {
            val results = Channel<RefreshResult>(channelCapacity)
            launch {
                writeOnlyRepository.delete(pathsToDelete.consumeAsFlow().onEach {
                    send(RefreshResult(it, RefreshType.DELETE))
                })
                results.close()
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun reload(pathsToReload: Channel<Document>): Flow<RefreshResult> = channelFlow {
        coroutineScope {
            val results = Channel<RefreshResult>(channelCapacity)
            launch {
                writeOnlyRepository.save(pathsToReload.consumeAsFlow().onEach {
                    send(RefreshResult(it.path, RefreshType.RELOAD))
                })
                results.close()
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

    private suspend fun loadDocument(path: DocumentPath): Document? = withContext(Dispatchers.IO) {
        try {
            documentLoader.load(File(path.value))
        } catch (exception: DocumentLoader.DocumentLoaderException) {
            null
        }
    }

}