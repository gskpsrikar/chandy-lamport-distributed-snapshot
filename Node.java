import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Node {

    String NETID = "sxs210570";

    public String currentNodeName;
    public String listenPort;
    public List<List<String>> neighbors = new ArrayList<>();
    
    int minPerActive;
    int maxPerActive;
    int minSendDelay;
    int snapshotDelay;
    int maxNumber;

    int messagesSent;
    
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
        System.out.println("Current node's hostname is " + currentNodeName);
        
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
                    
                    this.minPerActive = Integer.parseInt(globalMatcher.group(2));
                    this.maxPerActive = Integer.parseInt(globalMatcher.group(3));
                    this.minSendDelay = Integer.parseInt(globalMatcher.group(4));
                    this.snapshotDelay = Integer.parseInt(globalMatcher.group(5));
                    this.maxNumber = Integer.parseInt(globalMatcher.group(6));

                    validLineNumber += 1;

                } else if (validLineNumber <= n) {
                    String[] nodeDetails = line.split(" ");

                    int nodeId = Integer.parseInt(nodeDetails[0]);

                    // (hostname, listenport)
                    List<String> value = new ArrayList<>(); 
                    value.add(nodeDetails[1]+".utdallas.edu"); // This need to be changed incase of local systems
                    value.add(nodeDetails[2]);

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

    public List<String> get_random_neighbor() {
        Random random = new Random();
        
        int randomNumber = random.nextInt(this.neighbors.size());

        List<String> chosen_neighbor = this.neighbors.get(randomNumber);

        return chosen_neighbor;
    }

    public void send_messages_to_neighbors() {
        Random random = new Random();

        int number_of_messages_to_send = random.nextInt(this.maxPerActive - this.minPerActive + 1) + this.minPerActive;

        for (int i=0; i < number_of_messages_to_send; i++){

            if (this.messagesSent == this.maxNumber) {
                // The node should go to passive state
            }

            List<String> neighbor = this.get_random_neighbor();

            String neighbor_name = neighbor.get(0);
            int port = Integer.parseInt(neighbor.get(1));

            try {
                Thread.sleep(this.minSendDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.messagesSent += 1;
            send_message(this.NETID, neighbor_name, port);
        }
    }

    public static void send_message(String netid, String destinationNode, int port) {
        // TODO: Open a socket connection and send a message to a neighbor
        String message = String.format(
            "[DUMMY]: Sending message to %s@%s.utdallas.edu on port %d", netid, destinationNode, port
        );
        System.out.println(message);
    }
}
