package main.java.spatialtree;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

/**
 * This class is responsible for loading spatial data from an OSM XML file.
 */
public class DataFileManager {

    /**
     * Loads spatial data from an OSM file using StAX for efficient memory usage.
     * @param filePath The path to the OSM file.
     * @return a list of SpatialDataEntry objects parsed from the file.
     */
    public static List<SpatialDataEntry> loadDataFromFile(String filePath) {
        List<SpatialDataEntry> entries = new ArrayList<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();

        try {
            XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(filePath));

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && "node".equals(reader.getLocalName())) {
                    long id = Long.parseLong(reader.getAttributeValue(null, "id"));
                    double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));
                    double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));
                    ArrayList<Double> coordinates = new ArrayList<>();
                    coordinates.add(lat);
                    coordinates.add(lon);

                    String name = "";  // Initialize name as empty
                    boolean hasName = false;

                    // Move to the next element within the node
                    while (reader.hasNext() && !hasName) {
                        event = reader.next();
                        if (event == XMLStreamConstants.START_ELEMENT && "tag".equals(reader.getLocalName())) {
                            String key = reader.getAttributeValue(null, "k");
                            if ("name".equals(key)) {
                                name = reader.getAttributeValue(null, "v");
                                hasName = true;  // Found the name, can stop processing inner loop
                            }
                        } else if (event == XMLStreamConstants.END_ELEMENT && "node".equals(reader.getLocalName())) {
                            break;  // If end of the node element, stop the inner loop
                        }
                    }

                    // Only add entries with a non-empty name
                    if (!name.isEmpty()) {
                        SpatialDataEntry entry = new SpatialDataEntry(id, name, coordinates);
                        entries.add(entry);
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }

        if (!entries.isEmpty()) {
            System.out.println("Number of entries: " + entries.size());
        }
        return entries;
    }
}
