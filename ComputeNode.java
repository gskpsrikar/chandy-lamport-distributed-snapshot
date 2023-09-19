import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

public class ComputeNode {

    Node node;
    int minPerActive;
    int maxPerActive;
    int minSendDelay;
    int snapshotDelay;
    int maxNumber;
    int messagesSent = 0;
    String NETID = "sxs210570";

    public ComputeNode(){
        this.node = new Node();
        node.parse_configuration_file();
        this.parse_configuration_file();
    }

    public static void main(String[] args) {
        ComputeNode computeNode = new ComputeNode(); computeNode.repr();

        send_messages_to_neighbors(computeNode);
    }

    public static void send_messages_to_neighbors(ComputeNode c) {
        Random random = new Random();

        int number_of_messages_to_send = random.nextInt(c.maxPerActive - c.minPerActive + 1) + c.minPerActive;

        for (int i=0; i < number_of_messages_to_send; i++){

            if (c.messagesSent == c.maxNumber) {
                // The node should go to passive state
            }

            List<String> neighbor = c.node.get_random_neighbor();

            String neighbor_name = neighbor.get(0);
            int port = Integer.parseInt(neighbor.get(1));

            try {
                Thread.sleep(c.minSendDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            c.messagesSent += 1;

            send_message(c.NETID, neighbor_name, port);
        }
    }
    
    public static void send_message(String netid, String destinationNode, int port) {
        // TODO: Open a socket connection and send a message to a neighbor

        destinationNode = String.format("%s@%s.utdallas.edu", netid, destinationNode );
        System.out.println(destinationNode + " " + port);
    }

    public void parse_configuration_file () {
        // Wrap this function and never touch it. This is working fine.

        String CONFIG_FILENAME = "config.txt";
        Pattern GLOBAL_VARIABLES_REGEX_PATTERN = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");


        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILENAME))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.split("#")[0].trim(); // Remove everything after '#'
                if (line.isEmpty()) { // Skip empty lines or lines with only comment
                    continue; 
                }

                Matcher globalMatcher = GLOBAL_VARIABLES_REGEX_PATTERN.matcher(line);

                if (globalMatcher.matches()) {
                    this.minPerActive = Integer.parseInt(globalMatcher.group(2));
                    this.maxPerActive = Integer.parseInt(globalMatcher.group(3));
                    this.minSendDelay = Integer.parseInt(globalMatcher.group(4));
                    this.snapshotDelay = Integer.parseInt(globalMatcher.group(5));
                    this.maxNumber = Integer.parseInt(globalMatcher.group(6));
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void repr() {
        System.out.println("------------------------ Node Details -------------------------");
        this.node.details();
        System.out.println("---------------------------- Globals --------------------------");
        System.out.println(String.format("perActive message limit (inclusive) : [%d, %d]", minPerActive, maxPerActive));
        System.out.println("minSendDelay: " + minSendDelay);
        System.out.println("snapshotDelay: " + snapshotDelay);
        System.out.println("maxNumber: " + maxNumber);
        System.out.println("---------------------------------------------------------------");
    }
}
