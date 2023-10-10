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
    private Integer gatheredMessagesSent = 0;
    private Integer gatheredMessagesReceived = 0;

    private NodeState gatheredState;

    public ChandyLamport(Main m){
        this.m = m;
        this.PROCESS_COLOR = ProcessColor.BLUE;
        this.gatheredState = NodeState.PASSIVE;
    }

    public void initiateSpanning() throws Exception {
        this.PROCESS_COLOR = ProcessColor.RED;

        for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {

            SctpChannel channel = entry.getValue();

            Set<Integer> newVisited = new HashSet<>();
            newVisited.add(m.node.nodeId);
            
            Message msg = new Message(m.node.nodeId); // MARKER Message Constructor
            synchronized(m) {
                Client.send_message(msg, channel, m);
                this.markersSent+=1;
            }
        }

    }

    public void markerStatus(){
        System.out.println();
        System.out.println(String.format("[SNAPSHOT DEBUG] MARKERS Sent=%d | REPLIES Received=%d", this.markersSent, this.markerRepliesReceived));
        System.out.println();
    }

    public void receiveMarkerRejectionMessage(Message markerRejectionMsg) throws Exception {
        System.out.println("[COLOR]: "+this.PROCESS_COLOR);
        this.markerRepliesReceived += 1;
        checkTreeCollapseStatus();
        System.out.println(String.format("[REJECTION ARRIVED] NODE:%d Rejected you marker message", markerRejectionMsg.senderId));

    }

    public void receiveMarkerMessageFromParent(Message marker) throws Exception {
        System.out.println("[COLOR]: "+this.PROCESS_COLOR);

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
            SctpChannel channel = entry.getValue();

            Message msg = new Message(m.node.nodeId); // MARKER Message Constructor
            synchronized(m) {
                Client.send_message(msg, channel, m);
                this.markersSent++;
            }
        }

        System.out.println(String.format("[MARKER ACCEPTED] MARKER message from NODE-%d is accepted.", marker.senderId));
        markerStatus();
        checkTreeCollapseStatus();
    }

    public void receiveMarkerRepliesFromChildren (Message markerReply) throws Exception{

        this.gatheredLocalSnapshots.putAll(markerReply.localSnapshots);

        this.gatheredMessagesSent += markerReply.messagesSent;
        this.gatheredMessagesReceived += markerReply.messagesReceived;

        if (markerReply.state == NodeState.ACTIVE){
            this.gatheredState = NodeState.ACTIVE;
        }

        this.markerRepliesReceived++;
        markerStatus();

        checkTreeCollapseStatus();
        System.out.println("[CHANNEL INPUT RESPONSE] MARKER_REPLY message is handled");
    };

    private void checkTreeCollapseStatus() throws Exception{
        System.out.println("[COLLAPSE] Tree collapse identified at NODE:"+this.m.node.nodeId);
        markerStatus();
        if (this.markersSent == this.markerRepliesReceived) {
            
            this.gatheredLocalSnapshots.put(this.m.node.nodeId, m.node.clock);
            this.gatheredMessagesSent += this.m.node.messagesSent;
            this.gatheredMessagesReceived += this.gatheredMessagesReceived;

            if (this.m.node.state == NodeState.ACTIVE){
                System.out.println("[ALERT] Node is still active");
                this.gatheredState = NodeState.ACTIVE;
            }

            if (this.m.node.nodeId == 0){
                handleConvergence();
                return;
            }

            Message markerReplyMsg = new Message(
                this.m.node.nodeId, 
                this.gatheredLocalSnapshots, 
                this.gatheredState, 
                this.gatheredMessagesSent, 
                this.gatheredMessagesReceived
            );

            Client.send_message(markerReplyMsg, this.m.idToChannelMap.get(this.parentId), this.m);
        };
    }

    private void handleConvergence(){
        System.out.println("[CONVERGENCE] Euler Traversal successfully completed at node 0.");
        System.out.println("[CONVERGENCE] Local Snapshots = " + this.gatheredLocalSnapshots);
        System.out.println("[CONVERGENCE] Total messages sent = " + this.gatheredMessagesSent);
        System.out.println("[CONVERGENCE] Total messages received = " + this.gatheredMessagesReceived);
        System.out.println("[CONVERGENCE] Node state gathered = " + this.gatheredState);
    }
}