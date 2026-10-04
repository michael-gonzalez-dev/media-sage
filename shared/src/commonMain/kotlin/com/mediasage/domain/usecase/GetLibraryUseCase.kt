package com.mediasage.domain.usecase

import com.mediasage.domain.model.LibrarySection
import com.mediasage.domain.repository.FigureRepository
import com.mediasage.domain.repository.WorkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Groups every work under its reporter, in the reporters' name order, for the Library. A reporter with no works
 * gets no section, and a work whose reporter isn't stored (disabled since the last sync) is left out.
 */
class GetLibraryUseCase(
    private val figureRepository: FigureRepository,
    private val workRepository: WorkRepository,
) {
    operator fun invoke(): Flow<List<LibrarySection>> =
        combine(
            figureRepository.observeAllFigures(),
            workRepository.observeAllWorks(),
        ) { figures, works ->
            val worksByFigure = works.groupBy { it.figureId }
            figures.mapNotNull { figure ->
                worksByFigure[figure.id]?.let { LibrarySection(figure = figure, works = it) }
            }
        }
}
