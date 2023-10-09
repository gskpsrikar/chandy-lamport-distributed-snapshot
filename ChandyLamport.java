import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.sun.nio.sctp.SctpChannel;

enum ProcessColor {BLUE, RED};

public class ChandyLamport {
    public Main m;
    public Set<Integer> visited;
    public Integer parentId;

    public Integer markersSent=0;
    public Integer markerRepliesReceived=0;
    
    public ProcessColor PROCESS_COLOR;

    private Map<Integer, Vector<Integer>> gatheredLocalSnapshots = new HashMap<>();

    public ChandyLamport(Main m){
        this.m = m;
        this.PROCESS_COLOR = ProcessColor.BLUE;
    }

    public void initiateSpanning() throws Exception {
        for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {

            SctpChannel channel = entry.getValue();

            Set<Integer> newVisited = new HashSet<>();
            newVisited.add(m.node.nodeId);
            
            Message msg = new Message(m.node.nodeId, newVisited); // MARKER Message Constructor
            synchronized(m) {
                Client.send_message(msg, channel, m);
                this.markersSent++;
            }
        }

    }

    public void markerStatus(){
        System.out.println();
        System.out.println(String.format("[SNAPSHOT DEBUG] MARKERS Sent=%d | Replies Receivied=%d", this.markersSent, this.markerRepliesReceived));
        System.out.println();
    }

    public void receiveMarkerRejectionMessage(Message markerRejectionMsg) throws Exception {
        this.markerRepliesReceived++;
        checkTreeCollapseStatus();

    }

    public void receiveMarkerMessageFromParent(Message marker) throws Exception {

        if (this.PROCESS_COLOR == ProcessColor.RED){
            Message rejectMarker = new Message();
            SctpChannel channel = this.m.idToChannelMap.get(marker.senderId);
            Client.send_message(rejectMarker, channel, this.m);
            System.out.println(String.format("[MARKER REJECTED] MARKER message from NODE-%d is rejected.", marker.senderId));
            markerStatus();
            return;
        }

        this.PROCESS_COLOR = ProcessColor.RED;
        this.parentId = marker.senderId;

        for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {
            Integer neighborId = entry.getKey();
            if (marker.visited.contains(neighborId)){
                continue;
            } else {
                SctpChannel channel = entry.getValue();
                Set<Integer> newVisited = new HashSet<>(marker.visited);
                newVisited.add(m.node.nodeId);
                Message msg = new Message(m.node.nodeId, newVisited); // MARKER Message Constructor
                synchronized(m) {
                    Client.send_message(msg, channel, m);
                    this.markersSent++;
                }
            }
        }
        System.out.println(String.format("[MARKER ACCEPTED] MARKER message from NODE-%d is accepted.", marker.senderId));
        markerStatus();
        checkTreeCollapseStatus();
    }

    public void receiveMarkerRepliesFromChildren (Message markerReply) throws Exception{

        this.gatheredLocalSnapshots.putAll(markerReply.localSnapshots);
        this.markerRepliesReceived++;
        markerStatus();

        checkTreeCollapseStatus();
        System.out.println("[CHANNEL INPUT RESPONSE] MARKER_REPLY message is handled");
    };

    private void checkTreeCollapseStatus() throws Exception{
        if (this.markersSent == this.markerRepliesReceived) {
            this.gatheredLocalSnapshots.put(this.m.node.nodeId, m.node.clock);
            if (this.m.node.nodeId == 0){
                handleConvergence();
                return;
            }
            Message markerReplyMsg = new Message(
                this.m.node.nodeId, 
                this.gatheredLocalSnapshots, 
                this.m.node.state, 
                this.m.node.messagesSent, 
                this.m.node.messagesReveived
            );
            Client.send_message(markerReplyMsg, this.m.idToChannelMap.get(this.parentId), this.m);
        };
    }

    private void handleConvergence(){
        System.out.println("[CONVERGENCE] Euler Traversal successfully completed at node 0.");
        System.out.println("[DEBUG] " + this.m);
    }
}
