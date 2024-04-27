package main.java.spatialtree;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class helper {

    protected static final String PATH_TO_DATAFILE = "datafileFIE.dat";
    protected static final String PATH_TO_INDEXFILE = "indexfile.dat";
    protected static final int BLOCK_SIZE = 32 * 1024; // Each Block is 32KB
    static int dataDimensions; // The data's used dimensions
    protected static int totalBlocksInDatafile;  // The total blocks written in the datafile
    protected static int totalBlocksInIndexFile; // The total blocks written in the indexfile
    protected static int totalLevelsOfTreeIndex; // The total levels of the rStar tree

    static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    // Used to deserializable a byte array to a serializable Object
    static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

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
     * Converts a SpatialDataEntry to a Record object.
     * @param entry The SpatialDataEntry to convert.
     * @return A new Record based on the entry data.
     */


    /**
     * Initializes or resets the data file and writes spatial data entries into it.
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

    public static ArrayList<Record> readDataFile(int blockId){
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(PATH_TO_DATAFILE), "r");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            //go to the expected block
            raf.seek(blockId*BLOCK_SIZE);
            byte[] block = new byte[BLOCK_SIZE];
            bis.read(block,0,BLOCK_SIZE);
            byte[] LengthInBytes = serialize( Integer.parseInt("1")); // producing the integer size
            System.arraycopy(block, 0, LengthInBytes, 0, LengthInBytes.length);

            byte[] recordsInBlock = new byte[(Integer)deserialize(LengthInBytes)];
            System.arraycopy(block, LengthInBytes.length, recordsInBlock, 0, recordsInBlock.length);

            return (ArrayList<Record>)deserialize(recordsInBlock);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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

    // Updates the indexFile block with the corresponding given already saved Node
    // In case node's block id is the root's and the given parameter totalLevelsOfTreeIndex is changed during the tree's changes then
    // the totalLevelsOfTreeIndex variable's value is increased by one
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
    public static void RecordSorterX(List<Record> records){
        Collections.sort(records, new Comparator<Record>() {
            @Override
            public int compare(Record r1, Record r2) {
                return Double.compare(r1.getCoordinate(0), r2.getCoordinate(0));
            }
        });
    }

    public static void main(String[] args) {
        // Example usage
        ArrayList<Record> records = new ArrayList<>();
        records.add(new Record(1, "Record A", new ArrayList<Double>(List.of(3.0, 4.0))));
        records.add(new Record(2, "Record B", new ArrayList<Double>(List.of(1.0, 2.0))));
        records.add(new Record(3, "Record C", new ArrayList<Double>(List.of(2.0, 3.0))));

        System.out.println("Before sorting:");
        records.forEach(System.out::println);

        RecordSorterX(records);

        System.out.println("After sorting:");
        records.forEach(System.out::println);
    }
}
