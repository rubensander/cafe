:start
javac -parameters -g -d . ./*.java -cp ".\java-json.jar;."
if %ERRORLEVEL% == 0 (
    jar cfve cafe.jar cafe/Game cafe/ java-json.jar
    java -classpath .\java-json.jar -jar cafe.jar
)
:end
