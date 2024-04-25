# StarMade Launcher Version 3
This is the launcher for [StarMade](https://www.star-made.org/). It is a Java application that will download and install
StarMade for you. It will also keep StarMade up to date and can be used to switch branches.
The launcher can also automatically download and install the correct Java version as well. Game versions below 0.300.100
use Java 8, while versions 0.300.100 and above use Java 18.
This launcher was created due to the old one being outdated and only being able to use Java 7.

# Installation instructions
## Windows
1. Download and install Java 11 for [Windows](https://adoptium.net/temurin/releases/?os=windows&version=11&package=jdk)
2. Run `StarMade Starter.exe`
## Linux
1. Install Java:
   1. Download and install Java 11 for [Linux](https://adoptium.net/temurin/releases/?os=linux&version=11&package=jdk) or [Alpine Linux](https://adoptium.net/temurin/releases/?os=alpine-linux&version=11&package=jdk).
2. Ensure the install script is executable:
   1. Right click inside the extracted folder and click "Open in Terminal". If that option isn't there for some reason, try navigating there using `cd`
   2. run `sudo chmod +x "StarMade Starter.sh"`
   3. run `./"StarMade Starter.sh"`
## Mac
1. Download and install Java 11 for [Mac OS](https://adoptium.net/temurin/releases/?os=mac&version=11&package=jdk)
2. Todo (I don't have a Mac, but if you do and know how this works, contact `thederpgamer` on Discord)
   
# Note
This launcher requires at least Java 11 to work.
You can download Java 11 here: [Adoptium JDK](https://adoptium.net/temurin/releases/?os=any&version=11&package=jdk)

# Building from source
1. Clone the Repo (duh)
    1. run `https://github.com/garretreichenbach/New-StarMade-Launcher.git`, or
    2. Clone `https://github.com/garretreichenbach/New-StarMade-Launcher` in GitHub Desktop
2. Compile the .jar file
    1. run `gradlew all_jar` (Not `gradlew jar`!), or
    2. run `gradlew win_jar`, `gradlew mac_jar`, or `gradlew linux_jar` for a specific platform

# Credits
- Schine for making such a great game
- The StarLoader Team for making a new launcher
Note: Pretty much every class in `src/starmade` was copied over from the main repo.
