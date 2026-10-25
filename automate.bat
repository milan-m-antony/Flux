@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0automate.ps1" %*
endlocal
