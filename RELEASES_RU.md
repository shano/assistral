[English](RELEASES.md) | **Русский**

# Процесс выпуска

## Создание новой версии

1. **Обновление номеров версий**
   - Обновите `versionCode` и `versionName` в `app/build.gradle`
   - Создайте новый файл changelog в `metadata/en-US/changelogs/{versionCode}.txt`

2. **Создание Git-тега**
   ```bash
   git tag -a v3.0 -m "Релиз версии 3.0"
   git push origin v3.0
   ```

3. **GitHub Actions**
   - Workflow релиз автоматически запустится при пуше тега
   - Соберёт подписанный APK и создаст релиз на GitHub

4. **Отправка в F-Droid**
   - F-Droid автоматически обнаружит релиз через metadata
   - Приложение соберётся на инфраструктуре F-Droid

## Инструкции для мейнтейнеров

### GitHub Secrets (для подписанных релизов)
Настройте секреты в репозитории GitHub:
- `SIGNING_KEY`: Keystore в Base64
- `ALIAS`: Имя ключа
- `KEY_STORE_PASSWORD`: Пароль хранилища  
- `KEY_PASSWORD`: Пароль ключа

### Генерация Keystore
```bash
keytool -genkey -v -keystore assistral-release-key.keystore -alias assistral -keyalg RSA -keysize 2048 -validity 10000
```

### Конвертация в Base64 для GitHub Secrets
```bash
base64 assistral-release-key.keystore | tr -d '\n'
```

## Текущий релиз: v3.0

- **Version Code**: 300
- **Version Name**: 3.0
- **Дата релиза**: 2025-01-XX
- **Основные изменения**: Полная миграция с gptAssist/ChatGPT на Ассистрал/Mistral Le Chat

## История релизов

### v3.0 (300) - 2025-01-XX
- Полная переработка с gptAssist на Ассистрал
- Миграция с ChatGPT на Mistral Le Chat
- Обновление брендинга, package names и metadata
- Первый релиз для F-Droid
