import java.util.Map;

import com.sun.nio.sctp.SctpChannel;

public class ChandyLamport {
    public Main m;

    public ChandyLamport(Main m){
        this.m = m;
    }

    public void dfs() throws Exception {
        try {
            System.out.println("[THREAD] Initiating Snapshot DFS function in 3 seconds");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("######### [DEBUG] IdToChannelMap = " + m.idToChannelMap);

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
