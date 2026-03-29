@echo off
powershell -NoProfile -Command "$files = Get-ChildItem -Path src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName; javac -encoding UTF-8 -cp \"lib/*\" -d bin $files"
