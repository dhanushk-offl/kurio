#include <jni.h>
#include <whisper.h>
#include <android/log.h>
#include <cstring>
#include <vector>

#define LOG_TAG "WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static struct whisper_context *g_context = nullptr;

extern "C" {

/**
 * Initialises Whisper context from a model file path.
 *
 * Java: private native boolean nativeInit(String modelPath);
 */
JNIEXPORT jboolean JNICALL
Java_com_dhanu_kurio_data_engine_WhisperCppEngine_nativeInit(
    JNIEnv *env, jobject /*thiz*/, jstring modelPath) {

    // Release previous context if any
    if (g_context) {
        whisper_free(g_context);
        g_context = nullptr;
    }

    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    if (!path) {
        LOGE("Failed to get model path string");
        return JNI_FALSE;
    }

    LOGI("Loading Whisper model from: %s", path);

    struct whisper_context_params params = whisper_context_default_params();
    g_context = whisper_init_from_file_with_params(path, params);

    env->ReleaseStringUTFChars(modelPath, path);

    if (!g_context) {
        LOGE("Failed to initialise Whisper context");
        return JNI_FALSE;
    }

    LOGI("Whisper model loaded successfully");
    return JNI_TRUE;
}

/**
 * Transcribes 16kHz mono PCM-16 audio data.
 *
 * Java: private native String nativeTranscribe(byte[] audioData);
 */
JNIEXPORT jstring JNICALL
Java_com_dhanu_kurio_data_engine_WhisperCppEngine_nativeTranscribe(
    JNIEnv *env, jobject /*thiz*/, jbyteArray audioData) {

    if (!g_context) {
        LOGE("Whisper context not initialised");
        return env->NewStringUTF("");
    }

    jsize audioLen = env->GetArrayLength(audioData);
    if (audioLen == 0) {
        return env->NewStringUTF("");
    }

    jbyte *audioBytes = env->GetByteArrayElements(audioData, nullptr);
    if (!audioBytes) {
        LOGE("Failed to get audio data");
        return env->NewStringUTF("");
    }

    // Convert PCM-16 bytes to float samples
    int sampleCount = audioLen / 2;
    std::vector<float> pcmSamples(sampleCount);
    for (int i = 0; i < sampleCount; i++) {
        int16_t sample = (int16_t)((audioBytes[i * 2 + 1] << 8) | (audioBytes[i * 2] & 0xFF));
        pcmSamples[i] = sample / 32768.0f;
    }

    env->ReleaseByteArrayElements(audioData, audioBytes, JNI_ABORT);

    // Run Whisper inference
    struct whisper_full_params wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    wparams.print_realtime   = false;
    wparams.print_progress   = false;
    wparams.print_timestamps = false;
    wparams.print_special    = false;
    wparams.translate        = false;
    wparams.n_threads        = 4;
    wparams.offset_ms        = 0;
    wparams.no_context       = true;
    wparams.single_segment   = true;
    wparams.language         = "en";

    int ret = whisper_full(g_context, wparams, pcmSamples.data(), sampleCount);
    if (ret != 0) {
        LOGE("whisper_full failed with code %d", ret);
        return env->NewStringUTF("");
    }

    // Collect text from all segments
    int nSegments = whisper_full_n_segments(g_context);
    std::string result;

    for (int i = 0; i < nSegments; i++) {
        const char *text = whisper_full_get_segment_text(g_context, i);
        if (text) {
            if (!result.empty()) result += " ";
            result += text;
        }
    }

    LOGI("Transcription complete: %zu chars", result.size());
    return env->NewStringUTF(result.c_str());
}

/**
 * Releases the Whisper context.
 *
 * Java: private native void nativeRelease();
 */
JNIEXPORT void JNICALL
Java_com_dhanu_kurio_data_engine_WhisperCppEngine_nativeRelease(
    JNIEnv *env, jobject /*thiz*/) {

    if (g_context) {
        whisper_free(g_context);
        g_context = nullptr;
        LOGI("Whisper context released");
    }
}

} // extern "C"
