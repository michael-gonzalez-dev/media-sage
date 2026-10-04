package com.mediasage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One work in the Library, keyed by its server id. [figureId] is the figure's server id. */
@Entity(tableName = "works")
data class WorkEntity(
    @PrimaryKey val id: Long,
    val figureId: Long,
    val title: String,
    val year: Int? = null,
    val recordedBy: String? = null,
    val coverUrl: String? = null
)
