# CloudBuddy: подготовка релиза в RuStore

## Зафиксированные параметры

- Application ID: `dev.catandbunny.cloudbuddy`
- Plus: `299 ₽ / месяц`
- Product ID: `cloudbuddy_plus_monthly`
- Free: 15 приветственных AI-сообщений, затем 5 в сутки
- Plus: 30 AI-сообщений в сутки
- BYOK оплачивается пользователем и не открывает Plus
- Offline-чат доступен без ограничений на всех тарифах

## Что уже реализовано

- RuStore Pay SDK 10.5.0, без устаревшего BillingClient.
- Получение продукта и отформатированной цены.
- Покупка подписки и восстановление активного статуса.
- Обработка deeplink при возврате из банковского приложения.
- Локальное отображение квоты и независимая серверная проверка Free-квоты.
- Конфигурация без секретов через Gradle properties.

Сборка после получения ID приложения в консоли:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleRelease `
  -PRUSTORE_CONSOLE_APP_ID=123456 `
  -PRUSTORE_PLUS_PRODUCT_ID=cloudbuddy_plus_monthly `
  -PCLOUDBUDDY_BACKEND_URL=https://api.example.com
```

`RUSTORE_CONSOLE_APP_ID` и Product ID не являются секретами. OpenAI API key, ключи RuStore Public API и пароль release-keystore должны храниться только в защищённых переменных CI/сервера.

## Что нужно сделать в RuStore Console

1. Зарегистрировать организацию и включить монетизацию.
2. Создать приложение с package `dev.catandbunny.cloudbuddy`.
3. Создать месячную подписку `cloudbuddy_plus_monthly` за 299 ₽ без пробного периода.
4. Добавить VK ID тестировщика и включить тестовый режим.
5. Собрать и загрузить APK/AAB той же release-подписью, которая будет использоваться дальше.
6. Проверить покупку, отмену, восстановление, grace/hold и возврат из банковского приложения.

Текущая проверочная сборка `app/build/outputs/bundle/release/app-release.aab` создаётся без release-keystore и поэтому не предназначена для загрузки в Console. Подпись нужно настроить один раз до первой публикации и затем сохранять неизменной.

## Что блокирует публичный релиз

- постоянный backend и база для квот;
- серверная валидация Plus через RuStore Public API и серверные уведомления;
- release-keystore и безопасная CI-конфигурация подписи;
- URL политики конфиденциальности и пользовательского соглашения с условиями автопродления;
- support email, иконка, скриншоты и тексты карточки;
- ручная проверка на реальном устройстве с RuStore и VK ID;
- бюджетные лимиты и мониторинг OpenAI.

Временный `ALLOW_UNVERIFIED_PLUS=true` на backend предназначен только для локальной/sandbox-проверки интерфейса. В production он должен оставаться `false`.

## Официальная документация

- Pay SDK: <https://www.rustore.ru/help/sdk/pay>
- Kotlin/Java 10.5.0: <https://www.rustore.ru/help/sdk/pay/kotlin-java/10-5-0>
- Создание подписки: <https://www.rustore.ru/help/en/developers/monetization/create-app-subscription>
- Sandbox: <https://www.rustore.ru/help/developers/monetization/sandbox/testing-sdk-pay>
