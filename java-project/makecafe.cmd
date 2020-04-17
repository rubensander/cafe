:start
javac -parameters -g -d . ./*.java
if %ERRORLEVEL% == 0 (
    jar cfve cafe.jar cafe/Game cafe/
    REM java -jar cafe.jar
)
:end
