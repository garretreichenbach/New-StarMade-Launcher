# StarMade Launcher Version 3.0.8

This is the launcher for [StarMade](https://www.star-made.org/). It is a Java application that will download and install
StarMade for you. It will also keep StarMade up to date and can be used to switch branches.
The launcher can also automatically download and install the correct Java version as well. Game versions below 0.300.100
use Java 8, while versions 0.300.100 and above use Java 18.
This launcher was created due to the old one being outdated and only being able to use Java 7.

# Credits

- Schine for making such a great game
- The StarLoader Team for making a new launcher

Note: Pretty much every class in `src/starmade` was copied over from the main repo.

# Building from source

1. Clone the Repo (duh)
    1. run `https://github.com/garretreichenbach/New-StarMade-Launcher.git`, or
    2. Clone `https://github.com/garretreichenbach/New-StarMade-Launcher` in GitHub Desktop
2. Compile the .jar file
    1. run `gradlew all_jar` (Not `gradlew jar`!), or
    2. run `gradlew win_jar`, `gradlew mac_jar`, or `gradlew linux_jar` for a specific platform

# Note

This launcher requires at least Java 11 to work.
You can download Java 11 here: https://adoptium.net/temurin/releases/?version=11
