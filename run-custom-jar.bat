@echo off
rem Run the custom jar file for Unix Windows

rem Navigate to the project directory
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR% || exit /b 1

rem Run the custom jar file
echo Running StarMade launcher...
.\jre18\bin\java.exe ^
    --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED ^
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED ^
    --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED ^
    --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED ^
    --add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED ^
    --add-opens=java.base/sun.nio.ch=ALL-UNNAMED ^
    --add-opens=java.base/java.lang=ALL-UNNAMED ^
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
    --add-opens=java.base/java.io=ALL-UNNAMED ^
    --add-opens=java.base/java.util=ALL-UNNAMED ^
    -jar StarMade.jar ^
    -Xms1024m -Xmx8192m -force