#!/bin/sh
# Run the custom jar file for Unix

# Navigate to the project directory
SCRIPT_DIR=$(dirname "$0")
cd "$SCRIPT_DIR" || exit

# Check operating system
# shellcheck disable=SC2039
case "$OSTYPE" in
"linux-gnu"*)
  echo "Detecting GNU/Linux."
  ;;
"darwin"*)
  echo "Detecting macOS."
  is_mac=true
  ;;
*)
  echo "Unknown operating system detected."
  echo "If you are on Windows, you should use run-custom-jar.bat instead."
  ;;
esac

# Run the custom jar file
echo "Running custom StarMade jar..."

if $is_mac; then
  # OpenGL needs to run on main thread on macOS
  ./jre18/Contents/Home/bin/java \
    --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED \
    --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
    --add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED \
    --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens=java.base/java.io=ALL-UNNAMED \
    --add-opens=java.base/java.util=ALL-UNNAMED \
    -XstartOnFirstThread -jar StarMade.jar \
    -Xms1024m -Xmx8192m -force
else
  # Don't need OpenGL arg for Linux
  ./jre18/bin/java \
    --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED \
    --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
    --add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED \
    --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens=java.base/java.io=ALL-UNNAMED \
    --add-opens=java.base/java.util=ALL-UNNAMED \
    -jar StarMade.jar \
    -Xms1024m -Xmx8192m -force
fi
