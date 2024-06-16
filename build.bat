@echo off

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Java is not installed or not set in the PATH variable.
    echo Please install Java Development Kit (JDK) and set the PATH variable.
    exit /b 1
)

REM Clean previous build results
rmdir /s /q out

REM Create output directory
mkdir out

REM Compile all Java files within src directory
javac -d out -sourcepath src src\com\project\Main.java src\com\project\network\*.java src\com\project\utils\*.java

REM Check if Java files were compiled successfully
if errorlevel 1 (
    echo Error compiling Java files.
    exit /b 1
)

REM Run the program
java -cp out com.project.Main %*
