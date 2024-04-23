package main.java.spatialtree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class helper {

    protected static final String PATH_TO_DATAFILE = "datafile.dat";
    protected static final String PATH_TO_INDEXFILE = "indexfile.dat";
    protected static final int BLOCK_SIZE = 32 * 1024; // Each Block is 32KB
    protected static int dataDimensions; // The data's used dimensions
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
    private static Record convertEntryToRecord(SpatialDataEntry entry) {
        // Assuming Record constructor or factory method takes similar parameters
        return new Record(entry.getId(), entry.getName(), entry.getCoordinates());
    }

    /**
     * Initializes or resets the data file and writes spatial data entries into it.
     * @param entries List of SpatialDataEntry objects to write into the file.
     * @param dataDimensions The number of dimensions each data entry uses.
     * @param makeNewDataFile Flag to determine if a new file should be created.
     */
    static void CreateDataFile(List<SpatialDataEntry> entries, int dataDimensions, boolean makeNewDataFile) {
        try {
            if (makeNewDataFile) {
                Files.deleteIfExists(Paths.get(PATH_TO_DATAFILE)); // Resetting/Deleting dataFile data
            }
            helper.dataDimensions=dataDimensions;
            if (dataDimensions <= 0)
                throw new IllegalStateException("The number of data dimensions must be a positive integer");


            updateMetaData(PATH_TO_DATAFILE);

            ArrayList<Record> blockRecords = new ArrayList<>();
            int currentBlockSize=0;
            for (SpatialDataEntry entry : entries) {
                byte [] recordBytes = serialize(convertEntryToRecord(entry));
                if (currentBlockSize + recordBytes.length > BLOCK_SIZE) {
                    writeDataFileBlock(blockRecords);
                    blockRecords = new ArrayList<>();
                    currentBlockSize = 0;
                }
                blockRecords.add(convertEntryToRecord(entry));
                currentBlockSize+=recordBytes.length;
            }


        } catch (Exception e) {
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

    static ArrayList<Record> readDataFile(int blockId){
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
}
