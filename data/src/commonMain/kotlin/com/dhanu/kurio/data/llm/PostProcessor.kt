package com.dhanu.kurio.data.llm

interface PostProcessor {
    suspend fun process(text: String, language: String = "en"): String
    suspend fun loadModel(modelPath: String? = null): Boolean
    suspend fun unloadModel()
    fun isLoaded(): Boolean
    fun isEnabled(): Boolean
}
