# OpenRouter Chat

A native Android chat app for conversing with AI models via the [OpenRouter](https://openrouter.ai) API, with a UI inspired by Telegram.

## Features

- Browse all available OpenRouter models like a contact list
- Maintain multiple independent conversations per model
- Streaming responses in real time
- Markdown rendering (bold, italic, code blocks)
- Fully local — all conversations stored on-device, no account or cloud sync required
- Secure API key storage

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM with ViewModel + StateFlow
- **Local storage:** Room
- **Networking:** Retrofit / Ktor
- **Minimum Android version:** Android 10 (API level 29)

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- An [OpenRouter API key](https://openrouter.ai/keys)

### Setup

1. Clone the repository
   ```bash
   git clone <repo-url>
   cd openrouter-chat
   ```

2. Open the project in Android Studio

3. Build and run on a device or emulator (API 29+)

4. On first launch, go to **Settings** and enter your OpenRouter API key

## Project Structure

```
app/
├── data/
│   ├── local/        # Room database, DAOs, entities
│   └── remote/       # OpenRouter API client, models
├── domain/           # Use cases, repository interfaces
├── ui/
│   ├── models/       # Model list screen
│   ├── chats/        # Chat list screen
│   ├── chat/         # Chat detail screen
│   └── settings/     # API key settings screen
└── MainViewModel.kt
```

## Roadmap

- [ ] System prompt configuration per conversation
- [ ] Model search and filtering
- [ ] Conversation export
- [ ] Image/file attachments

## License

MIT