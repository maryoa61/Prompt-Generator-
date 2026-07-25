# Prompt Generator for Android

A modern, native Android application designed to engineer, structure, and optimize high-quality AI/LLM prompts. Engineered using **Jetpack Compose**, **Material 3**, **Clean Architecture**, **Hilt**, **Room Database**, and **Jetpack DataStore**.

Prompt generation is powered by the **NVIDIA NIM API** (build.nvidia.com), an OpenAI-compatible endpoint offering free access to dozens of hosted LLMs. If no API key is configured, or a call fails, the app automatically falls back to a fully offline/local template generator — the app always works, online or off.

---

## AI Setup (NVIDIA API)

1. Create a free API key at [build.nvidia.com/settings/api-keys](https://build.nvidia.com/settings/api-keys) (starts with `nvapi-`).
2. Copy `.env.example` to a new file named `.env` in the project root.
3. Set `NVIDIA_API_KEY=nvapi-...` in `.env` (this file is git-ignored, so your key stays local).
4. Optionally set `NVIDIA_MODEL=` to any model id from the [NVIDIA API Catalog](https://build.nvidia.com/models) (defaults to `nvidia/llama-3.3-nemotron-super-49b-v1.5`).
5. Rebuild the app. The Secrets Gradle Plugin injects these values into `BuildConfig` at compile time.

Without a key, the **Generate Prompt** button still works — it just uses the local template engine and shows an "Offline Fallback" badge in the output preview.

---

## Features

- **Domain-Specific Templates**: Tailor prompt generation for multiple specialized roles:
  - **Software Development**: Senior Software Architect & Principal Engineer persona with clean architecture and SOLID constraints.
  - **Creative Writing**: Narrative designer & world-building focus with evocative sensory guidelines.
  - **Business & Marketing**: CMO positioning, conversion hooks, value drivers, and CTAs.
  - **Data Analysis**: Lead Data Scientist analytics, metrics, and actionable recommendations.
  - **General Knowledge**: Structured subject matter expert explanations.
- **Full Offline Persistence**: Save and manage prompt history locally using **Room Database**.
- **Search & Favorites**: Filter, search, and pin favorite generated prompts.
- **User Preferences**: Persist settings like default prompt style, auto-copy on generation, and theme preference using **Jetpack DataStore**.
- **System Integrations**: Direct integration with Android System Clipboard (`ClipboardManager`) and native Share Sheet (`Intent.ACTION_SEND`).
- **Adaptive Launcher Icon**: Custom Material 3 vector adaptive icon

---

## Tech Stack & Architecture

- **Language**: 100% [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Architecture**: MVVM + Clean Architecture (Presentation, Domain Use Cases, Data Layers)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Local Database**: [Room](https://developer.android.com/training/data-storage/room) with KSP
- **Key-Value Storage**: [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences)
- **Asynchronous Operations**: Kotlin Coroutines & `StateFlow`
- **Navigation**: Jetpack Navigation Compose
- **AI Prompt Generation**: [NVIDIA NIM API](https://build.nvidia.com) via Retrofit + Moshi + OkHttp, with automatic offline fallback

---

## Project Structure

```
app/src/main/java/com/example/
├── data/
│   ├── local/
│   │   ├── datastore/       # UserPreferencesDataStore
│   │   └── db/              # Room AppDatabase, PromptDao, PromptEntity
│   ├── remote/               # NVIDIA API: NvidiaApiService, models, AiPromptDataSource
│   └── repository/          # PromptRepository & PromptRepositoryImpl
├── di/                      # Hilt AppModule, NetworkModule & RepositoryModule
├── domain/
│   ├── model/               # PromptStyle, UserPromptInput, PromptTemplate
│   └── usecase/             # GeneratePromptUseCase, PromptFormatterUseCase
├── ui/
│   ├── generator/           # GeneratorScreen & GeneratorViewModel
│   ├── history/             # HistoryScreen & HistoryViewModel
│   ├── settings/            # SettingsScreen & SettingsViewModel
│   ├── navigation/          # Navigation Bar & Compose Routing
│   └── theme/               # Material 3 ColorScheme, Type & Theme
└── MainActivity.kt          # App entry point with Hilt injection
```

---

## Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2) or newer recommended.
- **JDK**: Java 17 or Java 21.
- **Min SDK**: API Level 24 (Android 7.0 Nougat).
- **Target SDK**: API Level 35 (Android 15).

### Build & Run
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/prompt-generator-android.git
   ```
2. **Open in Android Studio**:
   Open the root project folder in Android Studio.
3. **Gradle Sync**:
   Allow Android Studio to sync dependencies and build the Gradle model automatically.
4. **Run Application**:
   Select an attached physical device or an Android Virtual Device (AVD) and press **Run** (`Shift + F10`).

----

## Testing

Run unit tests locally on the JVM:
```bash
./gradlew testDebugUnitTest
```

---

## License

```text
Copyright 2026

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
