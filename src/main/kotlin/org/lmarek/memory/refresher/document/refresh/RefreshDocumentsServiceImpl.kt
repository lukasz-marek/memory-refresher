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
            val allDocuments = readOnlyRepository.listAll()

            coroutineScope {
                val deletions = Channel<DocumentPath>(channelCapacity)
                val reloads = Channel<Document>(channelCapacity)
                launch {
                    coroutineScope {
                        allDocuments.collect { path ->
                            launch {
                                val document = loadDocument(path)
                                if (document == null)
                                    deletions.send(path)
                                else
                                    reloads.send(document)
                            }
                        }
                    }
                    deletions.close()
                    reloads.close()
                }
                val deletionResults = Channel<RefreshResult>(channelCapacity)
                val reloadResults = Channel<RefreshResult>(channelCapacity)
                launch {
                    writeOnlyRepository.unregister(deletions.consumeAsFlow().onEach {
                        deletionResults.send(RefreshResult(it, RefreshType.DELETE))
                    })
                    deletionResults.close()
                }
                launch {
                    writeOnlyRepository.register(reloads.consumeAsFlow().onEach {
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

    private suspend fun refresh(path: DocumentPath): RefreshResult {
        val refreshedDocument = loadDocument(path)
        val refreshType = if (refreshedDocument != null) {
            writeOnlyRepository.register(refreshedDocument)
            RefreshType.RELOAD
        } else {
            writeOnlyRepository.unregister(path)
            RefreshType.DELETE
        }
        return RefreshResult(path, refreshType)
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