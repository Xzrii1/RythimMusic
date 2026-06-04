/**
 * Rythim Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.rythim.music.models

import com.rythim.innertube.models.YTItem
import com.rythim.music.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
