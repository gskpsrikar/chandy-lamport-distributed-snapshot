import com.sun.nio.sctp.*;
import java.util.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class Sender {

    public List<SctpChannel> channelList;
    public Main m;

    public Sender(Main m){
        this.m = m;
        this.channelList = buildChannels(m.node);
    }

    private List<SctpChannel> buildChannels(Node node) {
        List<SctpChannel> channelList = new ArrayList<>();

        for (List<String> neighbor: node.neighbors) {
            String neighbor_name = neighbor.get(0);
            int port = Integer.parseInt(neighbor.get(1));

            try {
                SctpChannel clientChannel = SctpChannel.open();
                clientChannel.connect(
                    new InetSocketAddress("sxs210570@"+neighbor_name, port)
                );
                channelList.add(clientChannel);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return channelList;
    }

    public void sendLogic() throws Exception {
        while (true) {
            if (m.node.state.equals("passive")){
                Random random = new Random();
                int number_of_messages_to_send = random.nextInt(m.node.maxPerActive - m.node.minPerActive + 1) + m.node.minPerActive;
                send_message(number_of_messages_to_send);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void send_message(int count) throws Exception{
        Random random = new Random();

        for (int i=0; i<count; i++){

            if (m.node.messagesSent >= m.node.maxNumber){
                m.node.state = "passive";
            }

            int randomNumber = random.nextInt(this.channelList.size());
            SctpChannel channel = channelList.get(randomNumber);
            
            // TODO: Create and use the Message object
            Message msg = new Message("Sending from " + m.node.currentNodeName);
            
            // Create a message info object to specify destination address and stream ID
		    MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
            byte[] messageBytes = msg.toMessageBytes();
            
            channel.send(ByteBuffer.wrap(messageBytes), messageInfo);

            synchronized (m) {
                m.node.messagesSent ++;
            }

            try {
                Thread.sleep(m.node.minSendDelay);
                System.out.println("Resumed after 2 seconds");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
