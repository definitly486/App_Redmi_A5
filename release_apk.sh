#!/bin/sh

# Проверка установлен ли gh
if ! command -v gh >/dev/null 2>&1; then
    echo "gh tools не установлен"
    zenity --title="release_apk" --warning --text="gh tools не установлен"
    exit 1
fi

APK_DIR="$HOME/App_Redmi_A5/app/build/outputs/apk/release"

# Проверка существования директории
if [ ! -d "$APK_DIR" ]; then
    zenity --title="release_apk" --warning --text="Папка с apk не найдена: $APK_DIR"
    exit 1
fi

# Поиск apk файла
APK_FILE=$(find "$APK_DIR" -maxdepth 1 -type f -name "*.apk" | head -n 1)

if [ -z "$APK_FILE" ]; then
    zenity --title="release_apk" --warning --text="APK файл не найден"
    exit 1
fi

# Имя релиза (можно заменить на своё)
RELEASE_TAG="apk"

# Загрузка APK в релиз
gh release upload "$RELEASE_TAG" "$APK_FILE" --clobber

zenity --title="release_apk" --info --text="APK успешно загружен: $APK_FILE"
