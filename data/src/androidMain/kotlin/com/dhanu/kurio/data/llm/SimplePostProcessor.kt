package com.dhanu.kurio.data.llm

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SimplePostProcessor : PostProcessor {

    @Volatile private var modelLoaded = false
    @Volatile private var enabled = false

    override suspend fun process(text: String, language: String): String = withContext(Dispatchers.Default) {
        if (!enabled || !modelLoaded || text.isBlank()) return@withContext text

        try {
            // Basic post-processing: capitalise first letter, ensure trailing punctuation
            var result = text.trim()
            if (result.isNotEmpty()) {
                result = result.replaceFirstChar { it.uppercase() }
                if (!result.endsWith(".") && !result.endsWith("!") && !result.endsWith("?")) {
                    result += "."
                }
            }

            // Remove repeated filler words
            result = result.replace(Regex("\\b(um|uh|like|you know|actually|basically)\\b", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\s+"), " ")
                .trim()

            Napier.d(tag = "PostProcessor") { "Post-processed text (${text.length} -> ${result.length} chars)" }
            result
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "PostProcessor") { "Post-processing failed, returning original" }
            text
        }
    }

    override suspend fun loadModel(modelPath: String?): Boolean {
        // For simple rule-based post-processing, no actual model is needed
        modelLoaded = true
        enabled = true
        Napier.d(tag = "PostProcessor") { "Simple rule-based post-processor ready" }
        return true
    }

    override suspend fun unloadModel() {
        modelLoaded = false
    }

    override fun isLoaded(): Boolean = modelLoaded

    override fun isEnabled(): Boolean = enabled

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}
