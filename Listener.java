import com.sun.nio.sctp.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class Listener {
    private int port;
    private int MAX_MSG_SIZE = 4096;

    public Listener(int port) {
        this.port = port;
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
                    String messageString = "--------";
                    while (sc.isOpen()){
                        try {
                            sc.receive(buf, null, null);
                            
                            Message msg = Message.fromByteBuffer(buf);
                            messageString = msg.message;

                            System.out.println("Received message text: " + messageString);

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
}
