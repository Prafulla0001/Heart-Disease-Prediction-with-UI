@echo off
title CardioSaaS Web Server
echo Starting Health & Wellness Web API Server...
java -cp ".;libs/*" HealthWebApp
pause