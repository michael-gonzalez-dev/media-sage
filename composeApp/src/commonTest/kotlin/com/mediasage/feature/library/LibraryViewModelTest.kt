@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mediasage.feature.library

import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureCategory
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.Work
import com.mediasage.domain.repository.FigureRepository
import com.mediasage.domain.repository.WorkRepository
import com.mediasage.domain.usecase.GetLibraryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val tozer = Figure(id = 19, name = "A.W. Tozer", category = FigureCategory.THEOLOGIAN, century = "20th")
    private val tubman = Figure(id = 53, name = "Harriet Tubman", category = FigureCategory.SOCIAL_JUSTICE, century = "19th")
    private val pursuit = Work(id = 19003, figureId = 19, title = "The Pursuit of God", year = 1948)
    private val scenes = Work(
        id = 53901,
        figureId = 53,
        title = "Scenes in the Life of Harriet Tubman",
        year = 1869,
        recordedBy = "Sarah Bradford",
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun worksAreGroupedUnderEachReporterInNameOrder() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tozer, tubman), works = listOf(scenes, pursuit))

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("A.W. Tozer", "Harriet Tubman"), state.sections.map { it.reporterName })
        assertEquals(listOf("The Pursuit of God"), state.sections[0].works.map { it.title })
    }

    @Test
    fun aRecordedWorkKeepsItsWriterAndYear() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tubman), works = listOf(scenes))

        val work = assertIs<LibraryContract.UiState.Success>(vm.state.value).sections.single().works.single()
        assertEquals("Sarah Bradford", work.recordedBy)
        assertEquals(1869, work.year)
        assertEquals("Harriet Tubman", work.reporterName)
        assertNull(work.coverUrl)
    }

    @Test
    fun eachShelfCarriesItsReporterPortraitSoTheyCanBeRecognizedByFace() = runTest(testDispatcher) {
        val withPortrait = tozer.copy(portraitUrl = "https://example.com/tozer.jpg")
        val vm = libraryViewModel(figures = listOf(withPortrait, tubman), works = listOf(pursuit, scenes))

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("https://example.com/tozer.jpg", null), state.sections.map { it.portraitUrl })
    }

    @Test
    fun aReporterWithNoWorksGetsNoSection() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tozer, tubman), works = listOf(pursuit))

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("A.W. Tozer"), state.sections.map { it.reporterName })
    }

    @Test
    fun aWorkWhoseReporterIsNotStoredIsLeftOut() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tozer), works = listOf(pursuit, scenes))

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertEquals(listOf(19003L), state.sections.flatMap { section -> section.works.map { it.id } })
    }

    @Test
    fun beforeTheFirstSyncTheLibraryHasNoSections() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tozer), works = emptyList())

        assertTrue(assertIs<LibraryContract.UiState.Success>(vm.state.value).sections.isEmpty())
    }

    @Test
    fun searchingAReporterNameKeepsTheirWholeShelf() = runTest(testDispatcher) {
        val knowledge = Work(id = 19008, figureId = 19, title = "The Knowledge of the Holy", year = 1961)
        val vm = libraryViewModel(figures = listOf(tozer, tubman), works = listOf(pursuit, knowledge, scenes))

        vm.onIntent(LibraryContract.Intent.SearchQueryChanged("tozer"))

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("A.W. Tozer"), state.sections.map { it.reporterName })
        assertEquals(2, state.sections.single().works.size)
        assertTrue(state.isFiltered)
    }

    @Test
    fun searchingATitleKeepsOnlyTheMatchingWorks() = runTest(testDispatcher) {
        val knowledge = Work(id = 19008, figureId = 19, title = "The Knowledge of the Holy", year = 1961)
        val vm = libraryViewModel(figures = listOf(tozer, tubman), works = listOf(pursuit, knowledge, scenes))

        vm.onIntent(LibraryContract.Intent.SearchQueryChanged("pursuit"))

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertEquals(listOf(19003L), state.sections.flatMap { section -> section.works.map { it.id } })
    }

    @Test
    fun searchingARecorderFindsTheBooksTheyWrote() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tozer, tubman), works = listOf(pursuit, scenes))

        vm.onIntent(LibraryContract.Intent.SearchQueryChanged("Bradford"))

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertEquals(listOf(53901L), state.sections.flatMap { section -> section.works.map { it.id } })
    }

    @Test
    fun choosingAnEraKeepsOnlyThatEraReporters() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tozer, tubman), works = listOf(pursuit, scenes))

        vm.onIntent(LibraryContract.Intent.EraSelected(FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS))

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("Harriet Tubman"), state.sections.map { it.reporterName })
        assertEquals(FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS, state.selectedEra)
    }

    @Test
    fun aSearchWithNoMatchesLeavesNoSectionsAndReadsAsFiltered() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tozer), works = listOf(pursuit))

        vm.onIntent(LibraryContract.Intent.SearchQueryChanged("Kempis"))

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertTrue(state.sections.isEmpty())
        assertTrue(state.isFiltered)
    }

    @Test
    fun tappingABookOpensItsDetailsAndDismissingClosesThem() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tubman.copy(portraitUrl = "https://example.com/tubman.jpg")), works = listOf(scenes))
        val book = assertIs<LibraryContract.UiState.Success>(vm.state.value).sections.single().works.single()

        vm.onIntent(LibraryContract.Intent.WorkSelected(book))
        val opened = assertIs<LibraryContract.UiState.Success>(vm.state.value).selectedWork
        assertEquals("Scenes in the Life of Harriet Tubman", opened?.title)
        assertEquals("https://example.com/tubman.jpg", opened?.reporterPortraitUrl)

        vm.onIntent(LibraryContract.Intent.WorkDismissed)
        assertNull(assertIs<LibraryContract.UiState.Success>(vm.state.value).selectedWork)
    }

    @Test
    fun opensInShelfViewAndTogglesToListAndBack() = runTest(testDispatcher) {
        val vm = libraryViewModel(figures = listOf(tozer), works = listOf(pursuit))
        assertEquals(LibraryView.SHELF, assertIs<LibraryContract.UiState.Success>(vm.state.value).view)

        vm.onIntent(LibraryContract.Intent.ViewSelected(LibraryView.LIST))
        assertEquals(LibraryView.LIST, assertIs<LibraryContract.UiState.Success>(vm.state.value).view)

        vm.onIntent(LibraryContract.Intent.ViewSelected(LibraryView.SHELF))
        assertEquals(LibraryView.SHELF, assertIs<LibraryContract.UiState.Success>(vm.state.value).view)
    }

    @Test
    fun theViewChoiceHoldsWhenTheLibraryIsOpenedAgain() = runTest(testDispatcher) {
        val selection = LibraryViewSelection()
        libraryViewModel(figures = listOf(tozer), works = listOf(pursuit), selection = selection)
            .onIntent(LibraryContract.Intent.ViewSelected(LibraryView.LIST))

        val reopened = libraryViewModel(figures = listOf(tozer), works = listOf(pursuit), selection = selection)

        assertEquals(LibraryView.LIST, assertIs<LibraryContract.UiState.Success>(reopened.state.value).view)
    }

    @Test
    fun openingTheLibrarySyncsWorks() = runTest(testDispatcher) {
        val workRepository = FakeWorkRepositoryForLibrary(emptyList())

        libraryViewModel(figures = listOf(tozer), workRepository = workRepository)

        assertEquals(1, workRepository.syncCalls)
    }

    @Test
    fun aFailedSyncStillShowsTheStoredWorks() = runTest(testDispatcher) {
        val workRepository = FakeWorkRepositoryForLibrary(listOf(pursuit), syncError = RuntimeException("offline"))

        val vm = libraryViewModel(figures = listOf(tozer), workRepository = workRepository)

        val state = assertIs<LibraryContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("The Pursuit of God"), state.sections.single().works.map { it.title })
    }

    /** Builds the ViewModel and starts collecting: `stateIn(WhileSubscribed)` stays Loading until something subscribes. */
    private fun TestScope.libraryViewModel(
        figures: List<Figure>,
        works: List<Work> = emptyList(),
        selection: LibraryViewSelection = LibraryViewSelection(),
        workRepository: FakeWorkRepositoryForLibrary = FakeWorkRepositoryForLibrary(works),
    ): LibraryViewModel {
        val useCase = GetLibraryUseCase(FakeFigureRepositoryForLibrary(figures), workRepository)
        val vm = LibraryViewModel(useCase, workRepository, selection)
        backgroundScope.launch(testDispatcher) { vm.state.collect {} }
        return vm
    }
}

/** [figures] are given in name order, as the figures table returns them. */
private class FakeFigureRepositoryForLibrary(figures: List<Figure>) : FigureRepository {
    private val stored = MutableStateFlow(figures)

    override fun observeAllFigures(): Flow<List<Figure>> = stored
    override fun observeFiguresByCategory(category: FigureCategory): Flow<List<Figure>> = error("not used in this test")
    override suspend fun getFigureById(id: Long): Figure? = error("not used in this test")
    override suspend fun getFigureByName(name: String): Figure? = error("not used in this test")
    override suspend fun syncFigures() = Unit
}

private class FakeWorkRepositoryForLibrary(
    works: List<Work>,
    private val syncError: Exception? = null,
) : WorkRepository {
    private val stored = MutableStateFlow(works)
    var syncCalls = 0

    override fun observeAllWorks(): Flow<List<Work>> = stored

    override suspend fun syncWorks() {
        syncCalls++
        syncError?.let { throw it }
    }
}
