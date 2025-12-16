@echo off
setlocal
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"
set "CP=lib\sqlite-jdbc-3.51.1.0.jar"
if not exist out mkdir out
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop'; $cp='%CP%'; $files = Get-ChildItem -Path 'src/main/java' -Recurse -Filter *.java | %% { $_.FullName }; javac -cp $cp -d out $files; if ($LASTEXITCODE -ne 0) { exit 1 }; javaw -cp \"out;$cp\" com.gcl.conge.Main gui"
if exist logo.jpg (
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop'; java -cp 'out;%CP%' com.gcl.conge.ui.IconGenerator | Out-Null"
)
endlocal
