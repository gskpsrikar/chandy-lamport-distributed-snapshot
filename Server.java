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

        if (msg.messageType == MessageType.APPLICATION){
            handleApplicationMessage(msg);
        };

        if (msg.messageType == MessageType.MARKER){
            handleMarkerMessage(msg);
        };

        if (msg.messageType == MessageType.MARKER_REPLY){
            handleMarkerReplyMessage(msg);
        }

        if (msg.messageType == MessageType.MARKER_REJECTION){
            handleMarkerRejection(msg);
        }
    }

    public void handleMarkerRejection(Message msg) throws Exception{
        this.m.snapshot.receiveMarkerMessageFromParent(msg);
    }

    public void handleApplicationMessage(Message msg) {
        // This method updates the vector clock on receiving an application message
        synchronized (m) {
            
            wakeNodeIfPassive(msg);

            for (int i=0; i<m.node.numberOfNodes; i++){
                int value = Math.max(m.node.clock.get(i), msg.clock.get(i));
                m.node.clock.set(i, value);
            }
            System.out.println("Vector clock on receiving: "+ m.node.clock);
            m.node.messagesReveived++;
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

    public void handleMarkerMessage(Message msg) throws Exception {
        synchronized (m){
            System.out.println("[MARKER RECEIVED] Received MARKER message from NODE: "+msg.senderId);
            this.m.snapshot.receiveMarkerMessageFromParent(msg);
        }
    }

    public void handleMarkerReplyMessage(Message msg) throws Exception{
        synchronized (m){
            System.out.println("[CHANNEL INPUT] Received MARKER_REPLY message from "+msg.senderId);
            this.m.snapshot.receiveMarkerRepliesFromChildren(msg);
        }
    }
}
