# Distributed Graph Algorithms for Graph Coloring and Maximum Independent Set

This repository contains implementations of distributed algorithms for graph coloring and computing the maximum independent set in a network modeled as a connected but incomplete graph. Processes are represented as nodes, communication channels as edges, and some nodes may not have direct edges between them, although there exists a path between any two nodes.

The algorithms are based on the principles outlined in [Distributed algorithms for Message-Passing Systems](https://www.springer.com/gp/book/9783642381225) by M. Raynal, Springer, Berlin Heidelberg, 2013 (ISBN-13: 978-3642381225).

## Prerequisites

- Java Development Kit (JDK) installed and configured in your PATH environment variable.

## Task Description

The task involves describing, implementing, and testing distributed algorithms for efficient graph coloring and computing the maximum independent set in the described network topology. These algorithms are distributed in the sense that each process (node) collaborates with its neighbors without having complete knowledge of the entire graph structure.

## Algorithms Implemented

- **Luby's Maximal Independent Set (MIS) Algorithm:**
  Luby's algorithm computes a maximal independent set in a graph. It leverages randomized techniques to efficiently identify nodes that belong to the independent set without direct global coordination.

- **(Δ + 1)-coloring Algorithm :**
  The coloring algorithm assigns colors to nodes in a graph such that no adjacent nodes share the same color. It uses a greedy approach where each node is assigned a color not used by its neighbors, ensuring that the graph is colored with at most the maximum degree of the graph plus one colors.

- **Maximal Independent Set from m-coloring:**
  This algorithm determines a maximal independent set from a graph's m-coloring. It identifies nodes that are not adjacent to any other nodes of the same color, thereby forming an independent set.

## Examples

  - **Maximal Independent Set (MIS):**
  <div style="display: flex; align-items: center;">
<div class="mermaid" style="flex: 1;">
graph LR;
    1 --- 2;
    1 --- 3;
    2 --- 3;
    3 --- 6;
    4 --- 5;
    5 --- 7;
    5 --- 8;
    6 --- 8;
    7 --- 8;
</div>
<div class="text" style="flex: 1; margin: 0 20px;">
        <p style="font-size: 18px; text-align: center;">one example of MIS is :</p>
    </div>
<div class="mermaid" style="flex: 1;">
graph LR;
    1 --- 2;
    1 --- 3;
    2 --- 3;
    3 --- 6;
    4 --- 5;
    5 --- 7;
    5 --- 8;
    6 --- 8;
    7 --- 8;
    style 1 fill:#f9f,stroke:#333,stroke-width:4px
    style 4 fill:#f9f,stroke:#333,stroke-width:4px
    style 6 fill:#f9f,stroke:#333,stroke-width:4px
</div>
</div>

  - **(Δ + 1)-coloring:**
<div style="display: flex; align-items: center;">
<div class="mermaid" style="flex: 1;">
graph LR;
    1 --- 2;
    1 --- 3;
    2 --- 3;
    3 --- 6;
    4 --- 5;
    5 --- 7;
    5 --- 8;
    6 --- 8;
    7 --- 8;
</div>
<div class="text" style="flex: 1; margin: 0 20px;">
        <p style="font-size: 18px; text-align: center;">can be colored like this so that no two neighbours have the same color :</p>
    </div>
<div class="mermaid" style="flex: 1;">
graph LR;
    1 --- 2;
    1 --- 3;
    2 --- 3;
    3 --- 6;
    4 --- 5;
    5 --- 7;
    5 --- 8;
    6 --- 8;
    7 --- 8;
    style 1 fill:#FF6347
    style 2 fill:#FFFF00
    style 3 fill:#1E90FF
    style 4 fill:#BA55D3
    style 5 fill:#FF6347
    style 6 fill:#FF6347
    style 7 fill:#FFFF00
    style 8 fill:#1E90FF
</div>
</div>



## Implementation Details

The implementations are written in Java, leveraging standard Java libraries without additional frameworks. Each algorithm is designed to operate efficiently in a distributed environment where nodes communicate locally with their neighbors to achieve the desired coloring or independent set computation.

## Directory Structure

- `src/`: Contains the Java source code for the implementations.
  - `com/project/Main.java`: Entry point for running and testing the algorithms.
  - `com/project/network/`: Classes representing nodes and communication in the network.
  - `com/project/network/algorithms`: Classes representing specific distributed algorithms.
  - `com/project/utils/`: Utility classes for parsing input and supporting algorithms and message handling.
  
- `data/`: Directory to store input CSV files (`nodes.csv`, `links.csv`, etc.) defining various network topologies.

## Usage

### Compilation and Execution

#### On Linux and MacOS


1. Open a terminal and clone the repo.

2. Navigate to the directory where your `build.sh` script is located.

3. Run the following command to compile the Java source files and execute the compiled Java program.

```bash
./build.sh path/to/nodes.csv path/to/links.csv algorithm
```

#### On Windows

1. Open a terminal and clone the repo.

2. Navigate to the directory where your `build.bat` script is located.

3. Run the following command to compile the Java source files and execute the compiled Java program.

```powershell
.\build.bat path\to\nodes.csv path\to\links.csv algorithm
```

#### Parameters Explanation

When running the Java program using the provided scripts (`build.sh` for Linux/MacOS and `build.bat` for Windows), you need to specify three arguments:

1. **Nodes File Path (`nodes.csv`):**
   - This argument specifies the path to the CSV file containing information about nodes in the network.

2. **Links File Path (`links.csv`):**
   - This argument specifies the path to the CSV file containing information about links (connections) between nodes in the network.

3. **Algorithm Name:**
   - This argument specifies the name of the algorithm to execute. Depending on your program's design, the algorithm name can be one of the following:
     - `maximal`: Execute the Maximal Independent Set algorithm.
     - `color`: Execute the Graph Coloring algorithm.
     - `colorMis`: Execute a specific variant of the Graph Coloring algorithm that integrates with the Maximal Independent Set.

#### Using provided graph configuration files

In the repository's `data/NetworkExamples` directory, there are several examples of graphs used for testing purposes. If you wish to use one of these graphs, simply refer to the file paths in the following format:

- Nodes File: [`./data/NetworkExamples/nodes.csv`](./data/NetworkExamples/nodes.csv)
- Links File: [`./data/NetworkExamples/links.csv`](./data/NetworkExamples/links.csv)

Replace `nodes.csv` and `links.csv` with the specific file names of the nodes and links data you wish to use.


##### On Linux and MacOS

```bash
./build.sh ./data/NetworkExamples/nodes5.csv ./data/NetworkExamples/links5.csv maximal
```

##### On Windows

```powershell
.\build.bat .\data\NetworkExamples\nodes1.csv .\data\NetworkExamples\links1.csv maximal
```


## References

- [Raynal M. Distributed algorithms for Message-Passing Systems. Springer, Berlin Heidelberg, 2013.](https://www.springer.com/gp/book/9783642381225)