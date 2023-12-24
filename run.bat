@echo off
rem Compile and run the launcher for Windows

rem Navigate to the project directory
set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR% || exit /b 1

rem Compile the Gradle project
echo Compiling StarMade launcher...
call .\gradlew.bat Updater:jar
call .\gradlew.bat win_jar

rem Run the launcher jar file
echo Running StarMade launcher...
java -jar release-builds\StarMade Launcher-win32-ia32\starmade-launcher.jar