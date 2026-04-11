# OpenRouter Chat

A native Android chat app for conversing with AI models via the [OpenRouter](https://openrouter.ai) API, with a UI inspired by Telegram.

## Features

- Browse all available OpenRouter models like a contacts list
- Multiple independent conversations per model
- Streaming responses in real time (SSE)
- Markdown rendering (bold, italic, code blocks)
- Fully local — all conversations stored on-device, no account or cloud sync required
- Secure API key storage (AndroidX EncryptedSharedPreferences)

## Tech Stack

- **Language:** Kotlin 2.0
- **UI:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM with ViewModel + StateFlow
- **Local storage:** Room
- **Networking:** Retrofit + OkHttp with SSE
- **DI:** Hilt
- **Minimum Android:** API 29 (Android 10)

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 21+ (for building)
- An [OpenRouter API key](https://openrouter.ai/keys)

### Build

```bash
# Set up local.properties
echo "sdk.dir=/path/to/android-sdk" > local.properties

# Build debug APK
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### First Run

1. Install the APK on a device (API 29+)
2. Go to **Settings** (gear icon) and enter your OpenRouter API key
3. Start chatting with any AI model

## Project Structure

```
app/src/main/java/com/openrouter/chat/
├── data/
│   ├── local/         # Room database, DAOs, entities
│   └── remote/        # OpenRouter API, DTOs
├── domain/
│   ├── model/         # Domain models
│   └── repository/    # Repository implementations
├── di/               # Hilt modules
└── ui/
    ├── components/    # Shared UI components
    ├── navigation/    # Navigation graph
    ├── screens/      # Models, Conversations, Chat, Settings
    └── theme/         # Material 3 theming
```

## License

MIT