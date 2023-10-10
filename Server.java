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
            // System.out.println("Client Connected");

            Thread listener = new Thread() {
                public void run(){
                    ByteBuffer buf = ByteBuffer.allocateDirect(MAX_MSG_SIZE);
                    while (sc.isOpen()){
                        try {
                            sc.receive(buf, null, null);
                            Message msg = Message.fromByteBuffer(buf);

                            handleMessage(msg);

                            // System.out.println("[Received message text] : " + msg.message);

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

    public void handleMessage(Message msg) throws Exception{

        // Message Handler

        if (msg.messageType == MessageType.APPLICATION){
            handleApplicationMessage(msg);
        };

        // Receiving a MARKER message
        if (msg.messageType == MessageType.MARKER){
            synchronized (m){
                System.out.println("[MARKER : received] Received MARKER message from NODE: "+msg.senderId);
                this.m.snapshot.receiveMarkerMessageFromParent(msg);
            }
        };

        // Response to MARKER sent to a RED process
        if (msg.messageType == MessageType.MARKER_REJECTION){
            System.out.println("[MARKER_REJECTION : received] Received MARKER_REJECTION message from "+msg.senderId);
            this.m.snapshot.receiveMarkerRejectionMessage(msg);
        };

        // Received from children in the collapse process
        if (msg.messageType == MessageType.MARKER_REPLY){
                synchronized (m){
                System.out.println("[MARKER_REPLY : received] Received MARKER_REPLY message from "+msg.senderId);
                this.m.snapshot.receiveMarkerRepliesFromChildren(msg);
            }
        };

        if (msg.messageType == MessageType.END_SNAPSHOT){
            System.out.println("[END_SNAPSHOT : received] Received MARKER_REPLY message from "+msg.senderId);
            this.m.snapshot.receiveSnapshotResetMessage(msg);
        }
    }

    public void handleApplicationMessage(Message msg) {
        // This method updates the vector clock on receiving an application message
        synchronized (m) {
            wakeNodeIfPassive(msg);
            for (int i=0; i<m.node.numberOfNodes; i++){
                int value = Math.max(m.node.clock.get(i), msg.clock.get(i));
                m.node.clock.set(i, value);
            }
            m.node.messagesReveived += 1;
            // System.out.println("Vector clock on receiving= "+ m.node.clock+" | Messages received = "+m.node.messagesReveived);
        }
    }

    public void wakeNodeIfPassive(Message msg) {
        // Wakes up a PASSIVE node on receiving an application message
        if (m.node.state == NodeState.PASSIVE){
            if (m.node.messagesSent < m.node.maxNumber){
                m.node.flipState();
            }
        }
    }
}
