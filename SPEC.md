# OpenRouter Chat - Specification Document

## 1. Project Overview

**Project Name:** OpenRouter Chat  
**Project Type:** Native Android Application  
**Core Functionality:** A Telegram-style chat application that allows users to converse with AI models via OpenRouter API. Users can browse available models, manage multiple conversations per model, and chat with AI in a real-time streaming messaging interface.

## 2. Technology Stack & Choices

- **Language:** Kotlin 1.9.x
- **UI Framework:** Jetpack Compose with Material Design 3
- **Target SDK:** Android 34 (API 29+ minimum)
- **Architecture:** MVVM with Clean Architecture principles
- **State Management:** ViewModel + StateFlow/SharedFlow
- **Local Database:** Room Persistence Library
- **Networking:** Retrofit 2 + OkHttp with SSE (Server-Sent Events) support
- **Dependency Injection:** Hilt
- **Security:** AndroidX Security Crypto for API key storage
- **Markdown Rendering:** Custom Compose markdown parser
- **Coroutines:** Kotlin Coroutines + Flow

### Key Dependencies
- Jetpack Compose BOM 2024.02.00
- Room 2.6.1
- Retrofit 2.9.0
- OkHttp 4.12.0
- Hilt 2.50
- AndroidX Security 1.1.0-alpha06

## 3. Feature List

### Model List Screen
- Display all available OpenRouter models as contacts
- Show model name, provider (from API), last message, timestamp
- Pull-to-refresh to reload models
- Navigate to conversation list on tap

### Conversation List Screen
- Display all conversations for selected model
- Each conversation shows auto-generated title (from first message, ~40 chars)
- Timestamp display
- FAB to start new conversation
- Navigate to chat screen on tap

### Chat Screen
- Standard message bubbles (user right-aligned, model left-aligned)
- Text input bar at bottom with send button
- Real-time streaming responses via SSE
- Markdown rendering (bold, italic, code blocks)
- Auto-scroll to latest message during streaming

### Settings Screen
- OpenRouter API key input (stored securely)
- Clear all data option
- App version info

### Data Management
- Room database for local storage
- Entities: Model, Conversation, Message
- Relationships: Model -> Conversations -> Messages

## 4. UI/UX Design Direction

### Visual Style
- Telegram-inspired design with Material Design 3 theming
- Clean, minimalist interface
- Chat bubbles with rounded corners
- Floating action buttons

### Color Scheme
- Primary: Blue (#2481CC)
- Secondary: Light Blue (#EEFFEE)
- Background: White/Light mode
- User bubbles: Light blue (#DCF8C6)
- Model bubbles: Gray (#E5E5EA)

### Layout Approach
- Bottom navigation-free design
- Stack-based navigation (Model List -> Conversation List -> Chat)
- Settings accessible from toolbar menu
- FAB on conversation list only

### Navigation Flow
```
Model List → Conversation List → Chat Screen
                ↓                   
            Settings (via menu)
```