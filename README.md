# SaveDTF

![DTF_logo_low_res](https://user-images.githubusercontent.com/47672780/164269052-5ad8858d-c8cb-4152-951e-873316b7562c.png)

**SaveDTF is a desktop app, powered
by [Jetpack Compose for Desktop](https://www.jetbrains.com/ru-ru/lp/compose-mpp/ "Jetpack Compose for Desktop"). It can
save pages from [DTF](https://dtf.ru "DTF"), [VC](https://vc.ru "VC") and [TJournal](https://tjournal.ru "TJournal")**

**[Инструкция на русском языке](https://github.com/DareFox/SaveDTF-Compose/blob/main/README_RU.md)**

## Features ✨

- Ability to download multiple articles at once
- Saving media along with the article
- Media caching
- Download articles from bookmarks
- Download all articles from user profile

## Work in progress 🚧
- Download article with comments

## Installation ⚙️

### Windows:

#### Requirements: 
- Windows 7 or later
- [Visual C++ Redistributable for Visual Studio 2015 ](https://www.microsoft.com/en-us/download/details.aspx?id=48145 "Visual C++ Redistributable for Visual Studio 2015 ")

#### [Download .exe file](https://github.com/DareFox/SaveDTF-compose/releases) 

### Linux 

#### Debian, Ubuntu, Mint and etc (.deb)

- #### [Download .deb file](https://github.com/DareFox/SaveDTF-compose/releases) 

#### Red Hat based distros (.rpm) 

- #### [Build from sources](#build-from-sources-)

### MacOS

#### Instructions:
- [Download .pkg file](https://github.com/DareFox/SaveDTF-compose/releases) 
- Mount file
- Move SaveDTF to **Applications** folder
- Open Terminal and enter this command
```bash
xattr -d com.apple.quarantine /Applications/SaveDTF.app
```
**Last step is very important**

Without this, macOS will prevent launching app because it's unsigned. Command above removes this restriction

![macOS Error Message](https://github.com/DareFox/SaveDTF-Compose/blob/main/.github/resources/macOS-savedtf-damaged.jpg "macOS Error Message")

If you don't wan't to remove safety checks for the app, then [build app from source code](#build-from-sources-)

## Build from sources 🔨
Requirements: 
- JDK 17

### Unix (Linux/macOS)
Instructions: 
- Clone the repo via `git clone https://github.com/DareFox/SaveDTF-Compose.git` or [download it ](https://github.com/DareFox/SaveDTF-Compose/archive/refs/heads/main.zip "download it ") 
- Open cloned repo
- Make gradlew file executable with command `chmod +x ./gradlew`
- Build installation package via `./gradlew package` **OR** run `./gradlew runDistributable` to run the app without installation

### Windows 
Instructions: 
- Clone the repo via `git clone https://github.com/DareFox/SaveDTF-Compose.git` or [download it ](https://github.com/DareFox/SaveDTF-Compose/archive/refs/heads/main.zip "download it ") 
- Open cloned repo
- Build installation package via `./gradlew.bat package` **OR** run `./gradlew.bat runDistributable` to run the app without installation

## License 📃

MIT

**F-r-e-e, that spells free**!

