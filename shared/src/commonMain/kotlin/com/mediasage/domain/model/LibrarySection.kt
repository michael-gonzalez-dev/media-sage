package com.mediasage.domain.model

/** One reporter's shelf in the Library: the reporter and their works, oldest record first. */
data class LibrarySection(
    val figure: Figure,
    val works: List<Work>
)
