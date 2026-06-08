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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import com.rythim.music.LocalDatabase
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
import com.rythim.music.models.toMediaMetadata
import com.rythim.music.playback.queues.ListQueue
import com.rythim.music.ui.component.EnumDialog
import com.rythim.music.ui.menu.AddToPlaylistDialog
import com.rythim.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

// Shared with AiSettings — duplicated here so the sheet is self-healing:
// any inconsistency between aiProvider / openRouterBaseUrl / openRouterModel
// in DataStore gets corrected the moment the user picks a provider in the sheet.
private val PROVIDER_BASE_URLS = mapOf(
    "OpenRouter" to "https://openrouter.ai/api/v1/chat/completions",
    "OpenAI" to "https://api.openai.com/v1/chat/completions",
    "Perplexity" to "https://api.perplexity.ai/chat/completions",
    "Claude" to "https://api.anthropic.com/v1/messages",
    "Gemini" to "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
    "XAi" to "https://api.x.ai/v1/chat/completions",
    "Mistral" to "https://api.mistral.ai/v1/chat/completions",
    "DeepL" to "https://api.deepl.com/v2/translate",
    "Custom" to "",
)

private val PROVIDER_DEFAULT_MODELS = mapOf(
    "OpenRouter" to "google/gemini-2.5-flash-lite",
    "OpenAI" to "gpt-4o-mini",
    "Perplexity" to "sonar",
    "Claude" to "claude-haiku-4-5-20251001",
    "Gemini" to "gemini-flash-lite-latest",
    "XAi" to "grok-4-1-fast",
    "Mistral" to "mistral-small-latest",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecommendationSheet(
    onDismiss: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current

    var openRouterApiKey by rememberPreference(OpenRouterApiKey, "")
    var aiProvider by rememberPreference(AiProviderKey, "OpenRouter")
    var openRouterBaseUrl by rememberPreference(OpenRouterBaseUrlKey, OpenRouterDefaultBaseUrl)
    var openRouterModel by rememberPreference(OpenRouterModelKey, OpenRouterDefaultModel)
    val translateLanguage by rememberPreference(TranslateLanguageKey, "en")

    val isDeepL = aiProvider == "DeepL"
    val hasApiKey = openRouterApiKey.isNotBlank()

    var prompt by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var apiKeyInput by rememberSaveable { mutableStateOf("") }
    var showApiKeyEditor by rememberSaveable { mutableStateOf(false) }
    var showProviderPicker by rememberSaveable { mutableStateOf(false) }
    var songForPlaylist by remember { mutableStateOf<SongItem?>(null) }
    var showAddAllToPlaylist by remember { mutableStateOf(false) }

    fun applyProvider(newProvider: String) {
        aiProvider = newProvider
        when (newProvider) {
            "Custom" -> openRouterBaseUrl = ""
            "DeepL" -> openRouterBaseUrl = PROVIDER_BASE_URLS[newProvider] ?: ""
            else -> openRouterBaseUrl = PROVIDER_BASE_URLS[newProvider] ?: ""
        }
        PROVIDER_DEFAULT_MODELS[newProvider]?.let { openRouterModel = it }
        // Clear stale results — they were generated for a different provider
        results = emptyList()
        error = null
    }

    if (showProviderPicker) {
        EnumDialog(
            onDismiss = { showProviderPicker = false },
            onSelect = {
                applyProvider(it)
                showProviderPicker = false
            },
            title = "Pilih AI Provider",
            current = aiProvider,
            values = PROVIDER_BASE_URLS.keys.toList(),
            valueText = { it },
        )
    }

    val submit: () -> Unit = submit@{
        if (prompt.isBlank() || isLoading) return@submit
        if (isDeepL) {
            error = "DeepL tidak mendukung saran lagu. Pilih provider lain di atas."
            return@submit
        }
        if (!hasApiKey) {
            error = "API key kosong. Tap chip 'API key' di atas."
            return@submit
        }
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

    // Add single song to playlist dialog
    songForPlaylist?.let { song ->
        AddToPlaylistDialog(
            isVisible = true,
            onGetSong = { playlist ->
                database.withTransaction {
                    insert(song.toMediaMetadata())
                }
                coroutineScope.launch(Dispatchers.IO) {
                    playlist.playlist.browseId?.let { YouTube.addToPlaylist(it, song.id) }
                }
                listOf(song.id)
            },
            onGetSongIds = { listOf(song.id) },
            onDismiss = { songForPlaylist = null },
        )
    }

    // Add all results to playlist dialog
    if (showAddAllToPlaylist && results.isNotEmpty()) {
        AddToPlaylistDialog(
            isVisible = true,
            onGetSong = { playlist ->
                val ids = results.map { it.id }
                database.withTransaction {
                    results.forEach { insert(it.toMediaMetadata()) }
                }
                coroutineScope.launch(Dispatchers.IO) {
                    playlist.playlist.browseId?.let { browseId ->
                        ids.forEach { YouTube.addToPlaylist(browseId, it) }
                    }
                }
                ids
            },
            onGetSongIds = { results.map { it.id } },
            onDismiss = { showAddAllToPlaylist = false },
        )
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
            Spacer(Modifier.height(12.dp))

            // Provider + API key chips — fix root cause of any mismatched state
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AssistChip(
                    onClick = { showProviderPicker = true },
                    label = { Text(aiProvider) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.explore_outlined),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.expand_more),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
                AssistChip(
                    onClick = {
                        apiKeyInput = openRouterApiKey
                        showApiKeyEditor = !showApiKeyEditor
                    },
                    label = { Text(if (hasApiKey) "API key tersimpan" else "Set API key") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.key),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    colors = if (!hasApiKey) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    } else {
                        AssistChipDefaults.assistChipColors()
                    },
                )
            }

            if (showApiKeyEditor) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text("Paste API key di sini") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = { showApiKeyEditor = false }) {
                        Text("Batal")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            openRouterApiKey = apiKeyInput.trim()
                            apiKeyInput = ""
                            showApiKeyEditor = false
                        },
                        enabled = apiKeyInput.isNotBlank(),
                    ) {
                        Text("Simpan")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isDeepL) {
                Text(
                    text = "DeepL tidak mendukung saran lagu. Ganti provider di atas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
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
                            enabled = !isLoading && prompt.isNotBlank() && hasApiKey,
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
                        Row {
                            TextButton(
                                onClick = { showAddAllToPlaylist = true },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.add),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Playlist")
                            }
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
                    }
                    Spacer(Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 420.dp),
                    ) {
                        items(results, key = { it.id }) { song ->
                            SuggestionRow(
                                song = song,
                                onClick = {
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
                                onAddToPlaylist = { songForPlaylist = song },
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

@Composable
private fun SuggestionRow(
    song: SongItem,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
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
        Column(modifier = Modifier.weight(1f)) {
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
        IconButton(onClick = onAddToPlaylist) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = "Tambah ke playlist",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
