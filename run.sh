#!/bin/sh
# Compile and run the launcher for Mac/Linux

# Navigate to the project directory
SCRIPT_DIR=$(dirname "$0")
cd "$SCRIPT_DIR" || exit

# Check operating system
# shellcheck disable=SC2039
case "$OSTYPE" in
"linux-gnu"*)
  echo "Detecting GNU/Linux"
  is_mac=false
  ;;
"darwin"*)
  echo "Detecting macOS."
  is_mac=true
  ;;
*)
  echo "Unknown operating system detected."
  echo "If you are on Windows, you should use run.bat instead."
  ;;
esac

# Compile the Gradle project
echo "Compiling StarMade launcher..."
if $is_mac; then
  ./gradlew mac_jar
else
  ./gradlew linux_jar
fi


echo "Running StarMade launcher..."
if $is_mac; then
  java -jar release-builds/StarMade\ Launcher-darwin-x64/starmade-launcher.jar
else
  java -jar release-builds/StarMade\ Launcher-linux-x64/starmade-launcher.jar
fi

# Run the launcher jar file


