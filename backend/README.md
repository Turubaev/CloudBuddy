# CloudBuddy backend

Минимальный прокси между Android-приложением и OpenAI Responses API. Секретный ключ хранится только в окружении сервера.

## Запуск

Требуется Node.js 20+.

```powershell
$env:OPENAI_API_KEY="..."
$env:OPENAI_MODEL="gpt-5.4-mini"
npm start
```

Проверка:

```text
GET http://127.0.0.1:8787/health
```

Контракт клиента:

```http
POST /v1/chat
Content-Type: application/json

{
  "message": "Мне тревожно",
  "buddyName": "Луми",
  "personality": "WARM",
  "mood": "ANXIOUS"
}
```

Ответ:

```json
{"reply":"Я рядом…"}
```

Для Android-сборки передайте URL своего развёрнутого HTTPS backend:

```powershell
./gradlew assembleDebug -PCLOUDBUDDY_BACKEND_URL=https://example.com
```

Без параметра приложение автоматически использует локальный offline-режим.

Перед публичным запуском добавьте полноценную авторизацию пользователей, постоянное распределённое rate limiting, наблюдаемость и политику хранения данных. Текущий in-memory limiter рассчитан на закрытый MVP.
