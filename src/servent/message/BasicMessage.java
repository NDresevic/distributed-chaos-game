package servent.message;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import app.ChordState;

/**
 * A default message implementation. This should cover most situations.
 * If you want to add stuff, remember to think about the modificator methods.
 * If you don't override the modificators, you might drop stuff.
 * @author bmilojkovic
 *
 */
public class BasicMessage implements Message, Comparable<Message> {

	private static final long serialVersionUID = -9075856313609777945L;
	private final MessageType type;
	private final int senderPort;
	private final int receiverPort;
	private final String senderIpAddress;
	private final String receiverIpAddress;
	private final String messageText;
	private int clock;

	//This gives us a unique id - incremented in every natural constructor.
	private static AtomicInteger messageCounter = new AtomicInteger(0);
	private final int messageId;
	
	public BasicMessage(MessageType type, int senderPort, int receiverPort, String senderIpAddress,
						String receiverIpAddress) {
		this(type, senderPort, receiverPort, senderIpAddress, receiverIpAddress, "");
	}
	
	public BasicMessage(MessageType type, int senderPort, int receiverPort, String senderIpAddress,
						String receiverIpAddress, String messageText) {
		this.type = type;
		this.senderPort = senderPort;
		this.receiverPort = receiverPort;
		this.senderIpAddress = senderIpAddress;
		this.receiverIpAddress = receiverIpAddress;
		this.messageText = messageText;
		this.clock = 0;

		this.messageId = messageCounter.getAndIncrement();
	}
	
	@Override
	public MessageType getMessageType() {
		return type;
	}
	
	@Override
	public int getReceiverPort() {
		return receiverPort;
	}

	@Override
	public String getSenderIpAddress() {
		return senderIpAddress;
	}

	@Override
	public String getReceiverIpAddress() {
		return receiverIpAddress;
	}
	
	@Override
	public int getSenderPort() {
		return senderPort;
	}

	@Override
	public String getMessageText() {
		return messageText;
	}
	
	@Override
	public int getMessageId() {
		return messageId;
	}

	@Override
	public int getClock() {
		return clock;
	}

	@Override
	public void setClock(int clock) {
		this.clock = clock;
	}

	@Override
	public boolean isFifo() {
		return false;
	}

	/**
	 * Comparing messages is based on their unique id and the original sender ip address and port.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicMessage) {
			BasicMessage other = (BasicMessage)obj;
			
			if (getMessageId() == other.getMessageId() &&
				getSenderIpAddress() == other.getSenderIpAddress() &&
				getSenderPort() == other.getSenderPort()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Hash needs to mirror equals, especially if we are gonna keep this object
	 * in a set or a map. So, this is based on message id, original sender ip address and port.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getMessageId(), getSenderIpAddress(), getSenderPort());
	}
	
	/**
	 * Returns the message in the format:
	 * <code>[sender_id|sender_ip|sender_port|message_id|text|type|receiver_ip|receiver_port|receiver_id]</code>
	 */
	@Override
	public String toString() {
		return "[" + getSenderIpAddress() + "|" + getSenderPort() +
				"|" + getMessageId() + "|" + getMessageText() + "|" + getMessageType() +
				"|" + getReceiverIpAddress() + "|" + getReceiverPort() + "]";
	}

	@Override
	public int compareTo(Message other) {
		if (this.getClock() < other.getClock()) return -1;
		return 1;
	}
}
