import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComputeNode {

    Node node;
    int minPerActive;
    int maxPerActive;
    int minSendDelay;
    int snapshotDelay;
    int maxNumber;

    public ComputeNode(){
        this.node = new Node();
        node.parse_configuration_file();

        this.parse_configuration_file();
    }

    public static void main(String[] args) {
        ComputeNode computeNode = new ComputeNode();
        computeNode.repr();
    }
    
    public void parse_configuration_file () {

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
        System.out.println("minPerActive: " + minPerActive);
        System.out.println("maxPerActive: " + maxPerActive);
        System.out.println("minSendDelay: " + minSendDelay);
        System.out.println("snapshotDelay: " + snapshotDelay);
        System.out.println("maxNumber: " + maxNumber);
        System.out.println("---------------------------------------------------------------");
    }
}
