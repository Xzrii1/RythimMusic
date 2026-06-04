package com.rythim.innertube.models.body

import com.rythim.innertube.models.Context
import com.rythim.innertube.models.Continuation
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String?,
    val params: String?,
    val continuation: String?
)
