@echo off
REM Builds the AGNA .msi (and .exe) installer on Windows (PowerShell or cmd).
REM Requires JDK 17+; the first run downloads the WiX toolset automatically.
setlocal
call mvn -q -pl agna-desktop -am package -DskipTests || goto :err
if not exist installers-out mkdir installers-out
REM Stage a clean input folder: the jar plus exactly the 2.2.1 sample set (never the whole target folder or samples)
if exist installers-out\pkg rmdir /s /q installers-out\pkg
mkdir installers-out\pkg\samples
copy agna-desktop\target\agna-2.2.2.jar installers-out\pkg\ >nul || goto :err
for /f "usebackq delims=" %%F in ("packaging\samples-manifest.txt") do copy "samples\%%F" "installers-out\pkg\samples\" >nul || goto :err
jpackage --type msi --name AGNA --app-version 2.2.2 --vendor "Marius Ion Benta" ^
  --description "AGNA - Applied Graph and Network Analysis Open Source" ^
  --icon packaging\Agna.ico --input installers-out\pkg ^
  --main-jar agna-2.2.2.jar --main-class com.bentza.sna.Agna --dest installers-out || goto :err
echo Installer written to installers-out
popd
exit /b 0
:err
popd
exit /b 1
