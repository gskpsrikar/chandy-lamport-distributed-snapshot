import com.sun.nio.sctp.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class Listener {
    private int port;
    private int MAX_MSG_SIZE = 4096;
    private Main m;

    public Listener(Main m) {
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

    public static void logReceiveEvent(Message msg) {
        // TODO: Do operations that needs to be done on receiving a messsage.
    }

    
}
