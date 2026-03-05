# Процесс выпуска

## Текущая стабильная версия

- **Тег:** `v3.2.2`
- **Название версии:** `3.2.2`
- **Код версии:** `321`

## Чек-лист выпуска

1. Обновите значения версии в `app/build.gradle`:
   - `versionName`
   - `versionCode`
   - `project.ext.versionNameString`
2. Добавьте файл с изменениями: `metadata/ru-RU/changelogs/<versionCode>.txt`
3. Зафиксируйте и отправьте изменения в `master`.
4. Создайте и отправьте тег:
   ```bash
   git tag -a vX.Y.Z -m "Выпуск версии X.Y.Z"
   git push origin vX.Y.Z
   ```
5. Убедитесь, что GitHub Actions `Build Release APK` выполнен успешно.
6. Убедитесь, что в GitHub Release присутствует файл `assistral-release-X.Y.Z.apk`.

## Требуемые GitHub Secrets для подписанных релизов

Настроены в параметрах репозитория: **Secrets and variables -> Actions**:

- `SIGNING_KEYSTORE_BASE64`
- `SIGNING_STORE_PASSWORD`
- `SIGNING_KEY_ALIAS`
- `SIGNING_KEY_PASSWORD`

Они соответствуют свойствам Gradle, используемым в `app/build.gradle`:

- `MYAPP_RELEASE_STORE_FILE`
- `MYAPP_RELEASE_STORE_PASSWORD`
- `MYAPP_RELEASE_KEY_ALIAS`
- `MYAPP_RELEASE_KEY_PASSWORD`

## Настройка хранилища ключей

Сгенерируйте хранилище ключей:

```bash
keytool -genkeypair -v \
  -keystore assistral-release-key.keystore \
  -alias assistral \
  -keyalg RSA -keysize 4096 -validity 10000
```

Преобразуйте хранилище ключей в однострочный base64 (для `SIGNING_KEYSTORE_BASE64`):

```bash
base64 assistral-release-key.keystore | tr -d '\n'
```

## Локальная подписанная сборка (опционально)

```bash
./gradlew :app:assembleRelease \
  -PMYAPP_RELEASE_STORE_FILE=/abs/path/to/assistral-release-key.keystore \
  -PMYAPP_RELEASE_STORE_PASSWORD="$MYAPP_RELEASE_STORE_PASSWORD" \
  -PMYAPP_RELEASE_KEY_ALIAS="$MYAPP_RELEASE_KEY_ALIAS" \
  -PMYAPP_RELEASE_KEY_PASSWORD="$MYAPP_RELEASE_KEY_PASSWORD"
```

## Примечания по обновлению F-Droid

При обновлении метаданных `fdroiddata`:

1. Добавьте **новый** блок в `Builds:` для каждого релиза (не заменяйте старые блоки).
2. Установите `commit` на точный коммит/тег, использованный для GitHub release APK.
3. Обновите:
   - `CurrentVersion`
   - `CurrentVersionCode`
4. Оставьте `AllowedAPKSigningKeys` без изменений, если ключ подписи не изменился.
