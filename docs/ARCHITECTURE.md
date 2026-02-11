# CloudBuddy — структура проекта и архитектура

## Обзор

CloudBuddy — Android-приложение на **Kotlin** с **Jetpack Compose**. Сборка — Gradle (Kotlin DSL), один модуль `app`. Сейчас в приложении одна основная активность и один экран — мини-игра в стиле Flappy Bird.

---

## Структура каталогов

```
CloudBuddy/
├── app/
│   ├── build.gradle.kts          # Конфигурация модуля app
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/dev/catandbunny/cloudbuddy/
│       │   │   ├── MainActivity.kt           # Точка входа, Compose setContent
│       │   │   └── ui/
│       │   │       ├── component/
│       │   │       │   └── GameScreen.kt     # Экран мини-игры (Flappy-like)
│       │   │       └── theme/
│       │   │           ├── Color.kt
│       │   │           ├── Theme.kt          # CloudBuddyTheme, Material 3
│       │   │           └── Type.kt
│       │   └── res/                           # Ресурсы: drawable, mipmap, values, xml
│       ├── androidTest/                      # Инструментальные тесты
│       └── test/                             # Юнит-тесты
├── gradle/
│   ├── libs.versions.toml                    # Версии библиотек и плагинов
│   └── wrapper/
├── build.gradle.kts                          # Корневой build (плагины)
├── settings.gradle.kts                       # rootProject.name = "CloudBuddy", include(":app")
├── gradle.properties
├── gradlew / gradlew.bat
└── docs/                                     # Документация (этот файл и ROADMAP)
```

---

## Ключевые компоненты

### MainActivity

- `ComponentActivity`, `enableEdgeToEdge()`.
- `setContent { CloudBuddyTheme { GameScreen() } }` — единственный UI — экран игры.
- Пакет: `dev.catandbunny.cloudbuddy`.

### GameScreen (`ui/component/GameScreen.kt`)

- **Composable** с игровым циклом в `LaunchedEffect`.
- Логика в стиле Flappy Bird:
  - Персонаж (Lumy) — круг по вертикали, тап = прыжок, гравитация.
  - Препятствия — «облака» (пара прямоугольников с просветом), движутся влево.
  - Столкновение с препятствием или границами экрана → Game Over.
  - Счёт увеличивается при прохождении препятствия.
- Рисование через `Canvas` (фон, персонаж, препятствия, текст счёта и Game Over).
- Данные: `Cloud(x, gapY)`, состояние через `remember` / `mutableStateOf` / `mutableStateListOf`.
- Требует API 35 из-за `@RequiresApi(35)` (при необходимости можно понизить).

### Тема

- `CloudBuddyTheme` в `ui/theme/Theme.kt` — Material 3, поддержка тёмной темы и динамических цветов (Android 12+).
- Цвета и типографика в `Color.kt` и `Type.kt`.

### Ресурсы

- `res/values/`: `strings.xml` (app_name = CloudBuddy), `colors.xml`, `themes.xml`.
- Иконки приложения: `drawable`, `mipmap-*`.
- `res/xml/`: правила бэкапа и извлечения данных.

---

## Зависимости (кратко)

- AndroidX Core, Lifecycle, Activity Compose.
- Compose BOM, UI, Material 3.
- JUnit и Espresso для тестов.
- Версии задаются в `gradle/libs.versions.toml`; в `app/build.gradle.kts` подключаются через `libs.*`.

---

## Архитектура (текущая и целевая)

- **Сейчас:** один экран (GameScreen), без навигации, без слоёв (data/domain/ui). Вся логика игры и отрисовка — в одном Composable.
- **Целевое направление** (см. ROADMAP):
  - Разделение на экраны: главный (питомец + чат), игра, возможно настройки.
  - Слой данных: локальное хранение состояния питомца (настроение, скука), история чата; API OpenAI.
  - Доменная логика: «скучно/не скучно», влияние игры на настроение.
  - UI: Compose, навигация (Compose Navigation), переиспользуемые компоненты.

Детальный поэтапный план добавления фич и развития приложения описан в **[ROADMAP.md](ROADMAP.md)**.
