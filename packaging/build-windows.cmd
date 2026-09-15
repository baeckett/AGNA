@echo off
REM Builds the AGNA .msi (and .exe) installer on Windows (PowerShell or cmd).
REM Requires JDK 17+; the first run downloads the WiX toolset automatically.
setlocal
call mvn -q -pl agna-desktop -am package -DskipTests || goto :err
if not exist installers-out mkdir installers-out
jpackage --type msi --name AGNA --app-version 2.1.3 --vendor "Marius Ion Benta" ^
  --description "AGNA - Applied Graph and Network Analysis Open Source" ^
  --icon packaging\Agna.ico --input agna-desktop\target ^
  --main-jar agna-2.1.3.jar --main-class com.bentza.sna.Agna --dest installers-out || goto :err
echo Installer written to installers-out
popd
exit /b 0
:err
popd
exit /b 1
