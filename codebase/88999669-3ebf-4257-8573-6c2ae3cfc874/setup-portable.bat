@echo off
echo Setting up Portable Java 17 and Maven for TestVWA...

set PROJECT_DIR=%~dp0
set TOOLS_DIR=%PROJECT_DIR%tools
set JAVA_DIR=%TOOLS_DIR%\java
set MAVEN_DIR=%TOOLS_DIR%\maven

echo Creating tools directory...
if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"

echo.
echo Downloading Java 17 (OpenJDK)...
powershell -Command "& {Invoke-WebRequest -Uri 'https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_windows-x64_bin.zip' -OutFile '%TOOLS_DIR%\openjdk-17.zip'}"

echo.
echo Downloading Maven...
powershell -Command "& {Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.zip' -OutFile '%TOOLS_DIR%\maven.zip'}"

echo.
echo Extracting Java...
powershell -Command "& {Expand-Archive -Path '%TOOLS_DIR%\openjdk-17.zip' -DestinationPath '%TOOLS_DIR%' -Force}"
for /d %%i in ("%TOOLS_DIR%\jdk-*") do move "%%i" "%JAVA_DIR%"

echo.
echo Extracting Maven...
powershell -Command "& {Expand-Archive -Path '%TOOLS_DIR%\maven.zip' -DestinationPath '%TOOLS_DIR%' -Force}"
for /d %%i in ("%TOOLS_DIR%\apache-maven-*") do move "%%i" "%MAVEN_DIR%"

echo.
echo Cleaning up...
del "%TOOLS_DIR%\openjdk-17.zip"
del "%TOOLS_DIR%\maven.zip"

echo.
echo Setting up environment...
set JAVA_HOME=%JAVA_DIR%
set MAVEN_HOME=%MAVEN_DIR%
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo.
echo Testing installation...
"%JAVA_HOME%\bin\java" -version
"%MAVEN_HOME%\bin\mvn" -version

echo.
echo Setup complete! 
echo.
echo To use in this session:
echo set JAVA_HOME=%JAVA_DIR%
echo set MAVEN_HOME=%MAVEN_DIR%
echo set PATH=%JAVA_DIR%\bin;%MAVEN_DIR%\bin;%%PATH%%
echo.
echo Then run: mvn clean compile spring-boot:run
echo.
pause
