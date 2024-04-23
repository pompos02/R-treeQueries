package main.java.spatialtree;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
     static int calculateMaxRecordsInBlock() {
        ArrayList<Record> blockRecords = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            ArrayList<Double> coordinateForEachDimension = new ArrayList<>();
            for (int d = 0; d < dataDimensions; d++)
                coordinateForEachDimension.add(0.0);
            Record record = new Record(0, "test",coordinateForEachDimension);
            blockRecords.add(record);
            byte[] recordInBytes = new byte[0];
            byte[] recordLength = new byte[0];
            try {
                recordInBytes = serialize(blockRecords);
                recordLength = serialize(recordInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (recordLength.length + recordInBytes.length > BLOCK_SIZE)
                break;
        }
        System.out.println("The Max records In each blocks are: "+ i);
        return i;
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
}
