import com.sun.nio.sctp.*;
import java.util.HashMap;
import java.util.Map;

public class Main {

    Node node;
    Map<Integer, SctpChannel> idToChannelMap = new HashMap<>();
    ChandyLamport snapshot;

    public Main(){
        this.node = new Node();
        node.parse_configuration_file();
        node.initiateVectorClock();
        node.repr();
        node.state = NodeState.ACTIVE;
    }

    public static void main(String[] args) {

        Main m = new Main();

        System.out.println(m.node.clock);
        System.out.println(m.node.nodeId);

        m.snapshot = new ChandyLamport(m);

        m.initiateServerThread(m);
        
        try {
            System.out.println("Sleeping for 5 seconds to allow other nodes wake other nodes...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        m.initiateClientThread(m);

        m.initateChandyLamportSnapshot(m);
    }

    public void initiateClientThread(Main m) {
        System.out.println("Intiating sender(client) thread...");
        Thread sender = new Thread() {
            public void run() {
                try {
                    Client s1 = new Client(m);
                    s1.sendApplicationMessageLogic();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };
        sender.start();
        System.out.println("Sender(client) initiated");
    }

    private void initiateServerThread(Main m) {
        System.out.println("Intiating listener(server) thread...");
        Thread listener = new Thread() {
            public void run() {
                Server listenerObject = new Server(m);
                try {
                    listenerObject.listen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        listener.start();
        System.out.println("Listener(server) initiated");
    }

    private void initateChandyLamportSnapshot(Main m){
        System.out.println("Initiating snapshot thread...");
        Thread snapshot = new Thread() {
            public void run() {
                try {
                    if (m.node.nodeId == 0){
                        Thread.sleep(10000);
                        m.snapshot.initiateSpanning();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        snapshot.start();
        System.out.println("Chandy Lamport protocol initiated");
    }
}
