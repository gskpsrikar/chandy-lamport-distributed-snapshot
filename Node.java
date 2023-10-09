import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum NodeState {PASSIVE, ACTIVE};

public class Node {

    String NETID = "sxs210570";

    public String currentNodeName;
    public int nodeId;
    public String listenPort = "1234";
    public List<List<String>> neighbors = new ArrayList<>();
    
    int numberOfNodes;
    int minPerActive;
    int maxPerActive;
    int minSendDelay;
    int snapshotDelay;
    int maxNumber;

    int messagesSent = 0;
    int messagesReveived = 0;

    NodeState state;

    public Vector<Integer> clock = new Vector<>();

    Map<String, Integer> hostToIdMap = new HashMap<>();
    Map<Integer, String> idToHostMap = new HashMap<>();
    
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
        System.out.println("Current node is " + currentNodeName);
        System.out.println("Current node number is " + nodeId);
        
        int n = neighbors.size();
        String neighborString = "";
        for (int i=0; i < n; i++) {
            String neighborName = neighbors.get(i).get(0);
            String listenPort = neighbors.get(i).get(1);
            neighborString +=  neighborName + "(" + listenPort + ")";
            neighborString += ", ";
        }
        System.out.println("The neighboring nodes are: " + neighborString);
    }

    static String getHostName() {
        String localhost = "";
        try {
            localhost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            System.out.println(e);
        }
        return localhost;
    };

    public void parse_configuration_file () {

        String CONFIG_FILENAME = "distributed-systems-project-1/config.txt";
        // CONFIG_FILENAME = "config.txt";

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

                    numberOfNodes = n;
                    
                    this.minPerActive = Integer.parseInt(globalMatcher.group(2));
                    this.maxPerActive = Integer.parseInt(globalMatcher.group(3));
                    this.minSendDelay = Integer.parseInt(globalMatcher.group(4));
                    this.snapshotDelay = Integer.parseInt(globalMatcher.group(5));
                    this.maxNumber = Integer.parseInt(globalMatcher.group(6));

                    validLineNumber += 1;

                } else if (validLineNumber <= n) {
                    String[] nodeDetails = line.split(" ");
                    // System.out.println(this.currentNodeName + " " + nodeDetails[1] + " " + this.currentNodeName.equals(nodeDetails[1]));
                    if (this.currentNodeName.equals(nodeDetails[1]+".utdallas.edu")){
                        this.nodeId = Integer.parseInt(nodeDetails[0]);
                    }
                    // (hostname, listenport)
                    List<String> value = new ArrayList<>(); 
                    value.add(nodeDetails[1]+".utdallas.edu"); // This need to be changed incase of local systems
                    value.add(nodeDetails[2]);
                    
                    idToHostMap.put(Integer.parseInt(nodeDetails[0]), nodeDetails[1]+".utdallas.edu");
                    hostToIdMap.put(nodeDetails[1]+".utdallas.edu", Integer.parseInt(nodeDetails[0]));

                    dictionary.put(Integer.parseInt(nodeDetails[0]), value);

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

    public void repr() {
        System.out.println("------------------------ Node Details -------------------------");
        this.details();
        System.out.println("---------------------------- Globals --------------------------");
        System.out.println(String.format("perActive message limit (inclusive) : [%d, %d]", minPerActive, maxPerActive));
        System.out.println("minSendDelay: " + minSendDelay);
        System.out.println("snapshotDelay: " + snapshotDelay);
        System.out.println("maxNumber: " + maxNumber);
        System.out.println("---------------------------------------------------------------");
    }

    public void flipState() {
        if (this.state == NodeState.ACTIVE) {
            this.state = NodeState.PASSIVE;
            System.out.println("[STATE CHANGE] Node changed from ACTIVE TO PASSIVE");
        }
        else {
            this.state = NodeState.ACTIVE;
            System.out.println("[STATE CHANGE] Node changed from PASSIVE TO ACTIVE");
        }
    }

    public void initiateVectorClock() {
        for (int i=0; i < numberOfNodes; i++){
            this.clock.add(0);
        }
    }
}
