# Spatial Access R*-Tree

Spatial Access is a Java implementation of the R*-Tree spatial index. It provides tools to parse OpenStreetMap (`.osm`) files into a binary data format, construct an R*-Tree index, and execute spatial queries such as range search, k-nearest neighbours, and skyline queries. The project also includes sequential-scan implementations for benchmarking and an interactive command line interface.

## Features

- **Persistent R*-Tree** stored in `indexfile.dat` and `datafile.dat` using custom binary blocks.
- **Bounding Box Range Query**, **K-NN Query**, and **Skyline Query** implementations.
- **Bulk Loading** support for efficient tree construction from large datasets.
- **Sequential Scan Queries** to compare against indexed performance.
- **CLI Program** (`Program`) for interactive querying and CSV export.
- **Example tests and benchmarks** under `src/Tests` and `src/OutputQueries`.

## Architecture Overview

```
src/
 ├── main/java/spatialtree/    # Core R*-Tree implementation and utilities
 ├── queries/                  # Query classes executed on the tree
 ├── SequentialQueries/        # Non-indexed query variants
 ├── OutputQueries/            # Benchmarking utilities
 ├── Tests/                    # Example programs/tests
 └── Program.java              # Interactive command line interface
```

Key components:

- **RStarTree** – main tree structure supporting insert, delete, and query operations.
- **BulkLoadingRStarTree** – builds the tree directly from a list of records.
- **helper** – handles persistence, file metadata and block management.
- **DataFileManagerWithName / DataFileManagerNoName** – parse `.osm` files via StAX into `Record` objects.
- **Node/Entry/LeafEntry** – building blocks of the tree stored in index blocks.

Queries are implemented in the `queries` package, while sequential-scan versions are placed in `SequentialQueries`.

## Installation

1. **Prerequisites** – JDK 8 or later and the standard `javac`/`java` tools.
2. **Clone the repository** and ensure the sample data file (`map.osm`) is present in the root directory.
3. **Compile the source**:

```bash
$ mkdir -p out
$ javac $(find src -name '*.java') -d out
```

This produces class files under `out/`.

## Usage

### Interactive CLI

Run the main `Program` class. It initializes the tree (creating `datafile.dat` and `indexfile.dat` if necessary) and prompts for query selection:

```bash
$ java -cp out Program
```

The menu offers:

1. Range search with user-provided bounds
2. K-NN search
3. Skyline query
0. Exit

Results can optionally be exported to CSV files (e.g., `output2DRangeQuery.csv`).

### Benchmark and Bulk Examples

- `OutputQueries/Run2DQueries` – constructs a tree and repeatedly issues queries while logging performance to CSV.
- `OutputQueries/Run2DQueriesBulk` – performs the same using the bulk-loading implementation.
- Programs in `src/Tests` demonstrate deletion and additional query scenarios.

To run any of these, compile as shown above and execute the desired class using `java -cp out <ClassName>`.

## Configuration

Paths and block size are defined in `helper`:

```java
protected static final String PATH_TO_DATAFILE = "datafile.dat";
protected static final String PATH_TO_INDEXFILE = "indexfile.dat";
protected static final int BLOCK_SIZE = 32 * 1024; // bytes
```

Modify these constants if you wish to store files elsewhere or adjust block size. The tree dimensionality is inferred from the data at creation time.

## Testing

There are no automated unit tests, but the project can be built and sample programs executed to verify functionality. A typical compilation run prints warnings only:

```
$ javac $(find src -name '*.java') -d out
Note: src/main/java/spatialtree/helper.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
```

Running `Program` produces the query menu as confirmation that the tree was successfully created.

## Deployment

The project is distributed as source. After compilation, the generated class files can be packaged into a JAR if desired. No CI/CD configuration is provided.

## API Documentation

Important methods of `RStarTree`:

- `getDataInBoundingBox(BoundingBox)` – returns `LeafEntry` objects overlapping the given box.
- `getNearestNeighbours(List<Double> searchPoint, int k)` – returns the k closest entries to a point.
- `getSkyline(BoundingBox)` – returns non-dominated entries within a region.
- `deleteRecord(Entry)` – removes a record from the tree.

`BulkLoadingRStarTree` exposes similar query methods and builds the tree using `bulkLoadTree`.

Each query class in the `queries` package provides a `getQueryRecords` method executed on a `Node`.

## Contributing

1. Fork the repository and create a new branch for your feature or fix.
2. Ensure the project builds with `javac` and that example programs still run.
3. Submit a pull request describing your changes.

Please follow the existing code style (spaces for indentation, descriptive comments) and keep the source compilable with Java 8+.

## License

No explicit license file is present. All rights reserved by the original authors.

## Credits

- OpenStreetMap data (sample `map.osm`) used for demonstration.
- Java Standard Library and StAX API for XML parsing.
