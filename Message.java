import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

enum MessageType {APPLICATION, MARKER};

public class Message implements Serializable 
{
	public MessageType messageType;
	public String message;
	public Vector<Integer> timestamp;
	public int senderId;

	public Message(String message, int senderId, Vector<Integer> timestamp)
	{
		// Contructor for application message
		this.messageType = MessageType.APPLICATION;
		this.message = message;
		this.timestamp = timestamp;
		this.senderId = senderId;
	}

	public Message() {
		// Constructor for marker message
		this.messageType = MessageType.MARKER;
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
