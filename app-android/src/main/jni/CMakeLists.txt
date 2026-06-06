cmake_minimum_required(VERSION 3.22)
project("kurio-whisper" C CXX)

# ── Use whisper.cpp sources relative to build dir ────────────
# The source is injected via a Gradle task or pre-downloaded into the build directory.
# Expected layout (whisper.cpp v1.7.x+):
#   ${WHISPER_DIR}/ggml/src/ggml.c
#   ${WHISPER_DIR}/src/whisper.cpp
set(WHISPER_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../.cxx/whisper.cpp-1.7.4")

# Fallback: check the build variant's .cxx directory
if(NOT EXISTS "${WHISPER_DIR}")
    get_filename_component(CXX_BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}" DIRECTORY)
    set(WHISPER_DIR "${CXX_BUILD_DIR}/whisper.cpp-1.7.4")
endif()

if(NOT EXISTS "${WHISPER_DIR}")
    message(FATAL_ERROR "whisper.cpp source not found at ${WHISPER_DIR}. Build the Gradle task :app-android:downloadWhisperCpp first.")
endif()

message(STATUS "Using whisper.cpp from: ${WHISPER_DIR}")

# ── Add whisper library ─────────────────────────────────────
set(WHISPER_SOURCES
    ${WHISPER_DIR}/ggml/src/ggml.c
    ${WHISPER_DIR}/ggml/src/ggml-quants.c
    ${WHISPER_DIR}/ggml/src/ggml-alloc.c
    ${WHISPER_DIR}/ggml/src/ggml-backend.c
    ${WHISPER_DIR}/src/whisper.cpp
)

add_library(whisper SHARED
    ${WHISPER_SOURCES}
    whisper-jni.cpp
)

target_include_directories(whisper PRIVATE
    ${WHISPER_DIR}
    ${WHISPER_DIR}/ggml/include
    ${WHISPER_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}
)

target_compile_options(whisper PRIVATE
    -O3
    -funwind-tables
    -fexceptions
    -frtti
    -fPIC
)

target_link_options(whisper PRIVATE
    -Wl,--gc-sections
    -Wl,--no-undefined
)

if(CMAKE_ANDROID_ARCH_ABI STREQUAL "arm64-v8a")
    target_compile_options(whisper PRIVATE -march=armv8.4a+dotprod)
endif()

target_compile_definitions(whisper PRIVATE
    GGML_ANDROID=1
    ANDROID=1
    GGML_USE_ACCELERATE=0
    GGML_USE_METAL=0
)

find_library(log-lib log)
target_link_libraries(whisper PRIVATE log dl z)

message(STATUS "Whisper.cpp build configured for ABI: ${CMAKE_ANDROID_ARCH_ABI}")
