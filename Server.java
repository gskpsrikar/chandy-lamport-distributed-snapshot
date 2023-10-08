import com.sun.nio.sctp.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class Server {
    private int port;
    private int MAX_MSG_SIZE = 4096;
    private Main m;

    public Server(Main m) {
        this.port = Integer.parseInt(m.node.listenPort);
        this.m = m;
    }

    public void listen() throws Exception {
        InetSocketAddress address = new InetSocketAddress(port);
        SctpServerChannel ssc = SctpServerChannel.open();

        ssc.bind(address);

        while(true) { // ssc.isOpen() is equivalent to 'true' in this case
            SctpChannel sc = ssc.accept();
            System.out.println("Client Connected");

            Thread listener = new Thread() {
                public void run(){
                    ByteBuffer buf = ByteBuffer.allocateDirect(MAX_MSG_SIZE);
                    while (sc.isOpen()){
                        try {
                            sc.receive(buf, null, null);
                            Message msg = Message.fromByteBuffer(buf);

                            handleMessage(msg);

                            System.out.println("[Received message text] : " + msg.message);
                            
                            wakeNodeIfPassive(msg);

                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        };
                    }
                }
            };
            listener.start();
        }
    }

    public void wakeNodeIfPassive(Message msg) {
        // TODO: The node wakes up based on message length. Make this more reliable.
        if (m.node.state.equals("passive")){
            if (msg.message.length() > 0) {
                m.node.flipState();
                System.out.println("Node changed from active to passive state");
            }
        }
    }

    public void handleMessage(Message msg){

        if (msg.messageType == MessageType.APPLICATION){
            handleApplicationMessage(msg);
        };

        if (msg.messageType == MessageType.MARKER){
            handleMarkerMessage(msg);
        }
    }

    public void handleApplicationMessage(Message msg) {
        // This method updates the vector clock on receiving an application message
        synchronized (m) {
            for (int i=0; i<m.node.numberOfNodes; i++){
                int value = Math.max(m.node.clock.get(i), msg.clock.get(i));
                m.node.clock.set(i, value);
            }
            System.out.println("Vector clock on receveing: "+ m.node.clock);
        }
    }

    public void handleMarkerMessage(Message msg) {
        // TODO: Do operations that needs to be done on receiving a messsage.
    }
}
