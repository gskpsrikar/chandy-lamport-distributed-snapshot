import com.sun.nio.sctp.*;
import java.util.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class Sender {

    public List<SctpChannel> channelList;
    public Main m;

    public Sender(Main m) throws Exception{
        this.m = m;
        this.channelList = buildChannels(m.node);
        // sendLogic();
    }

    private List<SctpChannel> buildChannels(Node node) {
        System.out.println("Creating channels for neighboring nodes....");
        List<SctpChannel> channelList = new ArrayList<>();

        for (List<String> neighbor: node.neighbors) {
            String neighbor_name = neighbor.get(0);
            int port = Integer.parseInt(neighbor.get(1));

            try {
                SctpChannel clientChannel = SctpChannel.open();
                clientChannel.connect(
                    new InetSocketAddress(neighbor_name, port)
                );
                channelList.add(clientChannel);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Number of channels created = " + channelList.size());
        return channelList;
    }

    public void sendLogic() throws Exception {
        while (true) {
            if (m.node.messagesSent >= m.node.maxNumber){
                System.out.println("Node sent maximum number of messages. Going permanently passive");
                break;
            }
            if (m.node.state.equals("active")){
                System.out.println("Entering sendLogic() active");
                Random random = new Random();
                int number_of_messages_to_send = random.nextInt(m.node.maxPerActive - m.node.minPerActive + 1) + m.node.minPerActive;
                send_message(number_of_messages_to_send);
            }
            else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        System.out.println("Exiting sendLogic() while(true) loop.");
    }

    public void send_message(int count) throws Exception{
        Random random = new Random();

        for (int i=0; i<count; i++){

            if (m.node.messagesSent >= m.node.maxNumber){
                m.node.state = "passive";
                break;
            }

            int randomNumber = random.nextInt(this.channelList.size());
            SctpChannel channel = channelList.get(randomNumber);
            
            String messageString = String.format(
                "Hi from %s! (%d/%d)", m.node.currentNodeName, m.node.messagesSent, m.node.maxNumber
            );
            Message msg = new Message(messageString);
            
		    MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
            byte[] messageBytes = msg.toMessageBytes();
            
            channel.send(ByteBuffer.wrap(messageBytes), messageInfo);

            synchronized (m) {
                m.node.messagesSent ++;
            }

            try {
                Thread.sleep(m.node.minSendDelay);
                System.out.println("Delaying sending messages for" + m.node.minSendDelay/1000 + " seconds");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}