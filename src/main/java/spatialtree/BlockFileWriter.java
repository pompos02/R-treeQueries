package main.java.spatialtree;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockFileWriter {



    //  testing if the BlockFileWriter works as expected
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        List<SpatialDataEntry> entries = DataFileManager.loadDataFromFile("cyprus-latest.osm");
        helper.CreateDataFile(entries,2,true);
        ArrayList<Integer> metaData = helper.readMetaData(helper.PATH_TO_DATAFILE);

        // Check if metadata is not null and print it
        if (metaData != null) {
            System.out.println("Metadata from " + helper.PATH_TO_DATAFILE + ":");
            for (Integer data : metaData) {
                System.out.println(data); // 1st data dimensions 2nd block size 3rd total blocks
            }
        } else {
            System.out.println("Failed to read metadata or metadata is empty.");
        }
        // Read and print each block's records
        int totalBlocks = metaData.get(2)-1; // total blocks
        int NumberOfRecords = 0;
        for (int blockId = 1; blockId <= totalBlocks; blockId++) {
            ArrayList<Record> records = helper.readDataFile(blockId);
            if (records != null && !records.isEmpty()) {

                System.out.println("Records from Block " + blockId + ":");
                for (Record record : records) {
                    System.out.println(record.toString());
                    NumberOfRecords=NumberOfRecords+1;
                }
            } else {
                System.out.println("No records found in Block " + blockId + ".");
            }
        }
        System.out.println("Number of records: " + NumberOfRecords);
    }


}
