import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Node {

    public String currentNodeName;
    public String listenPort;
    public List<List<String>> neighbors = new ArrayList<>();

    public Node() {
        this.currentNodeName = getHostName();
    }

    public static void main(String[] args){
        Node node = new Node();
        node.parse_configuration_file();
        node.details();
    }

    public void addNeighbors(List<String> neighbor){
        neighbors.add(neighbor);
    }

    public void details(){
        System.out.println("My name is " + currentNodeName);
        
        int n = neighbors.size();
        String neighborString = "";
        for (int i=0; i < n; i++) {
            String neighborName = neighbors.get(i).get(0);
            String listenPort = neighbors.get(i).get(1);
            neighborString +=  neighborName + "(" + listenPort + ")";
            neighborString += ", ";
        }
        System.out.println("My neighboring nodes are: " + neighborString);
    }

    static String getHostName() {
        // Get the name of the current host
        String localhost = "";
        try {
            localhost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            System.out.println(e);
        }
        return localhost;
    };

    public void parse_configuration_file () {

        String CONFIG_FILENAME = "config.txt";
        Pattern GLOBAL_VARIABLES_REGEX_PATTERN = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");

        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILENAME))) {
            String line;
            int n = -1; // Number of nodes

            Map<Integer, List<String>> dictionary = new HashMap<>();

            int validLineNumber = 0;

            while ((line = reader.readLine()) != null) {
                line = line.split("#")[0].trim(); // Remove everything after '#'
                if (line.isEmpty()) { // Skip empty lines or lines with only comment
                    continue; 
                }

                Matcher globalMatcher = GLOBAL_VARIABLES_REGEX_PATTERN.matcher(line);

                if (globalMatcher.matches()) {// If the line is about global variables, ignore it
                    n = Integer.parseInt(globalMatcher.group(1));
                    validLineNumber += 1;

                } else if (validLineNumber <= n) {
                    String[] nodeDetails = line.split(" ");

                    int nodeId = Integer.parseInt(nodeDetails[0]);

                    // (hostname, listenport)
                    List<String> value = new ArrayList<>(); value.add(nodeDetails[1]); value.add(nodeDetails[2]);
                    dictionary.put(nodeId, value);
                    validLineNumber += 1;

                } else {
                    int node_num = validLineNumber - n - 1;

                    List<String> node_details = dictionary.get(node_num);
                    String node_name = node_details.get(0);

                    if (node_name.equals(this.currentNodeName)){
                        this.listenPort = node_details.get(1);

                        String[] neighborNodes = line.split(" ");
                        
                        for (int i=0; i < neighborNodes.length; i++){
                            
                            int p = Integer.parseInt(neighborNodes[i]);
                            this.addNeighbors(dictionary.get(p));
                        
                        }
                    }
                    validLineNumber += 1;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
