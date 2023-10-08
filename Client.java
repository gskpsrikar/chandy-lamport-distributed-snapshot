import com.sun.nio.sctp.*;
import java.util.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class Client {

    public List<SctpChannel> channelList;
    public Main m;

    public Client(Main m) throws Exception{
        this.m = m;
        this.channelList = buildChannels(m.node);
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

    public void sendApplicationMessageLogic() throws Exception {
        while (true) {
            if (m.node.messagesSent >= m.node.maxNumber){
                System.out.println("[TERMINATION] Node sent maximum number of messages. Going permanently passive");
                break;
            }

            if (m.node.state.equals("active")){
                Random random = new Random();
                int number_of_messages_to_send = random.nextInt(m.node.maxPerActive - m.node.minPerActive + 1) + m.node.minPerActive;
                sendBatchMessages(number_of_messages_to_send);
            }
            else {
                try {
                    System.out.println(String.format("Node is temporarily passive (Messages sent: [%d/%d]", m.node.messagesSent, m.node.maxNumber));
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        System.out.println("Exiting sendLogic() method.");
    }

    public void sendBatchMessages(int count) throws Exception{
        
        // This method sends 'count' number of messages to a randomly chosen neighbor
        System.out.println(String.format("Sending a batch of %d messages", count));
        Random random = new Random();

        for (int i=0; i<count; i++){

            if (m.node.messagesSent >= m.node.maxNumber){
                m.node.state = "passive";
                break;
            }

            int randomNumber = random.nextInt(this.channelList.size());
            SctpChannel channel = channelList.get(randomNumber);
            
            String messageString = String.format(
                "Hi from %s! (%d/%d)", m.node.currentNodeName, m.node.messagesSent+1, m.node.maxNumber
            );
            Message msg = new Message(messageString, m.node.nodeId, m.node.clock);
            
            synchronized (m){ // This block increments the value of vector clock after sending a message
                Client.send_message(msg, channel, m);
            }

            try {
                Thread.sleep(m.node.minSendDelay);
                System.out.println(String.format("Delaying sending messages for %d milliseconds", m.node.minSendDelay));
                System.out.println();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[STATE CHANGE] Flipping node state from active to passive because a batch of messages are sent to neighbors.");
        m.node.flipState();
    }

    public static void send_message(Message msg, SctpChannel channel, Main m) throws Exception {
        
        // This static method sends a message.
        //  If the message is APPLICATION type, update the vector clock.


        MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
        byte[] messageBytes = msg.toMessageBytes();
        
        channel.send(ByteBuffer.wrap(messageBytes), messageInfo);

        if (msg.messageType == MessageType.APPLICATION){
            System.out.println("[DEBUG] Sending application message");

            int prevEntry = m.node.clock.get(m.node.nodeId);

            System.out.println(m.node.messagesSent +" Before sending: " + m.node.clock);
            
            m.node.clock.set(m.node.nodeId, prevEntry+1);

            System.out.println(m.node.messagesSent + " After sending: " + m.node.clock);

            m.node.messagesSent ++;
        }

    }
}
