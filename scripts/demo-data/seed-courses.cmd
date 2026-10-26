@echo off
setlocal EnableExtensions

if /I not "%~1"=="APPLY" (
    echo Dry run: no database changes made.
    echo Run scripts\demo-data\seed-courses.cmd APPLY to seed the local Docker Compose databases.
    exit /b 0
)

set "COMPOSE=%~dp0..\..\docker\compose.yml"
for /f "usebackq delims=" %%i in (`docker compose -f "%COMPOSE%" ps -q postgres`) do set "CONTAINER=%%i"
if not defined CONTAINER (
    echo The postgres service from docker\compose.yml must be running.
    exit /b 1
)

for /f "usebackq delims=" %%i in (`docker inspect --format "{{.Name}}" %CONTAINER%`) do set "CONTAINER_NAME=%%i"
if not "%CONTAINER_NAME%"=="/cyberlearnix-postgres" (
    echo Refusing to seed unexpected database container "%CONTAINER_NAME%".
    exit /b 1
)

for /f "usebackq delims=" %%i in (`docker exec %CONTAINER% printenv POSTGRES_USER`) do set "DB_USER=%%i"
if not defined DB_USER (
    echo POSTGRES_USER is not configured in the local database container.
    exit /b 1
)

call :Apply lms_user_db seed-course-users.sql
if errorlevel 1 exit /b 1
call :Apply lms_instructor_db seed-course-instructors.sql
if errorlevel 1 exit /b 1
call :Apply lms_course_db seed-course-catalog.sql
if errorlevel 1 exit /b 1

echo Seed completed. SQL verification queries are documented in README.md.
exit /b 0

:Apply
set "DATABASE=%~1"
set "SEED_FILE=%~2"
set "CONTAINER_FILE=/tmp/%~2"
docker cp "%~dp0%SEED_FILE%" "%CONTAINER%:%CONTAINER_FILE%"
if errorlevel 1 exit /b 1
echo Applying %SEED_FILE% to %DATABASE%
docker exec %CONTAINER% psql -X -v ON_ERROR_STOP=1 -U %DB_USER% -d %DATABASE% -f %CONTAINER_FILE%
set "SEED_EXIT=%ERRORLEVEL%"
docker exec %CONTAINER% rm -f %CONTAINER_FILE% >nul
exit /b %SEED_EXIT%