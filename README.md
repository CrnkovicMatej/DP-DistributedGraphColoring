# Distributed Graph Algorithms for Graph Coloring and Maximum Independent Set

This repository contains implementations of distributed algorithms for graph coloring and computing the maximum independent set in a network modeled as a connected but incomplete graph. Processes are represented as nodes, communication channels as edges, and some nodes may not have direct edges between them, although there exists a path between any two nodes.

The algorithms are based on the principles outlined in [Distributed algorithms for Message-Passing Systems](https://www.springer.com/gp/book/9783642381225) by M. Raynal, Springer, Berlin Heidelberg, 2013 (ISBN-13: 978-3642381225).

## Prerequisites

- Java Development Kit (JDK) installed and configured in your PATH environment variable.

## Task Description

The task involves describing, implementing, and testing distributed algorithms for efficient graph coloring and computing the maximum independent set in the described network topology. These algorithms are distributed in the sense that each process (node) collaborates with its neighbors without having complete knowledge of the entire graph structure.

## Algorithms Implemented

- **TO BE IMPLEMENTED**

## Implementation Details

The implementations are written in Java, leveraging standard Java libraries without additional frameworks. Each algorithm is designed to operate efficiently in a distributed environment where nodes communicate locally with their neighbors to achieve the desired coloring or independent set computation.

## Directory Structure

- `src/`: Contains the Java source code for the implementations.
  - `com/project/Main.java`: Entry point for running and testing the algorithms.
  - `com/project/network/`: Classes representing nodes and communication in the network.
  - `com/project/utils/`: Utility classes for parsing input and supporting algorithms.
  
- `data/`: Directory to store input CSV files (`nodes.csv`, `links.csv`, etc.) defining various network topologies.

## Usage

### Compilation and Execution

#### On Linux and MacOS

```bash
# Compile the Java source files
./build.sh ./data/NetworkExamples/nodes1.csv ./data/NetworkExamples/links1.csv

# Run the compiled Java program
java -cp out com.project.Main ./data/NetworkExamples/nodes1.csv ./data/NetworkExamples/links1.csv
```

#### On Windows

```powershell
REM Compile the Java source files
build.bat ./data/NetworkExamples/nodes1.csv ./data/NetworkExamples/links1.csv

REM Run the compiled Java program
java -cp out com.project.Main ./data/NetworkExamples/nodes1.csv ./data/NetworkExamples/links1.csv
```

## References

- [Raynal M. Distributed algorithms for Message-Passing Systems. Springer, Berlin Heidelberg, 2013.](https://www.springer.com/gp/book/9783642381225)