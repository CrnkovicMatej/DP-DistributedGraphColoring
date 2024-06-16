### CSV File Structure for Network Topology

Two CSV files are used to define the network topology: `nodes.csv` and `links.csv`. For multiple network examples, additional files can be named accordingly, such as `nodes5.csv` and `links5.csv`.

#### nodes.csv

- **File Purpose**: Defines the nodes (vertices) of the network.
- **Format**:
  - Column 1: `id` (Header)
    - Description: Unique identifier for each node.
    - Data Type: Integer
    - Example: 
      ```
      id
      0
      1
      2
      ```
  - Each subsequent row represents a node identifier.

#### links.csv

- **File Purpose**: Specifies the connections (edges) between nodes.
- **Format**:
  - Column 1: `source` (Header)
    - Description: Identifier of the source node.
    - Data Type: Integer
  - Column 2: `destination` (Header)
    - Description: Identifier of the destination node.
    - Data Type: Integer
  - Example:
    ```
    source,destination
    0,1
    1,2
    2,0
    ```
  - Each row represents a directed link from the `source` node to the `destination` node.

#### Notes:

- Ensure that `nodes.csv` contains all node identifiers referenced in `links.csv`.
- For multiple network examples, additional CSV files can be named to reflect different network configurations (e.g., `nodes5.csv`, `links5.csv`, etc.).
- The CSV files should be UTF-8 encoded for compatibility.
- Empty lines and extra spaces should be avoided to ensure correct parsing.
- **Choice of CSV**: The decision to use CSV files instead of a more convenient format like YAML was deliberate. We opted to avoid additional Java packages for parsing, relying only on standard Java capabilities.
  
These CSV files provide a structured format for defining the nodes and links in a network topology, facilitating easy input and validation within the network simulation program.
