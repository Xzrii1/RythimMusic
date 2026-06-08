/**
 * Rythim Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.rythim.music.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_summary")
data class AiSummaryEntity(
    @PrimaryKey val songId: String,
    val summary: String,
    val provider: String,
    val language: String,
    val cachedAt: Long = System.currentTimeMillis(),
)
