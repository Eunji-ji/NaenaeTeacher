@echo off
setlocal

if "%GRADLE_VERSION%"=="" set "GRADLE_VERSION=8.10.2"
set "BASE_DIR=%~dp0"
set "GRADLE_USER_HOME=%BASE_DIR%.gradle"
set "DIST_DIR=%BASE_DIR%.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin"
set "GRADLE_HOME=%DIST_DIR%\gradle-%GRADLE_VERSION%"
set "ZIP_FILE=%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip"

if exist "%BASE_DIR%gradle.properties" (
    for /f "usebackq tokens=1,* delims==" %%A in ("%BASE_DIR%gradle.properties") do (
        if "%%A"=="org.gradle.java.home" set "PROJECT_JAVA_HOME=%%B"
    )
)

if not "%PROJECT_JAVA_HOME%"=="" (
    set "JAVA_HOME=%PROJECT_JAVA_HOME%"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
    if not exist "%ZIP_FILE%" (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP_FILE%'"
        if errorlevel 1 exit /b %errorlevel%
    )
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP_FILE%' -DestinationPath '%DIST_DIR%' -Force"
    if errorlevel 1 exit /b %errorlevel%
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
