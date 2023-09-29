import java.util.List;
import java.util.Random;

public class Helper {
    public static void main(String args[]){
        System.out.println("this is a class to store deprecated and/or static code");
    }

    public static void send_messages_to_neighbors(Node node) {
        Random random = new Random();

        int number_of_messages_to_send = random.nextInt(node.maxPerActive - node.minPerActive + 1) + node.minPerActive;

        for (int i=0; i < number_of_messages_to_send; i++){

            if (node.messagesSent == node.maxNumber) {
                // The node should go to passive state
            }

            List<String> neighbor = get_random_neighbor(node);

            String neighbor_name = neighbor.get(0);
            int port = Integer.parseInt(neighbor.get(1));

            try {
                Thread.sleep(node.minSendDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            node.messagesSent += 1;
            send_message(node.NETID, neighbor_name, port);
        }
    }

    public static void send_message(String netid, String destinationNode, int port) {
        // TODO: Open a socket connection and send a message to a neighbor
        
        String message = String.format(
            "[DUMMY]: Sending message to %s@%s.utdallas.edu on port %d", netid, destinationNode, port
        );
        System.out.println(message);
    }

    public static List<String> get_random_neighbor(Node node) {
        Random random = new Random();
        
        int randomNumber = random.nextInt(node.neighbors.size());

        List<String> chosen_neighbor = node.neighbors.get(randomNumber);

        return chosen_neighbor;
    }


}
