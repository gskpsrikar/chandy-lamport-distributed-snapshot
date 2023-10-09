import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

enum MessageType {APPLICATION, MARKER, MARKER_REPLY};

public class Message implements Serializable 
{
	public MessageType messageType;
	public String message;
	public Vector<Integer> clock;
	public int senderId;
	public Map<Integer, Vector<Integer>> localSnapshots;
	public NodeState state;
	public Integer messagesSent;
	public Integer messagesReceived;
	public Set<Integer> visited;

	public Message(int senderId, Vector<Integer> timestamp, String message)
	{
		// Contructor for application message
		this.messageType = MessageType.APPLICATION;
		this.message = message;
		this.clock = timestamp;
		this.senderId = senderId;
	}

	public Message(int senderId, Set<Integer> visited) {
		// Constructor for marker message
		this.messageType = MessageType.MARKER;
		this.senderId = senderId;
		this.visited = visited;

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
