import com.sun.nio.sctp.*;
import java.util.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

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

    public void sendLogic() throws IOException {
        // White true
        Random random = new Random();
        int number_of_messages_to_send = random.nextInt(m.node.maxPerActive - m.node.minPerActive + 1) + m.node.minPerActive;

        send_message(number_of_messages_to_send);
    }

    public void send_message(int count) throws IOException{
        Random random = new Random();

        for (int i=0; i<count; i++){
            int randomNumber = random.nextInt(this.channelList.size());
            SctpChannel channel = channelList.get(randomNumber);
            
            // TODO: Create and use the Message object
            channel.send(null, null);
        }
    }
}
