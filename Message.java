import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

enum MessageType {
	APPLICATION, 
	MARKER, MARKER_REPLY, MARKER_REJECTION, 
	DEMARKER, DEMARKER_REPLY, DEMARKER_REJECTION
};

public class Message implements Serializable 
{
	public MessageType messageType;
	public String message;
	public Vector<Integer> clock;
	public int senderId = -1;
	public Map<Integer, Vector<Integer>> localSnapshots;
	public NodeState state;
	public int messagesSent;
	public int messagesReceived;
	public Set<Integer> parents;
	
	public Message(int senderId, Vector<Integer> timestamp, String message)
	{
		// Contructor for application message
		this.messageType = MessageType.APPLICATION;
		this.message = message;
		this.clock = timestamp;
		this.senderId = senderId;
	}

	public Message(int senderId) {
		// Constructor for marker message
		this.messageType = MessageType.MARKER;
		this.senderId = senderId;
	}

	public Message () {
		this.messageType = MessageType.MARKER_REJECTION;
	}

	public Message(int senderId, Map<Integer, Vector<Integer>> localSnapshots, NodeState state, Integer messagesSent, Integer messagesReceived) {
		// Constructor for the marker reply message
		this.messageType = MessageType.MARKER_REPLY;
		this.senderId = senderId;
		this.localSnapshots = localSnapshots;
		this.state = state;
		this.messagesSent = messagesSent;
		this.messagesReceived = messagesReceived;
	}

	public Message(int senderId, MessageType messageType){
		this.senderId = senderId;
		this.messageType = messageType;
	}

	// Convert current instance of Message to ByteBuffer in order to send message over SCTP
	public byte[] toMessageBytes() throws Exception
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(this);
		byte[] messageBytes = byteArrayOutputStream.toByteArray();

		return messageBytes;
	}

	// Retrieve Message from ByteBuffer received from SCTP
	public static Message fromByteBuffer(ByteBuffer buf) throws Exception
	{
		// Buffer needs to be flipped before reading
		// Buffer flip should happen only once
		buf.flip();
		byte[] data = new byte[buf.limit()];
		buf.get(data);
		buf.clear();

		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Message msg = (Message) ois.readObject();

		bis.close();
		ois.close();

		return msg;
	};
}
