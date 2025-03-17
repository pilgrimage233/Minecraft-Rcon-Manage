@echo off
setlocal enabledelayedexpansion

rem Application JAR file name
set AppName=endless-manager.jar

rem JVM parameters
set JVM_OPTS=-Dname=%AppName% -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC

:menu
cls
echo.
echo  [1] Start %AppName%
echo  [2] Stop %AppName%
echo  [3] Restart %AppName%
echo  [4] Check status of %AppName%
echo  [5] Exit
echo.

set /p ID=Please select an option: 
if "%ID%"=="1" goto start
if "%ID%"=="2" goto stop
if "%ID%"=="3" goto restart
if "%ID%"=="4" goto status
if "%ID%"=="5" exit /b
goto menu

:start
call :check_status
if defined pid (
    echo %AppName% is already running with PID !pid!
    pause
    goto menu
)

start javaw %JVM_OPTS% -jar %AppName%
echo Starting %AppName%...
echo %AppName% started successfully.
pause
goto menu

:stop
call :check_status
if not defined pid (
    echo %AppName% is not running.
) else (
    echo Preparing to stop %AppName% with PID !pid!
    taskkill /f /pid !pid!
    echo %AppName% stopped successfully.
)
pause
goto menu

:restart
echo Restarting %AppName%...
call :stop
timeout /t 2 /nobreak >nul
call :start
goto menu

:status
call :check_status
if not defined pid (
    echo %AppName% is not running.
) else (
    echo %AppName% is running with PID !pid!
)
pause
goto menu

:check_status
set pid=
for /f "tokens=1,2" %%a in ('jps -l ^| findstr %AppName%') do (
    set pid=%%a
    set image_name=%%b
)
goto :eof

endlocal
