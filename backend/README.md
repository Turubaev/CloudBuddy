# CloudBuddy backend

Минимальный прокси между Android-приложением и OpenAI Responses API. Секретный ключ хранится только в окружении сервера.

## Запуск

Требуется Node.js 20+.

```powershell
$env:OPENAI_API_KEY="..."
$env:OPENAI_MODEL="gpt-5.6-luna"
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
  "mood": "ANXIOUS",
  "installationId": "стабильный-id-установки",
  "requestedTier": "FREE"
}
```

Ответ:

```json
{"reply":"Я рядом…","quota":{"plan":"FREE","remaining":14,"limit":15,"welcome":true}}
```

Для Android-сборки передайте URL своего развёрнутого HTTPS backend:

```powershell
./gradlew assembleDebug -PCLOUDBUDDY_BACKEND_URL=https://example.com
```

Без параметра приложение автоматически использует локальный offline-режим.

## Квоты тарифов

Backend повторно проверяет лимит независимо от Android-клиента:

- Free: 15 приветственных сообщений, затем 5 в сутки;
- Plus: 30 сообщений в сутки;
- BYOK не обращается к этому backend и оплачивается владельцем личного ключа.

Переменные окружения:

```text
FREE_WELCOME_MESSAGES=15
FREE_DAILY_MESSAGES=5
PLUS_DAILY_MESSAGES=30
TRUSTED_PLUS_CLIENTS=id1,id2
ALLOW_UNVERIFIED_PLUS=false
```

`ALLOW_UNVERIFIED_PLUS=true` допустим только для локальной разработки. По умолчанию backend игнорирует заявленный клиентом Plus, пока идентификатор не был подтверждён доверенным серверным процессом.

Перед публичным запуском нужны постоянная база квот, серверная проверка подписки через RuStore Public API/уведомления, полноценная авторизация, распределённый rate limiting и наблюдаемость. Текущее in-memory хранилище квот рассчитано на локальный и sandbox-MVP и очищается при перезапуске процесса.
