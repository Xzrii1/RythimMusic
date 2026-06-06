/**
 * Rythim Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.rythim.music.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SongSuggestion(
    val title: String,
    val artist: String,
)

object OpenRouterService {
    private val client =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    suspend fun translate(
        text: String,
        targetLanguage: String,
        apiKey: String,
        baseUrl: String,
        model: String,
        mode: String,
        maxRetries: Int = 3,
        sourceLanguage: String? = null,
        customSystemPrompt: String = "",
    ): Result<List<String>> =
        withContext(Dispatchers.IO) {
            var currentAttempt = 0

            // Validate input
            if (text.isBlank()) {
                return@withContext Result.failure(Exception("Input text is empty"))
            }

            val lines = text.lines()
            val lineCount = lines.size

            while (currentAttempt < maxRetries) {
                try {
                    // Use custom system prompt if provided, otherwise use the default
                    val systemPrompt =
                        if (customSystemPrompt.isNotBlank()) {
                            customSystemPrompt.replace("{lineCount}", lineCount.toString())
                        } else {
                            """You are a precise lyrics translation assistant. Your output must ALWAYS be a valid JSON array of strings.

CRITICAL RULES:
1. Output ONLY a JSON array: ["line1", "line2", "line3"]
2. NO explanations, NO questions, NO additional text
3. Each input line maps to exactly one output line
4. Preserve empty lines as empty strings ""
5. Return EXACTLY $lineCount items in the array
6. If uncertain, provide best approximation but maintain line count"""
                        }

                    val userPrompt =
                        when (mode) {
                            "Romanized" -> {
                                """Romanize/transliterate the following $lineCount lines into simple Latin script using ONLY basic English letters (a-z, A-Z).

CRITICAL REQUIREMENTS:
- Use ONLY simple ASCII characters (a-z, A-Z, 0-9, basic punctuation)
- NO special characters like ā, ī, ū, ñ, ç, etc.
- NO diacritics or accent marks
- If text is already in Latin script, return it UNCHANGED
- For non-Latin scripts (Hindi, Chinese, Japanese, Korean, Cyrillic, etc.), provide simple romanization
- DO NOT translate meaning, only convert script to simple English letters
- Keep all punctuation and formatting
- Preserve line-by-line structure exactly

Examples of correct simple romanization:
- Sanskrit/Hindi "आ" → "aa" (not "ā")
- Japanese "東京" → "toukyou" or "tokyo" (not "tōkyō")
- Korean "서울" → "seoul" (not "sŏul")

Input ($lineCount lines):
$text

Output MUST be a JSON array with EXACTLY $lineCount strings using ONLY simple ASCII characters."""
                            }

                            "Transcribed" -> {
                                """Transcribe/transliterate the following $lineCount lines phonetically into $targetLanguage script.

CRITICAL REQUIREMENTS:
- Convert the SOUND/PRONUNCIATION of the original text into $targetLanguage script
- DO NOT translate the meaning - only represent how the original words SOUND
- Use the native script of $targetLanguage (e.g., Devanagari for Hindi, Hangul for Korean, etc.)
- Preserve the original pronunciation as closely as possible in the target script
- Keep punctuation and formatting
- Preserve line-by-line structure exactly
- If text is already in $targetLanguage script, return it UNCHANGED

Examples:
- Japanese "こんにちは" to Hindi → "कोन्निचिवा" (phonetic, not translation)
- English "Hello" to Hindi → "हेलो" (phonetic)
- Korean "안녕하세요" to Hindi → "अन्न्योंग हासेयो" (phonetic)

Input ($lineCount lines):
$text

Output MUST be a JSON array with EXACTLY $lineCount strings in $targetLanguage script."""
                            }

                            else -> {
                                """Translate the following $lineCount lines to $targetLanguage.

IMPORTANT:
- Provide natural, accurate translation
- Maintain poetic flow and meaning
- Keep punctuation appropriate for target language
- Preserve line-by-line structure exactly
- For song lyrics, prioritize singability

Input ($lineCount lines):
$text

Output MUST be a JSON array with EXACTLY $lineCount strings."""
                            }
                        }

                    val messages =
                        JSONArray().apply {
                            put(
                                JSONObject().apply {
                                    put("role", "system")
                                    put("content", systemPrompt)
                                },
                            )
                            put(
                                JSONObject().apply {
                                    put("role", "user")
                                    put("content", userPrompt)
                                },
                            )
                        }

                    val jsonBody =
                        JSONObject().apply {
                            if (model.isNotBlank()) {
                                put("model", model)
                            }
                            put("messages", messages)
                            put("temperature", 0.3) // Lower temperature for more consistent output
                            put("max_tokens", lineCount * 100) // Adequate tokens for translation
                        }

                    val request =
                        Request
                            .Builder()
                            .url(baseUrl.ifBlank { "https://openrouter.ai/api/v1/chat/completions" })
                            .apply {
                                if (apiKey.isNotBlank()) {
                                    addHeader("Authorization", "Bearer ${apiKey.trim()}")
                                }
                            }.addHeader("Content-Type", "application/json")
                            .addHeader("HTTP-Referer", "https://github.com/Yamzzdev/Rythim-Music")
                            .addHeader("X-Title", "Rythim Music")
                            .post(jsonBody.toString().toRequestBody(JSON))
                            .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (!response.isSuccessful) {
                        // Retry on server errors (5xx)
                        if (response.code >= 500) {
                            currentAttempt++
                            kotlinx.coroutines.delay(1000L * currentAttempt)
                            continue
                        }

                        val errorMsg =
                            try {
                                JSONObject(responseBody ?: "").optJSONObject("error")?.optString("message")
                                    ?: "HTTP ${response.code}: ${response.message}"
                            } catch (e: Exception) {
                                "HTTP ${response.code}: ${response.message}"
                            }
                        return@withContext Result.failure(Exception("Translation failed: $errorMsg"))
                    }

                    if (responseBody == null) {
                        currentAttempt++
                        continue
                    }

                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val message = choices.getJSONObject(0).optJSONObject("message")
                        var content = message?.optString("content")?.trim()

                        if (!content.isNullOrBlank()) {
                            // Enhanced JSON extraction with multiple fallback strategies
                            var translatedLines: List<String>? = null

                            // Strategy 1: Try direct JSON parsing
                            try {
                                val jsonArray = JSONArray(content)
                                translatedLines = (0 until jsonArray.length()).map { jsonArray.optString(it) }
                            } catch (e: Exception) {
                                // Strategy 2: Extract JSON from markdown code blocks
                                content = content.replace("```json", "").replace("```", "").trim()

                                try {
                                    val jsonArray = JSONArray(content)
                                    translatedLines = (0 until jsonArray.length()).map { jsonArray.optString(it) }
                                } catch (e2: Exception) {
                                    // Strategy 3: Find first [ and last ]
                                    val startIdx = content.indexOf('[')
                                    val endIdx = content.lastIndexOf(']')

                                    if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                                        val jsonString = content.substring(startIdx, endIdx + 1)
                                        try {
                                            val jsonArray = JSONArray(jsonString)
                                            translatedLines = (0 until jsonArray.length()).map { jsonArray.optString(it) }
                                        } catch (e3: Exception) {
                                            // Strategy 4: Manual line-by-line parsing as last resort
                                            translatedLines =
                                                content
                                                    .lines()
                                                    .filter { it.trim().isNotEmpty() }
                                                    .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                                        }
                                    }
                                }
                            }

                            if (translatedLines != null) {
                                // Validate line count matches
                                if (translatedLines.size == lineCount) {
                                    return@withContext Result.success(translatedLines)
                                } else if (translatedLines.size > lineCount) {
                                    // If we got more lines, take first N
                                    return@withContext Result.success(translatedLines.take(lineCount))
                                } else {
                                    // If we got fewer lines, pad with empty strings
                                    val paddedLines = translatedLines.toMutableList()
                                    while (paddedLines.size < lineCount) {
                                        paddedLines.add("")
                                    }
                                    return@withContext Result.success(paddedLines)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (currentAttempt == maxRetries - 1) {
                        return@withContext Result.failure(e)
                    }
                }
                currentAttempt++
                kotlinx.coroutines.delay(1000L * currentAttempt)
            }
            return@withContext Result.failure(Exception("Max retries exceeded"))
        }

    suspend fun summarize(
        songTitle: String,
        artistName: String,
        apiKey: String,
        baseUrl: String,
        model: String,
        provider: String,
        targetLanguage: String = "English",
    ): Result<String> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext Result.failure(Exception("API key required"))
            try {
                val systemPrompt =
                    "You are a knowledgeable music expert. Write concise, engaging song insights in 3–4 sentences. " +
                        "Always respond in $targetLanguage, regardless of the song's original language."
                val userPrompt =
                    "Tell me about the song \"$songTitle\" by $artistName — what it\'s about, its themes, musical style, " +
                        "and what makes it special or notable. Be conversational and insightful. Write the entire response in $targetLanguage."

                val isClaude = provider == "Claude"

                val request =
                    if (isClaude) {
                        // Anthropic Messages API — distinct format from OpenAI-compatible providers
                        val messages =
                            JSONArray().apply {
                                put(JSONObject().apply {
                                    put("role", "user")
                                    put("content", userPrompt)
                                })
                            }
                        val body =
                            JSONObject().apply {
                                put("model", model.ifBlank { "claude-haiku-4-5-20251001" })
                                put("max_tokens", 400)
                                put("temperature", 0.7)
                                put("system", systemPrompt)
                                put("messages", messages)
                            }
                        Request.Builder()
                            .url(baseUrl.ifBlank { "https://api.anthropic.com/v1/messages" })
                            .addHeader("x-api-key", apiKey.trim())
                            .addHeader("anthropic-version", "2023-06-01")
                            .addHeader("Content-Type", "application/json")
                            .post(body.toString().toRequestBody(JSON))
                            .build()
                    } else {
                        // OpenAI-compatible: OpenRouter, OpenAI, Perplexity, Gemini, XAi, Mistral, Custom
                        val messages =
                            JSONArray().apply {
                                put(JSONObject().apply {
                                    put("role", "system")
                                    put("content", systemPrompt)
                                })
                                put(JSONObject().apply {
                                    put("role", "user")
                                    put("content", userPrompt)
                                })
                            }
                        val body =
                            JSONObject().apply {
                                if (model.isNotBlank()) put("model", model)
                                put("messages", messages)
                                put("temperature", 0.7)
                                put("max_tokens", 400)
                            }
                        Request.Builder()
                            .url(baseUrl.ifBlank { "https://openrouter.ai/api/v1/chat/completions" })
                            .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("HTTP-Referer", "https://github.com/Yamzzdev/Rythim-Music")
                            .addHeader("X-Title", "Rythim Music")
                            .post(body.toString().toRequestBody(JSON))
                            .build()
                    }

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response"))
                if (!response.isSuccessful)
                    return@withContext Result.failure(Exception("API error ${response.code}: $responseBody"))

                val content =
                    if (isClaude) {
                        JSONObject(responseBody)
                            .getJSONArray("content")
                            .getJSONObject(0)
                            .getString("text")
                            .trim()
                    } else {
                        JSONObject(responseBody)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim()
                    }
                Result.success(content)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun recommend(
        userPrompt: String,
        apiKey: String,
        baseUrl: String,
        model: String,
        provider: String,
        targetLanguage: String = "English",
        count: Int = 10,
    ): Result<List<SongSuggestion>> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext Result.failure(Exception("API key required"))
            if (userPrompt.isBlank()) return@withContext Result.failure(Exception("Prompt is empty"))
            try {
                val systemPrompt =
                    "You are a music recommendation expert with deep knowledge of every genre. " +
                        "Given a user's mood, situation, vibe, or description, you recommend real songs that match. " +
                        "Output ONLY a JSON array of objects with this exact shape: " +
                        "[{\"title\":\"Song Name\",\"artist\":\"Artist Name\"}]. " +
                        "Rules: NO markdown, NO explanations, NO trailing commentary. " +
                        "Return EXACTLY $count items. Use real, popular songs that exist on YouTube Music. " +
                        "Provide a diverse mix of artists. Use accurate song titles and primary artist names " +
                        "(no \"feat.\" or remixers, just the lead artist). The display language hint is $targetLanguage, " +
                        "but always use each song's original title and artist in their native script — never translate them."
                val userMessage =
                    "Recommend $count songs based on this prompt: \"$userPrompt\". Return the JSON array only."

                val isClaude = provider == "Claude"

                val request =
                    if (isClaude) {
                        val messages =
                            JSONArray().apply {
                                put(JSONObject().apply {
                                    put("role", "user")
                                    put("content", userMessage)
                                })
                            }
                        val body =
                            JSONObject().apply {
                                put("model", model.ifBlank { "claude-haiku-4-5-20251001" })
                                put("max_tokens", 1200)
                                put("temperature", 0.7)
                                put("system", systemPrompt)
                                put("messages", messages)
                            }
                        Request.Builder()
                            .url(baseUrl.ifBlank { "https://api.anthropic.com/v1/messages" })
                            .addHeader("x-api-key", apiKey.trim())
                            .addHeader("anthropic-version", "2023-06-01")
                            .addHeader("Content-Type", "application/json")
                            .post(body.toString().toRequestBody(JSON))
                            .build()
                    } else {
                        val messages =
                            JSONArray().apply {
                                put(JSONObject().apply {
                                    put("role", "system")
                                    put("content", systemPrompt)
                                })
                                put(JSONObject().apply {
                                    put("role", "user")
                                    put("content", userMessage)
                                })
                            }
                        val body =
                            JSONObject().apply {
                                if (model.isNotBlank()) put("model", model)
                                put("messages", messages)
                                put("temperature", 0.7)
                                put("max_tokens", 1200)
                            }
                        Request.Builder()
                            .url(baseUrl.ifBlank { "https://openrouter.ai/api/v1/chat/completions" })
                            .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("HTTP-Referer", "https://github.com/Yamzzdev/Rythim-Music")
                            .addHeader("X-Title", "Rythim Music")
                            .post(body.toString().toRequestBody(JSON))
                            .build()
                    }

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response"))
                if (!response.isSuccessful)
                    return@withContext Result.failure(Exception("API error ${response.code}: $responseBody"))

                var content =
                    if (isClaude) {
                        JSONObject(responseBody)
                            .getJSONArray("content")
                            .getJSONObject(0)
                            .getString("text")
                            .trim()
                    } else {
                        JSONObject(responseBody)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim()
                    }

                // Multi-strategy JSON extraction (LLMs love adding extra prose)
                val jsonArray: JSONArray = try {
                    JSONArray(content)
                } catch (_: Exception) {
                    content = content.replace("```json", "").replace("```", "").trim()
                    try {
                        JSONArray(content)
                    } catch (_: Exception) {
                        val start = content.indexOf('[')
                        val end = content.lastIndexOf(']')
                        if (start != -1 && end > start) {
                            JSONArray(content.substring(start, end + 1))
                        } else {
                            return@withContext Result.failure(Exception("Could not parse AI response as JSON"))
                        }
                    }
                }

                val suggestions = (0 until jsonArray.length()).mapNotNull { i ->
                    val obj = jsonArray.optJSONObject(i) ?: return@mapNotNull null
                    val title = obj.optString("title").trim()
                    val artist = obj.optString("artist").trim()
                    if (title.isBlank() || artist.isBlank()) null else SongSuggestion(title, artist)
                }

                if (suggestions.isEmpty()) {
                    Result.failure(Exception("AI returned no usable suggestions"))
                } else {
                    Result.success(suggestions)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
