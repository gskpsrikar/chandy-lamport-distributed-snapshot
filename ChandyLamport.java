import java.util.Map;
import java.util.Set;

import com.sun.nio.sctp.SctpChannel;

public class ChandyLamport {
    public Main m;
    public Set<Integer> parents;

    public ChandyLamport(Main m){
        this.m = m;

    }

    public void spanTree() throws Exception {
        try {
            System.out.println("[THREAD] Initiating Snapshot DFS function in 3 seconds");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {
            
            // Integer nodeId = entry.getKey();
            SctpChannel channel = entry.getValue();

            Message msg = new Message(m.node.nodeId, parents);
            synchronized(m) {
                Client.send_message(msg, channel, m);
            }
        }
    }

    public void collapseTree() throws Exception {

    }
}
