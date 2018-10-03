package serverpackage;

import java.io.Serializable;

public class UDPMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	MessageType message;
	String replyMessage;
	Object data = null;

	public UDPMessage(MessageType message, Object data) {
		this.message = message;
		this.data = data;
	}
}

enum MessageType{
	TransferMessage,
	RecordCount
}
