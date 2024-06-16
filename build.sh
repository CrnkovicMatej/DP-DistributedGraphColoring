#!/bin/bash

# Check if Java is installed
java -version >/dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "Java is not installed or not set in the PATH variable."
    echo "Please install Java Development Kit (JDK) and set the PATH variable."
    exit 1
fi

# Clean previous build results
rm -rf out

# Create output directory
mkdir -p out

# Compile all Java files within src directory
javac -d out -sourcepath src src/com/project/Main.java src/com/project/network/*.java src/com/project/utils/*.java

# Check if Java files were compiled successfully
if [ $? -ne 0 ]; then
    echo "Error compiling Java files."
    exit 1
fi

# Run the program
java -cp out com.project.Main "$@"
