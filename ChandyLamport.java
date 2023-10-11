import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.sun.nio.sctp.SctpChannel;

enum ProcessColor {BLUE, RED};

public class ChandyLamport {
    public Main m;
    public int parentId;

    public int markersSent=0;
    public int markerRepliesReceived=0;
    
    public ProcessColor PROCESS_COLOR;

    private Map<Integer, Vector<Integer>> gatheredLocalSnapshots = new HashMap<>();
    private int gatheredMessagesSent = 0;
    private int gatheredMessagesReceived = 0;
    private NodeState gatheredState;

    private int demarkersSent = 0;
    private int demarkerRepliesReceived = 0;

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

        for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {
            if (entry.getKey() == 0 || resetMessage.parents.contains(entry.getKey())){
                System.out.println("[REFRAIN] Refraining from sending end snapshot message to Node "+entry.getKey());
            }
            SctpChannel channel = entry.getValue();

            Set<Integer> parents = new HashSet<>(resetMessage.parents);
            parents.add(this.m.node.nodeId);
            Message msg = new Message(resetMessage.message, parents); // RESET SNAPSHOT Message Constructor
            synchronized(m) {
                Client.send_message(msg, channel, m);
            }
        }
    }

    public void receiveMarkerRejectionMessage(Message markerRejectionMsg) throws Exception {
        // System.out.println("[COLOR]: "+this.PROCESS_COLOR);
        this.markerRepliesReceived += 1;
        checkTreeCollapseStatus();
        // System.out.println(String.format("[REJECTION ARRIVED] NODE:%d Rejected you marker message", markerRejectionMsg.senderId));

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

            Helper.writeOutput(this.m.node.nodeId, this.m.node.clock);

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
        Helper.verifyConsistency(this.gatheredLocalSnapshots, this.m.node.numberOfNodes);
        this.initiateSnapshotReset();
        // this.initiateDemarkationProcess();
    }

    // public void receiveDemarkerFromParent(Message demarker) throws Exception{
    //     // TODO: To be done
    //     if (this.PROCESS_COLOR == ProcessColor.BLUE){
    //         Message rejectDemarker = new Message(this.m.node.nodeId, MessageType.DEMARKER);
    //         SctpChannel channel = this.m.idToChannelMap.get(demarker.senderId);
    //         Client.send_message(rejectDemarker, channel, this.m);
    //         System.out.println(String.format("[DEMARKER REJECTED] DEMARKER message from NODE-%d is rejected.", demarker.senderId));
    //         return;
    //     }

    //     this.PROCESS_COLOR = ProcessColor.BLUE;
    //     this.parentId = demarker.senderId;

    //     this.resetSnapshot();

    //     for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {
    //         SctpChannel channel = entry.getValue();
    //         Message msg = new Message(this.m.node.nodeId, MessageType.DEMARKER); // DEMARKER Message Constructor
    //         synchronized(m) {
    //             Client.send_message(msg, channel, m);
    //             this.demarkersSent++;
    //         }
    //     }

    //     System.out.println(String.format("[DEMARKER ACCEPTED] DEMARKER message from NODE-%d is accepted.", demarker.senderId));
    //     // snapshotStatus();
    //     checkTreeCollapseStatus();
    // }

    // public void receiveDemarkationRepliesFromChildren(Message demarkerReply) throws Exception{
    //     // DONE
    //     this.demarkerRepliesReceived++;
    //     System.out.println("[DEMARKER REPLY ACCEPTED]");

    //     checkDemarkationCollapse();
    // }

    // private void initiateDemarkationProcess() throws Exception {
    //     // DONE
    //     System.out.println("[INITIATE] Initiating Demarkation Spanning process at NODE: "+this.m.node.nodeId);
    //     this.PROCESS_COLOR = ProcessColor.BLUE;
    //     this.resetSnapshot();
    //     for (Map.Entry<Integer, SctpChannel> entry : m.idToChannelMap.entrySet()) {
    //         SctpChannel channel = entry.getValue();
    //         Message demarker = new Message(this.m.node.nodeId, MessageType.DEMARKER); // DEMARKER Message Constructor
    //         System.out.println("[TRACE] Sending MessageType of "+demarker.messageType+" to "+entry.getKey());
    //         Client.send_message(demarker, channel, m);
    //         this.demarkersSent+=1;
    //     }
    // }

    // private void checkDemarkationCollapse() throws Exception {
    //     // DONE
    //     if (this.demarkersSent == this.demarkerRepliesReceived) {
    //         if (this.m.node.nodeId == 0){
    //             this.handleDemarkationConvergence();
    //             return;
    //         }
    //         Message demarkerReplyMessage = new Message(this.m.node.nodeId, MessageType.DEMARKER_REPLY);
    //         Client.send_message(demarkerReplyMessage, this.m.idToChannelMap.get(this.parentId), this.m);
    //     };
    // }

    // private void handleDemarkationConvergence() throws Exception {
    //     // DONE
    //     System.out.println("--------------- DEMARKATION FINISHED --------------");
    //     System.out.println(String.format("[SNAPSHOT PROCESS SLEEPING] Sleeping for %d(ms) seconds to allow other nodes wake other nodes...", this.m.node.snapshotDelay));
    //     Thread.sleep(this.m.node.snapshotDelay);
    //     this.initiateSpanning();
    // }

    // public void receiveDemarkerRejectionMessage() throws Exception{
    //     // DONE
    //     this.demarkerRepliesReceived += 1;
    //     this.checkDemarkationCollapse();
    // }

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
            
            Set<Integer> parents = new HashSet<>();
            parents.add(0);

            Message msg = new Message(messageText, parents); // END_SNAPSHOT Message Constructor
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