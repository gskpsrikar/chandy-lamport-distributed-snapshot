public class ComputeNode {

    public ComputeNode(){
    }

    public static void main(String[] args) {
        Node node = new Node(); 
        node.parse_configuration_file();
        node.repr();

        node.send_messages_to_neighbors();
    }
}
