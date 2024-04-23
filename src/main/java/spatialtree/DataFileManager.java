package main.java.spatialtree;
import java.io.File;
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

/**
 * This class is responsible for loading spatial data from an OSM XML file.
 */
public class DataFileManager {

    /**
     * Loads spatial data from an OSM file.
     * @param filePath The path to the OSM file.
     * @return a list of SpatialDataEntry objects parsed from the file.
     */
    public static List<SpatialDataEntry> loadDataFromFile(String filePath) {
        List<SpatialDataEntry> entries = new ArrayList<>();
        File xmlFile = new File(filePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Get all nodes tagged as "node" (which are points in OSM)
            NodeList nodeList = doc.getElementsByTagName("node");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element node = (Element) nodeList.item(i);
                long id = Long.parseLong(node.getAttribute("id"));
                double lat = Double.parseDouble(node.getAttribute("lat"));
                double lon = Double.parseDouble(node.getAttribute("lon"));
                double[] coordinates = new double[]{lat, lon}; // Store lat and lon as coordinates array
                ArrayList<Double> coordinateList = new ArrayList<Double>();
                for (double coord : coordinates) {
                    coordinateList.add(coord);  // Autoboxing converts double to Double
                }
                String name = ""; // Initialize name as empty, it might be filled by tag "name"

                // Check for child nodes that may have additional data like names
                NodeList tags = node.getElementsByTagName("tag");
                for (int j = 0; j < tags.getLength(); j++) {
                    Element tag = (Element) tags.item(j);
                    if ("name".equals(tag.getAttribute("k"))) {
                        name = tag.getAttribute("v");
                        break;
                    }
                }

                // Only add entries with a non-empty name
                if (!name.isEmpty()) {
                    // Create a new SpatialDataEntry and add it to the list
                    SpatialDataEntry entry = new SpatialDataEntry(id, name, coordinateList);
                    entries.add(entry);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        if (!entries.isEmpty()) {
            System.out.println( "Number of entries: "+entries.size());
        }
        return entries;
    }
}
