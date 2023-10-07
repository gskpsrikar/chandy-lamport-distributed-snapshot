import javax.sound.midi.Soundbank;

public class Main {

    Node node;

    public Main(){
        this.node = new Node();
        node.parse_configuration_file();
        node.repr();
        node.state = "active";
    }

    public static void main(String[] args) {

        Main m = new Main();

        System.out.println(m.node.clock);
        System.out.println(m.node.nodeId);

        // m.initiateListener(m);
        
        try {
            System.out.println("Sleeping for 5 seconds to allow other nodes wake other nodes...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // m.initiateSender(m);
    }

    public void initiateSender(Main m) {
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

    private void initiateListener(Main m) {
        System.out.println("Intiating listener(server) thread...");
        Thread listener = new Thread() {
            public void run() {
                Server listenerObject = new Server(m);
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
