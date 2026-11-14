# Project Horizen (Fabric mod, Minecraft 1.21.11)

Press RIGHT SHIFT in game (or click the "Horizen" button on the title/pause screen) for the menu.
Hold C to zoom (rebind in Options > Controls > Project Horizen).

## Build (one time)
You need JDK 21 installed.

1. Go to https://fabricmc.net/develop/template/ , pick Minecraft 1.21.11, any mod name, and download the zip.
2. From that template copy `gradlew`, `gradlew.bat` and the whole `gradle` folder into THIS folder
   (next to build.gradle). Do not copy anything else from the template.
3. Open a terminal in this folder and run:
   - Windows:  gradlew build
   - Mac/Linux: ./gradlew build
4. Your mod is `build/libs/projecthorizen-1.0.0.jar` (not the "-sources" one).

## Install
1. Install Fabric Loader for 1.21.11 (https://fabricmc.net/use/installer/), then launch once.
2. Download Fabric API for 1.21.11 from Modrinth and put it in `.minecraft/mods`.
3. Put `projecthorizen-1.0.0.jar` in `.minecraft/mods` too.
4. Launch the Fabric 1.21.11 profile.

Config is saved to `.minecraft/config/projecthorizen.json`.
