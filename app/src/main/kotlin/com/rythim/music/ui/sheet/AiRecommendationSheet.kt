/**
 * Rythim Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.rythim.music.ui.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rythim.innertube.YouTube
import com.rythim.innertube.models.SongItem
import com.rythim.music.LocalPlayerConnection
import com.rythim.music.R
import com.rythim.music.api.OpenRouterService
import com.rythim.music.constants.AiProviderKey
import com.rythim.music.constants.LanguageCodeToName
import com.rythim.music.constants.OpenRouterApiKey
import com.rythim.music.constants.OpenRouterBaseUrlKey
import com.rythim.music.constants.OpenRouterDefaultBaseUrl
import com.rythim.music.constants.OpenRouterDefaultModel
import com.rythim.music.constants.OpenRouterModelKey
import com.rythim.music.constants.TranslateLanguageKey
import com.rythim.music.extensions.toMediaItem
import com.rythim.music.playback.queues.ListQueue
import com.rythim.music.utils.rememberPreference
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecommendationSheet(
    onDismiss: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current

    var openRouterApiKey by rememberPreference(OpenRouterApiKey, "")
    val aiProvider by rememberPreference(AiProviderKey, "OpenRouter")
    val openRouterBaseUrl by rememberPreference(OpenRouterBaseUrlKey, OpenRouterDefaultBaseUrl)
    val openRouterModel by rememberPreference(OpenRouterModelKey, OpenRouterDefaultModel)
    val translateLanguage by rememberPreference(TranslateLanguageKey, "en")

    val hasApiKey = aiProvider != "DeepL" && openRouterApiKey.isNotBlank()
    val isDeepL = aiProvider == "DeepL"

    var prompt by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var apiKeyInput by rememberSaveable { mutableStateOf("") }

    val submit: () -> Unit = {
        if (prompt.isNotBlank() && !isLoading) {
            coroutineScope.launch {
                isLoading = true
                error = null
                results = emptyList()
                keyboardController?.hide()

                val aiResult = OpenRouterService.recommend(
                    userPrompt = prompt,
                    apiKey = openRouterApiKey,
                    baseUrl = openRouterBaseUrl,
                    model = openRouterModel,
                    provider = aiProvider,
                    targetLanguage = LanguageCodeToName[translateLanguage] ?: translateLanguage,
                )

                aiResult.onSuccess { suggestions ->
                    val deferred = suggestions.map { sug ->
                        async {
                            YouTube.search("${sug.title} ${sug.artist}", YouTube.SearchFilter.FILTER_SONG)
                                .getOrNull()
                                ?.items
                                ?.filterIsInstance<SongItem>()
                                ?.firstOrNull()
                        }
                    }
                    val songs = deferred.awaitAll().filterNotNull().distinctBy { it.id }

                    if (songs.isEmpty()) {
                        error = "Lagu tidak ditemukan di YouTube Music. Coba prompt yang lebih spesifik."
                    } else {
                        results = songs
                    }
                }.onFailure {
                    error = it.message ?: "Gagal mengambil saran AI"
                }

                isLoading = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.sparkle_ai),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "AI Saran Lagu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ceritain mood, vibe, atau lagi pengen denger apa.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            when {
                isDeepL -> {
                    Text(
                        text = "DeepL tidak mendukung saran lagu. Ganti provider AI di Settings > AI lyrics translation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                !hasApiKey -> {
                    Text(
                        text = "Atur AI API key dulu buat pakai fitur ini.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("Paste API key di sini") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (apiKeyInput.isNotBlank()) {
                                openRouterApiKey = apiKeyInput
                                apiKeyInput = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("Simpan")
                    }
                }

                else -> {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("Misal: lagu sedih buat hujan-hujanan") },
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { submit() }),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = submit,
                                enabled = !isLoading && prompt.isNotBlank(),
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_upward),
                                        contentDescription = "Cari",
                                    )
                                }
                            }
                        },
                    )

                    error?.let { msg ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    if (results.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "${results.size} lagu untuk kamu",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                            )
                            TextButton(
                                onClick = {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = prompt.take(40),
                                            items = results.map { it.toMediaItem() },
                                        ),
                                    )
                                    onDismiss()
                                },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.play),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Putar semua")
                            }
                        }
                        Spacer(Modifier.height(4.dp))

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 420.dp),
                        ) {
                            items(results, key = { it.id }) { song ->
                                SuggestionRow(
                                    song = song,
                                    onClick = {
                                        // Play tapped song first, queue the rest after
                                        val start = results.indexOf(song).coerceAtLeast(0)
                                        val ordered = results.drop(start) + results.take(start)
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = song.title,
                                                items = ordered.map { it.toMediaItem() },
                                            ),
                                        )
                                        onDismiss()
                                    },
                                )
                            }
                        }
                    } else if (!isLoading && error == null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Tekan tombol kirim atau Enter untuk mulai.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    song: SongItem,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            AsyncImage(
                model = song.thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier
            .weight(1f)
            .padding(end = 8.dp)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artists.joinToString { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
