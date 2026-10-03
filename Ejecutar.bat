@echo off
REM Lanza la aplicacion Torneo Minecraft sin consola visible.
REM Usa JAVA_HOME si esta definido; si no, el JDK local de este equipo; si no, javaw del PATH.
set "JAR=%~dp0target\torneo-minecraft-1.0.0-ejecutable.jar"

if not exist "%JAR%" (
    echo No se encontro el ejecutable. Compila primero con:  mvn clean package
    pause
    exit /b 1
)

set "JAVAW=javaw"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVAW=%JAVA_HOME%\bin\javaw.exe"
if "%JAVAW%"=="javaw" if exist "%USERPROFILE%\.jdks\openjdk-26.0.2\bin\javaw.exe" set "JAVAW=%USERPROFILE%\.jdks\openjdk-26.0.2\bin\javaw.exe"

start "" "%JAVAW%" --enable-native-access=ALL-UNNAMED -jar "%JAR%"
