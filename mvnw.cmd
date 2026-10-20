@echo off
setlocal
set "DETECTED_MVN=C:\Users\glaks\.gemini\antigravity-ide\scratch\tools\apache-maven-3.9.6\bin\mvn.cmd"
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn %*
) else (
    if exist "%DETECTED_MVN%" (
        call "%DETECTED_MVN%" %*
    ) else (
        echo [ERROR] Maven was not found in PATH or at %DETECTED_MVN%.
        echo Please ensure Apache Maven is installed and added to your system PATH.
        exit /b 1
    )
)
