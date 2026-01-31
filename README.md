# Aurora Chat — Backend

A secure, real-time chat backend built with Java 21 and Spring Boot. Provides REST APIs, WebSocket messaging, Firebase authentication, end-to-end encrypted message storage, and secure file sharing via Cloudinary.

> Built during the **Sourcery Academy 2025 Fall** internship program.

## Features

- **Firebase Authentication** — JWT token validation via Firebase Admin SDK (RS256 asymmetric signing)
- **Real-Time Messaging** — WebSocket (STOMP over SockJS) for instant message delivery, typing indicators, and presence
- **Secure File Sharing** — 8 layers of upload validation, proxied downloads (Cloudinary URLs never exposed to clients)
- **Servers & Topics** — Server creation with topic-based channels for organized group conversations
- **Pinned Messages** — Pin/unpin messages within group chats
- **User Management** — Profile updates, user search, avatar uploads
- **Automatic File Cleanup** — Cron job deletes expired files from Cloudinary and database after 7 days
- **Rate Limiting** — 20 file uploads per user per minute
- **API Documentation** — Swagger UI / OpenAPI 3.0

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 3.3.4 | Application framework |
| PostgreSQL | 17 | Database |
| MyBatis | — | SQL mapper |
| Flyway | — | Database migrations |
| Firebase Admin SDK | — | JWT authentication |
| Cloudinary | — | Cloud file storage |
| Apache Tika | — | File type detection (magic bytes) |
| Docker | — | Local development services |
| Swagger / OpenAPI | 3.0 | API documentation |

## Getting Started

### Prerequisites

- Java 21
- Docker & Docker Compose
- Gradle

### 1. Start Database

```bash
docker compose -p edvinas-be up -d
```

Database credentials: `db_user` / `password` (configured in `application.yml`)

### 2. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com) → your project → **Service Accounts**
2. Click **Generate new private key**
3. Rename the downloaded file to `firebase.json`
4. Place it in `src/main/resources/firebase/firebase.json`

For tests, create `src/test/resources/test-firebase.json` with the same structure (or use dummy values).

### 3. Cloudinary Setup (for file uploads)

1. Create a [Cloudinary](https://cloudinary.com) account
2. Create `src/main/resources/cloudinary.json`:
```json
{
  "cloud_name": "your-cloud-name",
  "api_key": "your-api-key",
  "api_secret": "your-api-secret"
}
```
3. For tests, create `src/test/resources/test-cloudinary.json` with the same structure.

### 4. Run

```bash
# Local profile
./gradlew bootRun --args="--spring.profiles.active=local"

# Production profile
./gradlew bootRun --args="--spring.profiles.active=prod"
```

### 5. Linting

```bash
./gradlew check
```

Runs Spotless, SpotBugs, and PMD.

## API Documentation

| Environment | Swagger UI | OpenAPI JSON |
|---|---|---|
| Local | http://localhost:8080/swagger-ui/index.html | http://localhost:8080/v3/api-docs |

## File Sharing Security

The file sharing system implements **8 layers of upload security**:

1. **JWT Authentication** — Firebase token required
2. **Rate Limiting** — 20 uploads/user/minute
3. **File Count Limit** — Max 5 files per message
4. **File Size Limit** — 10 MB max per file
5. **Filename Length** — Max 255 characters
6. **Extension Blacklist** — 22 dangerous extensions blocked
7. **Magic Byte Detection** — Apache Tika verifies actual file type
8. **UUID Filenames** — Original filenames replaced with UUIDs in storage

**Download security:** Files are proxied through the backend — Cloudinary URLs are **never exposed** to clients. The backend fetches from Cloudinary and streams raw bytes to authenticated, authorized users.

**Automatic cleanup:** A cron job runs daily at 3 AM, deleting files older than 7 days from both Cloudinary and the database.

## Project Structure

```
src/main/java/com/example/kns/
├── chat/           # Chat messaging (WebSocket + REST)
├── file/           # File upload, download, storage, cleanup
├── group/          # Group/server management
├── security/       # Firebase auth, security config
├── server/         # Server & topic management
└── user/           # User profiles & search
```

## Team

**Students:**
- Egle Mickeviciute
- Lukas Kasparavicius
- Matas Brazauskas
- Povilas Sakalauskas
- Ruta Gaizutyte
- Viktoras Timofejevas

**Mentor:** Edvinas Jaskovikas

## Related

- [Aurora Chat Frontend](https://github.com/xmivte/aurora-chat-frontend) — React + TypeScript frontend

---

*Built at Sourcery Academy 2025*
