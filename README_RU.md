# SaveDTF

![DTF_logo_low_res](https://user-images.githubusercontent.com/47672780/164269052-5ad8858d-c8cb-4152-951e-873316b7562c.png)

**SaveDTF - это приложение, работающее на
базе [Jetpack Compose for Desktop](https://www.jetbrains.com/ru-ru/lp/compose-mpp/ "Jetpack Compose for Desktop"). Оно
может сохранять страницы из [DTF](https://dtf.ru "DTF"), [VC](https://vc.ru "VC")
и [TJournal](https://tjournal.ru "TJournal")**.

## Особенности ✨

- Возможность загрузки нескольких статей одновременно
- Сохранение медиафайлов вместе со статьей
- Кэширование медиа
- Загрузка статей из закладок
- Загрузить все статьи из профиля пользователя

## Работа в процессе 🚧

- Загрузка статьи с комментариями

## Установка ⚙️

### Windows:

#### Требования: 
- Windows 7 или более поздняя версия
- [Visual C++ Redistributable for Visual Studio 2015](https://www.microsoft.com/en-us/download/details.aspx?id=48145 "Visual C++ Redistributable for Visual Studio 2015 ")

#### [Скачать .exe файл](https://github.com/DareFox/SaveDTF-compose/releases) 

### Linux 

#### Debian, Ubuntu, Mint и т.п. (.deb)

- #### [Скачать .deb файл](https://github.com/DareFox/SaveDTF-compose/releases) 

#### Дистрибутивы на базе Red Hat (.rpm) 

- #### [Сборка из исходников](#сборка-из-исходников-)

### MacOS

#### Инструкция:
- [Скачайте .pkg файл](https://github.com/DareFox/SaveDTF-compose/releases) 
- Смонитруйте файл
- Переместите SaveDTF в папку **Приложения**.
- Откройте Терминал и введите эту команду
```bash
xattr -d com.apple.quarantine /Applications/SaveDTF.app
```
**Последний шаг очень важен!**

Без этого macOS не запустит приложение, потому что оно не подписано. Команда выше снимает это ограничение

![macOS Error Message](https://github.com/DareFox/SaveDTF-Compose/blob/main/.github/resources/macOS-savedtf-damaged.jpg "macOS Сообщение об ошибке")

Если вы не хотите удалять проверки безопасности для приложения, то [соберите приложение сами из исходников](#сборка-из-исходников-)

## Сборка из исходников 🔨
Требования: 
- JDK 17

### Unix (Linux/macOS)
Инструкция: 
- Клонируйте репозиторий командой `git clone https://github.com/DareFox/SaveDTF-Compose.git` или [скачайте через браузер](https://github.com/DareFox/SaveDTF-Compose/archive/refs/heads/main.zip "скачайте через браузер")
- Откройте склонированный репозиторий
- Сделайте файл gradlew исполняемым с помощью команды `chmod +x ./gradlew`.
- Соберите установочный пакет с помощью `./gradlew package` **ИЛИ** выполните команду `./gradlew runDistributable` для запуска приложения без установки

### Windows 
Инструкция: 
- Клонируйте репозиторий через `git clone https://github.com/DareFox/SaveDTF-Compose.git` или [скачайте через браузер](https://github.com/DareFox/SaveDTF-Compose/archive/refs/heads/main.zip "скачайте через браузер")
- Откройте склонированный репозиторий
- Соберите установочный пакет с помощью `./gradlew.bat package` **ИЛИ** выполните команду `./gradlew.bat runDistributable` для запуска приложения без установки

## Лицензия 📃
MIT

**ХВАТАЙ БЕСПЛАТНО**
