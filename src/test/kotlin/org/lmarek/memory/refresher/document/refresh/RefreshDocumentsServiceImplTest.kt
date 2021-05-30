package org.lmarek.memory.refresher.document.refresh

import io.mockk.mockk
import org.lmarek.memory.refresher.document.DocumentLoader
import org.lmarek.memory.refresher.document.find.PathsReadOnlyRepository
import org.lmarek.memory.refresher.document.register.PathsWriteOnlyRepository

class RefreshDocumentsServiceImplTest {
    private val readOnlyRepository = mockk<PathsReadOnlyRepository>()
    private val writeOnlyRepository = mockk<PathsWriteOnlyRepository>()
    private val documentLoader = mockk<DocumentLoader>()
}