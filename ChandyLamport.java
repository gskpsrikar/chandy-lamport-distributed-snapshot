import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.sun.nio.sctp.SctpChannel;

enum ProcessColor {BLUE, RED};

public class ChandyLamport {
    public Main m;
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

    private void resetSnapshot(){
        this.markersSent = 0;
        this.markerRepliesReceived = 0;
        this.PROCESS_COLOR = ProcessColor.BLUE;
        this.gatheredState = NodeState.PASSIVE;
        this.gatheredLocalSnapshots = new HashMap<>();
        this.gatheredMessagesSent = 0;
        this.gatheredMessagesReceived = 0;
    }

    public void initiateSpanning() throws Exception {
        System.out.println("[INITIATE] Initiating Snapshot Spanning process at NODE: "+this.m.node.nodeId);
        
        this.PROCESS_COLOR = ProcessColor.RED;

        this.snapshotStatus();
        // System.out.println("[TRACE] Channels are "+m.idToChannelMap);
        for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {

            SctpChannel channel = entry.getValue();
            
            Message msg = new Message(m.node.nodeId); // MARKER Message Constructor
            System.out.println("[TRACE] Sending MessageType of "+msg.messageType+" to "+entry.getKey());
            Client.send_message(msg, channel, m);
            this.markersSent+=1;
        }
    }

    public void snapshotStatus(){
        System.out.println();
        System.out.println("[PROCESS COLOR]: "+this.PROCESS_COLOR);
        System.out.println(String.format("[SNAPSHOT DEBUG] MARKERS Sent=%d | REPLIES Received=%d", this.markersSent, this.markerRepliesReceived));
        System.out.println();
    }

    public void receiveSnapshotResetMessage(Message resetMessage) throws Exception{
        if (this.PROCESS_COLOR == ProcessColor.BLUE){
            System.out.println("[END_SNAPSHOT: rejected] Rejected END_SNAPSHOT at "+this.m.node.nodeId);
            return;
        }

        this.resetSnapshot();
        System.out.println("[RESET SNAPSHOT] This node is set to BLUE");
        
        // System.out.println("[SNAPSHOT PROCESS RESULT] "+resetMessage.message);

        for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {
            if (entry.getKey() == 0){
                System.out.println("[REFRAIN] Refraining from sending end snapshot message to Node 0.");
            }
            SctpChannel channel = entry.getValue();
            Message msg = new Message(resetMessage.message); // RESET SNAPSHOT Message Constructor
            synchronized(m) {
                Client.send_message(msg, channel, m);
            }
        }
    }

    public void receiveMarkerRejectionMessage(Message markerRejectionMsg) throws Exception {
        // System.out.println("[COLOR]: "+this.PROCESS_COLOR);
        this.markerRepliesReceived += 1;
        checkTreeCollapseStatus();
        System.out.println(String.format("[REJECTION ARRIVED] NODE:%d Rejected you marker message", markerRejectionMsg.senderId));

    }

    public void receiveMarkerMessageFromParent(Message marker) throws Exception {
        // System.out.println("[COLOR]: "+this.PROCESS_COLOR);

        if (this.PROCESS_COLOR == ProcessColor.RED){
            Message rejectMarker = new Message();
            SctpChannel channel = this.m.idToChannelMap.get(marker.senderId);
            Client.send_message(rejectMarker, channel, this.m);
            System.out.println(String.format("[MARKER REJECTED] MARKER message from NODE-%d is rejected.", marker.senderId));
            // snapshotStatus();
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
        // snapshotStatus();
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
        System.out.println("[MARKER REPLY ACCEPTED]");
        // snapshotStatus();

        checkTreeCollapseStatus();
        // System.out.println("[CHANNEL INPUT RESPONSE] MARKER_REPLY message is handled");
    };

    private void checkTreeCollapseStatus() throws Exception{
        // System.out.println("[COLLAPSE] Tree collapse identified at NODE:"+this.m.node.nodeId);
        if (this.markersSent == this.markerRepliesReceived) {
            
            this.gatheredLocalSnapshots.put(this.m.node.nodeId, m.node.clock);
            this.gatheredMessagesSent += this.m.node.messagesSent;
            this.gatheredMessagesReceived += this.m.node.messagesReveived;

            if (this.m.node.state == NodeState.ACTIVE){
                // System.out.println("[ALERT] Node is still active");
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

    private void handleConvergence() throws Exception{
        System.out.println("[CONVERGENCE] Euler Traversal successfully completed at node 0.");
        System.out.println("[CONVERGENCE] Local Snapshots = " + this.gatheredLocalSnapshots);
        System.out.println("[CONVERGENCE] Total messages sent = " + this.gatheredMessagesSent);
        System.out.println("[CONVERGENCE] Total messages received = " + this.gatheredMessagesReceived);
        System.out.println("[CONVERGENCE] Node state gathered = " + this.gatheredState);

        this.initiateSnapshotReset();
    }

    private void initiateSnapshotReset() throws Exception{
        System.out.println("[INITIATE] Initiating Snapshot Reset Process resetting snapshot states for all nodes");
        
        this.PROCESS_COLOR = ProcessColor.BLUE;

        Boolean TERMINATED = false;

        for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {

            SctpChannel channel = entry.getValue();

            String messageText;
            if (this.gatheredState == NodeState.ACTIVE || this.gatheredMessagesSent != this.gatheredMessagesReceived){
                messageText = "**** SYSTEM IS NOT TERMINATED ****";
            } else {
                messageText = "**** YOU ARE TERMINATED ****";
                TERMINATED = true;
            }

            Message msg = new Message(messageText); // END_SNAPSHOT Message Constructor
            synchronized(m) {
                Client.send_message(msg, channel, m);
            }
        };

        this.resetSnapshot();

        if (m.node.nodeId == 0 && !TERMINATED){
            System.out.println("[SNAPSHOT START] Initiating new Snapshot Process.");
            try {
                System.out.println(String.format("[SNAPSHOT PROCESS SLEEPING] Sleeping for %d(ms) seconds to allow other nodes wake other nodes...", this.m.node.snapshotDelay));
                Thread.sleep(this.m.node.snapshotDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // this.initiateSpanning();
        } else {
            System.out.println("SNAPSHOT PROTOCOL DETECTED TERMINATION. NOT FURTHER SPANNING;");
        }
    }
}