import java.util.Map;

import com.sun.nio.sctp.SctpChannel;

public class ChandyLamport {
    public Main m;

    public ChandyLamport(Main m){

    }

    public void dfs() throws Exception {
        try {
            System.out.println("Initiating Snapshot DFS function in 3 seconds");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {
            
            Integer nodeId = entry.getKey();
            SctpChannel channel = entry.getValue();
            
            System.out.println("Key: " + nodeId + ", Channel: " + channel);

            Message msg = new Message(m.node.nodeId);

            synchronized(m) {
                Client.send_message(msg, channel, m);
            }
        }
    }
}
