# R-tree Spatial Queries

A Java implementation of an R\*-tree spatial index for efficient querying of OpenStreetMap (OSM) data, with support for range queries, k-nearest neighbor searches, and skyline queries.

## Prerequisites

- Java 8 or higher
- An OpenStreetMap (.osm) XML file

## Setup

1. **Place your OSM file**: Download an `.osm` file from [OpenStreetMap](https://www.openstreetmap.org/) and place it in the project root directory with the name `malta.osm` (or modify the code to use your filename).

2. **Compile the project**:
   ```bash
   cd src
   javac -cp . **/*.java
   ```

## Data Pipeline

The application follows a three-stage pipeline:

```
OSM File (.osm) → DataFile (datafile.dat) → IndexFile (indexfile.dat)
```

### Stage 1: OSM Parsing

- **Input**: OpenStreetMap XML file containing spatial data
- **Processors**:
  - `DataFileManagerWithName`: Extracts named locations only
  - `DataFileManagerNoName`: Extracts all coordinate points
- **Output**: List of `Record` objects with coordinates and metadata

### Stage 2: DataFile Creation

- **Process**: `helper.CreateDataFile()`
- **Function**: Organizes records into 32KB blocks for efficient storage
- **Output**: `datafile.dat` - Binary file containing serialized spatial records
- **Location**: Generated in `src/` directory

### Stage 3: IndexFile Creation

- **Process**: R\*-tree construction via `RStarTree` or `BulkLoadingRStarTree`
- **Function**: Builds spatial index structure pointing to datafile blocks
- **Output**: `indexfile.dat` - Binary file containing R\*-tree nodes
- **Location**: Generated in `src/` directory

## Architecture Types

### 1. Standard R\*-tree (Incremental Loading)

- **Class**: `RStarTree`
- **Method**: Records inserted one-by-one with dynamic tree restructuring
- **Data Manager**: `DataFileManagerNoName` (named locations only)
- **Characteristics**:
  - Smaller dataset (~9K records for Malta)
  - Dynamic insertion with overflow treatment
  - Supports insertions after initial construction

### 2. Bulk Loading R\*-tree

- **Class**: `BulkLoadingRStarTree`
- **Method**: Sorts all records and builds tree bottom-up
- **Data Manager**: `DataFileManagerNoName` (all coordinate points)
- **Characteristics**:
  - Larger dataset (~729K records for Malta)
  - More efficient initial construction
  - Better space utilization

## Query Types

### Range Query (`BoundingBoxRangeQuery`)

Finds all points within a rectangular bounding box.

### K-Nearest Neighbors (`NearestNeighboursQuery`)

Finds the k closest points to a given coordinate.

### Skyline Query (`SkylineQuery`)

Identifies non-dominated points within a bounding box region.

## Test Classes

### Core Tests (in `src/Tests/`)

- **`RangeQuery2D`**: Benchmarks R-tree vs sequential scan for range queries
- **`RangeQuery2DBulkLoad`**: Same benchmark using bulk-loaded R-tree
- **`KNN`**: K-nearest neighbor query testing (standard R-tree)
- **`KNNBulk`**: K-nearest neighbor query testing (bulk-loaded R-tree)
- **`SkyLine`**: Skyline query demonstration
- **`RecordDeletion`/`RecordDeletionBulk`**: Record deletion operations

### Output Generation (in `src/OutputQueries/`)

- **`Run2DQueries`**: Interactive query execution with CSV output
- **`Run2DQueriesBulk`**: Same functionality with bulk-loaded tree

## Running Examples

### Benchmark R-tree vs Sequential Scan

```bash
cd src
java Tests.RangeQuery2D
```

Output shows performance comparison between indexed and sequential searches.

### Interactive Queries

```bash
cd src
java OutputQueries.Run2DQueries
```

Provides interactive interface for running different query types.

### Bulk Loading Performance

```bash
cd src
java Tests.RangeQuery2DBulkLoad
```

Demonstrates bulk-loaded R-tree performance characteristics.

## File Structure

```
src/
├── main/java/spatialtree/     # Core R-tree implementation
│   ├── RStarTree.java         # Standard R*-tree
│   ├── BulkLoadingRStarTree.java # Bulk loading R*-tree
│   ├── helper.java            # File I/O and utilities
│   └── DataFileManager*.java  # OSM parsing
├── queries/                   # Query implementations
│   ├── BoundingBoxRangeQuery.java
│   ├── NearestNeighboursQuery.java
│   └── SkylineQuery.java
├── SequentialQueries/         # Brute-force comparison
├── Tests/                     # Test and benchmark classes
└── OutputQueries/             # Interactive query tools
```

## Performance Notes

- **Standard R-tree**: Better for smaller, named-location datasets
- **Block size**: Fixed at 32KB for optimal I/O performance
- **Sequential vs Indexed**: R-tree provides significant speedup for selective queries

## Generated Files

When you run the applications, the following files are generated in the `src/` directory:

- `datafile.dat`: Binary file containing spatial records organized in 32KB blocks
- `indexfile.dat`: Binary file containing the R\*-tree index structure (must be specifies in the source code)
- `output2DRangeQuery.csv`: CSV output from range query results (when enabled)
- `output2DRangeQueryBulkLoaded.csv`: CSV output from bulk-loaded range queries

## Troubleshooting

### "Block size read was not of 32768bytes" Error

This indicates a corrupted or incomplete index file. Solutions:

1. Delete `indexfile.dat` and run the program again to rebuild
2. Ensure the OSM file exists and is accessible
3. Set `reconstruct = true` in test classes to force rebuilding

### "FileNotFoundException: malta.osm"

Place your OSM file in the correct directory (project root) or modify the file path in the source code.

### Different Record Counts

- `DataFileManagerNoName` extracts only named locations (~9K for Malta)
- `DataFileManagerNoName` extracts all coordinates (~729K for Malta)
- Choose the appropriate manager based on your use case
