# CloudBuddy MVP

## Что реализовано

- Onboarding с выбором имени и одного из четырёх характеров облачка.
- Эмоциональный check-in с шестью состояниями и необязательной заметкой.
- Главный экран с живой Canvas-анимацией, погодой, прогрессом путешествия и пасхалкой.
- Безопасный чат:
  - полноценный локальный fallback без сети;
  - кризисный сценарий;
  - прозрачное предупреждение, что компаньон не является врачом;
  - подключаемый backend без API-ключа в APK.
- Игра с отдельным тестируемым движком:
  - классический режим с рекордом;
  - спокойный режим без проигрыша;
  - старт, пауза, рестарт и выход.
- Локальное хранение через DataStore:
  - onboarding и характер;
  - последнее настроение;
  - лучший игровой результат;
  - настройки;
  - до 12 выбранных заметок.
- Экран управления памятью и полный локальный сброс.
- Собственная светлая/тёмная Material 3 тема.
- Unit-тесты эмоциональной модели и игрового движка.
- Минимальный Node.js backend для OpenAI Responses API.

## Архитектура

```text
app/
├── app/                 # AppContainer, ViewModel, NavHost
├── core/model/          # модели эмоций, погоды, характера и игры
├── data/                # repository + DataStore
├── feature/
│   ├── onboarding/
│   ├── checkin/
│   ├── home/
│   ├── chat/
│   ├── game/
│   ├── memory/
│   └── settings/
└── ui/                  # тема и общие Compose-компоненты

backend/
├── server.mjs           # HTTP proxy к Responses API
└── package.json
```

Android использует MVVM и однонаправленный поток данных. `CloudBuddyViewModel` объединяет состояние репозитория и действия экранов. Игровой `GameEngine` не зависит от Compose, поэтому его физика тестируется обычными unit-тестами.

## Сборка Android

AGP 8.6 требует JDK 17 или новее. Можно временно использовать bundled JBR Android Studio без изменения системного `JAVA_HOME`:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug
```

APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Проверки:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
node --check backend/server.mjs
```

## Режимы чата

По умолчанию backend URL пуст, поэтому приложение полностью работает с локальным собеседником.

Для подключения развёрнутого backend:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug -PCLOUDBUDDY_BACKEND_URL=https://api.example.com
```

Если backend вернул ошибку или недоступен, Android автоматически использует локальный ответ.

## Backend

Требуется Node.js 20+:

```powershell
Set-Location backend
$env:OPENAI_API_KEY='...'
$env:OPENAI_MODEL='gpt-5.4-mini'
npm start
```

Секрет нельзя добавлять в Android, репозиторий или `local.properties`. Официальная рекомендация OpenAI — направлять запросы мобильного приложения через собственный backend: <https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety>.

Backend использует Responses API `POST /v1/responses`, ограничивает размер запроса и частоту обращений одного IP. Для публичного запуска ещё потребуются пользовательская авторизация, распределённый rate limiting, HTTPS, мониторинг расходов и формальная политика обработки данных.

## Границы MVP

- Настройка мягких напоминаний сохраняется, но системные уведомления пока не планируются автоматически: сначала нужно проверить, что они действительно полезны и не создают давления.
- История чата намеренно не сохраняется. Локально хранятся только заметки, которые пользователь сам ввёл в check-in при включённой памяти.
- Backend не развёрнут автоматически и не содержит ключа. Для реальных ответов OpenAI владелец проекта должен предоставить сервер и секрет через переменную окружения.
- CloudBuddy не ставит диагнозы и не заменяет профессиональную или экстренную помощь.
