@echo off
title Library Management System
echo ==========================================
echo       LIBRARY MANAGEMENT SYSTEM
echo ==========================================
echo.
where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven was not found. Please check Maven PATH.
    pause
    exit /b 1
)
mvn clean compile exec:java
echo.
pause
