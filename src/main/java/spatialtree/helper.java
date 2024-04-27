package main.java.spatialtree;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
/**
 * Utility class for handling operations related to the R*-Tree,
 * focusing on file management, serialization, and other helper functions.
 */
public class helper {

    protected static final String PATH_TO_DATAFILE = "datafile.dat";
    protected static final String PATH_TO_INDEXFILE = "indexfile.dat";
    protected static final int BLOCK_SIZE = 32 * 1024; // Each Block is 32KB


    // Static variables for dimensions of data, and the total number of blocks and levels in data and index files.
    static int dataDimensions;
    protected static int totalBlocksInDatafile;
    protected static int totalBlocksInIndexFile;
    protected static int totalLevelsOfTreeIndex;

    /**
     * Serializes an object to a byte array.
     * @param obj The object to serialize.
     * @return The byte array representation of the object.
     * @throws IOException if an I/O error occurs during serialization.
     */
    static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    /**
     * Deserializes a byte array back to an object.
     * @param data The byte array to deserialize.
     * @return The deserialized object.
     * @throws IOException if an I/O error occurs during deserialization.
     * @throws ClassNotFoundException if the class of the serialized object cannot be found.
     */
    static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    /**
     * Updates metadata in a file based on the path specified.
     * Metadata includes dimensions, block size, total blocks, and tree levels.
     * @param pathToFile Decides what Metadata will change (Index or Data).
     */
    static void updateMetaData(String pathToFile) {
        try {
            ArrayList<Integer> dataFileMetaData = new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(BLOCK_SIZE);
            if (pathToFile.equals(PATH_TO_DATAFILE))
                dataFileMetaData.add(++totalBlocksInDatafile);
            else if (pathToFile.equals(PATH_TO_INDEXFILE))
            {
                dataFileMetaData.add(++totalBlocksInIndexFile);
                dataFileMetaData.add(totalLevelsOfTreeIndex);
            }
            byte[] metaDataInBytes = serialize(dataFileMetaData);
            byte[] metadataLength = serialize(metaDataInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(metadataLength, 0, block, 0, metadataLength.length);
            System.arraycopy(metaDataInBytes, 0, block, metadataLength.length, metaDataInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(pathToFile), "rw");
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int getTotalBlocksInIndexFile() {
        return totalBlocksInIndexFile;
    }
    public static void setTotalBlocksInIndexFile(int newIndex){totalBlocksInIndexFile=newIndex;}
    public static int getDataDimensions() {
        return dataDimensions;
    }
    public static int getTotalLevelsOfTreeIndex() {
        return totalLevelsOfTreeIndex;
    }

    /**
     * Reads and returns the metadata from the specified file path.
     * @param pathToFile The file path from which metadata is read (Always on Datafile.dat).
     * @return An ArrayList of Integers containing the metadata.
     */
    static ArrayList<Integer> readMetaData(String pathToFile){
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(pathToFile), "rw");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] block = new byte[BLOCK_SIZE];
            if (bis.read(block,0,BLOCK_SIZE) != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");

            byte[] goodPutLengthInBytes = serialize(new Random().nextInt()); // Serializing an integer ir order to get the size of goodPutLength in bytes
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            byte[] dataInBlock = new byte[(Integer)deserialize(goodPutLengthInBytes)];
            System.arraycopy(block, goodPutLengthInBytes.length, dataInBlock, 0, dataInBlock.length);

            return (ArrayList<Integer>)deserialize(dataInBlock);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Initializes or resets the data file and writes spatial data entries into it if needed.
     * @param records List of SpatialDataEntry objects to write into the file.
     * @param dataDimensions The number of dimensions each data entry uses.
     * @param makeNewDataFile Flag to determine if a new file should be created.
     */
    public static void CreateDataFile(List<Record> records, int dataDimensions, boolean makeNewDataFile) {
        try {
            if (!makeNewDataFile && Files.exists(Paths.get(PATH_TO_DATAFILE)))
            {
                ArrayList<Integer> dataFileMetaData = readMetaData(PATH_TO_DATAFILE);
                if (dataFileMetaData == null)
                    throw new IllegalStateException("Could not read datafile's Meta Data Block properly");
                helper.dataDimensions = dataFileMetaData.get(0);
                if (helper.dataDimensions  <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                if (dataFileMetaData.get(1) != BLOCK_SIZE)
                    throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
                totalBlocksInDatafile = dataFileMetaData.get(2);
                if (totalBlocksInDatafile  < 0)
                    throw new IllegalStateException("The total blocks of the datafile cannot be a negative number");
            }else{
                Files.deleteIfExists(Paths.get(PATH_TO_DATAFILE));
                helper.dataDimensions=dataDimensions;
                if (dataDimensions <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");


                updateMetaData(PATH_TO_DATAFILE);
                ArrayList<Record> blockRecords = new ArrayList<>();
                int currentBlockSize=0;
                for (Record record : records) {
                    byte [] recordBytes = serialize(record);
                    if (currentBlockSize + recordBytes.length > BLOCK_SIZE) {
                        writeDataFileBlock(blockRecords);
                        blockRecords = new ArrayList<>();
                        currentBlockSize = 0;
                    }

                    blockRecords.add(record);
                    currentBlockSize+=recordBytes.length;
                }
                writeDataFileBlock(blockRecords); // fill the leftovers
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates or updates the index file based on the specified parameters.
     * If the file exists and no new file creation is requested, it reads and updates based on existing data.
     * Otherwise, it creates a new file or resets an existing file based on the input data dimensions.
     *
     * @param dataDimensions The dimensionality of the data to be handled in the index file.
     * @param makeNewDataFile A boolean flag indicating whether a new index file should be created.
     */
    public static void CreateIndexFile(int dataDimensions, boolean makeNewDataFile) throws IOException {
        try {
            if (!makeNewDataFile && Files.exists(Paths.get(PATH_TO_INDEXFILE)))
            {
                ArrayList<Integer> indexFileMetaData = readMetaData(PATH_TO_INDEXFILE);
                if (indexFileMetaData == null)
                    throw new IllegalStateException("Could not read datafile's Meta Data Block properly");
                helper.dataDimensions = indexFileMetaData.get(0);
                if (helper.dataDimensions  <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                if (indexFileMetaData.get(1) != BLOCK_SIZE)
                    throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
                totalBlocksInIndexFile = indexFileMetaData.get(2);
                if (totalBlocksInIndexFile  < 0)
                    throw new IllegalStateException("The total blocks of the index file cannot be a negative number");
                totalLevelsOfTreeIndex = indexFileMetaData.get(3);
                if (totalLevelsOfTreeIndex  < 0)
                    throw new IllegalStateException("The total index's tree levels cannot be a negative number");
            }else{
                Files.deleteIfExists(Paths.get(PATH_TO_INDEXFILE)); // Resetting/Deleting dataFile data
                helper.dataDimensions = dataDimensions;
                if (dataDimensions <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                Files.deleteIfExists(Paths.get(PATH_TO_INDEXFILE)); // Resetting/Deleting index file data
                helper.dataDimensions = dataDimensions;
                totalLevelsOfTreeIndex = 1; // increasing the size from the root, the root (top level) will always have the highest level
                if (helper.dataDimensions <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                updateMetaData(PATH_TO_INDEXFILE);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Writes a list of Record objects to the data file in blocks of specified BLOCK_SIZE.
     * Ensures that the data written does not exceed the block size and handles block management.
     *
     * @param blockRecords The list of Record objects to be written.
     */
    public static void writeDataFileBlock(ArrayList<Record> blockRecords) {
        try {
            byte[] recordInBytes = serialize(blockRecords);
            byte[] LengthInBytes = serialize(recordInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(LengthInBytes, 0, block, 0, LengthInBytes.length);
            System.arraycopy(recordInBytes, 0, block, LengthInBytes.length, recordInBytes.length);

            FileOutputStream fos = new FileOutputStream(PATH_TO_DATAFILE,true);
            BufferedOutputStream bout = new BufferedOutputStream(fos);
            bout.write(block);
            updateMetaData(PATH_TO_DATAFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a specific block from the data file and deserializes it into a list of Record objects.
     *
     * @param blockId The block ID to read from the data file.
     * @return A list of Record objects read from the specified block.
     */

    public static ArrayList<Record> readDataFile(int blockId) {
        // Declare the path to the data file
        String dataFilePath = PATH_TO_DATAFILE;
        byte[] block = new byte[BLOCK_SIZE];

        try (RandomAccessFile raf = new RandomAccessFile(new File(dataFilePath), "r");
             FileInputStream fis = new FileInputStream(raf.getFD());
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            // Go to the expected block
            raf.seek(blockId * BLOCK_SIZE);
            if (bis.read(block, 0, BLOCK_SIZE) != BLOCK_SIZE) {
                throw new IOException("Could not read the complete block");
            }

            // Process the block to extract records
            byte[] lengthInBytes = serialize(Integer.parseInt("1"));  // Example size, likely needs adjustment
            System.arraycopy(block, 0, lengthInBytes, 0, lengthInBytes.length);

            byte[] recordsInBlock = new byte[(Integer) deserialize(lengthInBytes)];
            System.arraycopy(block, lengthInBytes.length, recordsInBlock, 0, recordsInBlock.length);

            return (ArrayList<Record>) deserialize(recordsInBlock);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Estimates the maximum number of entries that can be stored in a node without exceeding the block size.
     * This method is useful for determining the capacity of nodes within the R*-tree structure.
     *
     * @return The maximum number of entries per node.
     */
    static int calculateMaxEntriesInNode() {
        ArrayList<Entry> entries = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
            for (int d = 0; d < dataDimensions; d++)
                boundsForEachDimension.add(new Bounds(0.0, 0.0));
            Entry entry = new LeafEntry(new Random().nextLong(),new Random().nextInt(), boundsForEachDimension);
            entry.setChildNodeBlockId(new Random().nextLong());
            entries.add(entry);
            byte[] nodeInBytes = new byte[0];
            byte[] goodPutBytes = new byte[0];
            try {
                nodeInBytes = serialize(new Node(new Random().nextInt(), entries));
                goodPutBytes = serialize(nodeInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (goodPutBytes.length + nodeInBytes.length > BLOCK_SIZE)
                break;
        }
        return i;
    }

    /**
     * Writes a serialized node to the index file.
     *
     * @param node The Node object to write to the index file.
     */

    static void writeNewIndexFileBlock(Node node) {
        try {
            byte[] nodeInBytes = serialize(node);
            byte[] goodPutLengthInBytes = serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            FileOutputStream fos = new FileOutputStream(PATH_TO_INDEXFILE,true);
            BufferedOutputStream bout = new BufferedOutputStream(fos);
            bout.write(block);
            updateMetaData(PATH_TO_INDEXFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Updates the indexFile block with the corresponding given already saved Node
     * In case node's block id is the root's and the given parameter totalLevelsOfTreeIndex
     * is changed during the tree's changes then the totalLevelsOfTreeIndex variable's value is increased by one
     * @param node The node to update in the index file.
     * @param totalLevelsOfTreeIndex The current total levels of the tree, used to check for updates in tree depth.
     */

    static void updateIndexFileBlock(Node node, int totalLevelsOfTreeIndex) {
        try {
            byte[] nodeInBytes = serialize(node);
            byte[] goodPutLengthInBytes = serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");
            f.seek(node.getBlockId()*BLOCK_SIZE); // this basically reads n bytes in the file
            f.write(block);
            f.close();

            if (node.getBlockId() == RStarTree.getRootNodeBlockId() && helper.totalLevelsOfTreeIndex != totalLevelsOfTreeIndex)
                updateLevelsOfTreeInIndexFile();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static int getTotalBlocksInDatafile() {
        return totalBlocksInDatafile;
    }

    /**
     * Updates the levels of the R*-tree stored in the index file's metadata. This is typically called after a structural
     * change in the tree that affects its depth (e.g., a split at the root causing an increase in tree height).
     */

    public static void updateLevelsOfTreeInIndexFile()
    {
        try {
            ArrayList<Integer> dataFileMetaData = new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(BLOCK_SIZE);
            dataFileMetaData.add(totalBlocksInIndexFile);
            dataFileMetaData.add(++totalLevelsOfTreeIndex);
            byte[] metaDataInBytes = serialize(dataFileMetaData);
            byte[] goodPutLengthInBytes = serialize(metaDataInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, goodPutLengthInBytes.length, metaDataInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    /**
     * Reads a specific block from the index file, interpreting it as a Node object.
     *
     * @param blockId The block ID to read, which corresponds to a specific node in the R*-tree.
     * @return The Node object deserialized from the specified block.
     */

    public static Node readIndexFileBlock(long blockId){
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            //seek to a a different section of the file, so discard the previous buffer
            raf.seek(blockId*BLOCK_SIZE);
            //bis = new BufferedInputStream(fis);
            byte[] block = new byte[BLOCK_SIZE];
            if (bis.read(block,0,BLOCK_SIZE) != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + "bytes");


            byte[] goodPutLengthInBytes = serialize(new Random().nextInt()); // Serializing an integer ir order to get the size of goodPutLength in bytes
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            byte[] nodeInBytes = new byte[(Integer)deserialize(goodPutLengthInBytes)];
            System.arraycopy(block, goodPutLengthInBytes.length, nodeInBytes, 0, nodeInBytes.length);

            return (Node)deserialize(nodeInBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Sorts a list of Record objects based on the value of a specified dimension (dimension 0).
     * used in the BulkLoaded implementation
     * @param records The list of records to sort.
     */
    public static void RecordSorterX(List<Record> records){
        Collections.sort(records, new Comparator<Record>() {
            @Override
            public int compare(Record r1, Record r2) {
                return Double.compare(r1.getCoordinate(0), r2.getCoordinate(0));
            }
        });
    }
}
