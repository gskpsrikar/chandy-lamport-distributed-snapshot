import com.sun.nio.sctp.*;

import java.io.IOException;
import java.util.List;

public class Main {

    Node node;

    public Main(){
        this.node = new Node();
        node.parse_configuration_file(); node.repr();
    }

    public static void main(String[] args) {

        Main m = new Main();
        
        m.node.send_messages_to_neighbors();
        m.initiateListener();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        m.initiateSender(m);
    }

    public void initiateSender(Main m) {

        Thread sender = new Thread() {
            public void run() {
                Sender s1 = new Sender(m);
                try {
                    s1.sendLogic();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        };
        sender.start();
    }

    private void initiateListener() {
        Thread listener = new Thread() {
            public void run() {
                Listener listenerObject = new Listener(Integer.parseInt(node.listenPort));
                try {
                    listenerObject.listen();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            };
        };
        listener.start();
    }

}
