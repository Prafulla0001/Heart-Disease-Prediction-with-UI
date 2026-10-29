@echo off
setlocal
title Health Prediction Python Setup

where python >nul 2>nul
if errorlevel 1 (
    where py >nul 2>nul
    if errorlevel 1 (
        echo Python 3 is required but was not found on PATH.
        echo Install Python 3 and enable the "Add Python to PATH" option.
        exit /b 1
    )
    py -3 -m venv --upgrade .venv
) else (
    python -m venv --upgrade .venv
)

if errorlevel 1 (
    echo Failed to create or upgrade the .venv environment.
    exit /b 1
)

call .venv\Scripts\python.exe -m pip install -r requirements.txt
if errorlevel 1 (
    echo Failed to install Python dependencies.
    exit /b 1
)

echo Python environment is ready.
exit /b 0
