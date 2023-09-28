import com.sun.nio.sctp.*;

import java.io.IOException;
import java.util.List;

public class Main {

    Node node;

    public Main(){
        this.node = new Node();
        node.parse_configuration_file(); node.repr();
        node.state = "active";
    }

    public static void main(String[] args) {

        Main m = new Main();
        // m.node.send_messages_to_neighbors();

        m.initiateListener();
        
        try {
            System.out.println("Sleeping for 5 seconds to allow other nodes wake other nodes...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        m.initiateSender(m);
    }

    public void initiateSender(Main m) {
        System.out.println("Intiating sender(client) thread...");
        Thread sender = new Thread() {
            public void run() {
                Sender s1 = new Sender(m);
                try {
                    s1.sendLogic();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };
        sender.start();
        System.out.println("Sender(client) initiated");
    }

    private void initiateListener() {
        System.out.println("Intiating listener(server) thread...");
        Thread listener = new Thread() {
            public void run() {
                Listener listenerObject = new Listener(Integer.parseInt(node.listenPort));
                try {
                    listenerObject.listen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };
        listener.start();
        System.out.println("Listener(server) initiated");
    }
}
