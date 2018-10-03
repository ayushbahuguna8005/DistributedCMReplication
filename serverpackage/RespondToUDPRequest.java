package serverpackage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RespondToUDPRequest extends Thread {

	private DatagramPacket receivedPacket;
	private BullyProcess server;

	public RespondToUDPRequest(DatagramPacket receivedPacket, BullyProcess server) {
		this.receivedPacket = receivedPacket;
		this.server = server;
	}

	public void run() {
		try {
			ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivedPacket.getData()));
			UDPMessage incomingMessage = (UDPMessage) iStream.readObject();
			if (incomingMessage.message == MessageType.RecordCount) {
				incomingMessage.replyMessage = getServerRecordCount();
			} else if (incomingMessage.message == MessageType.TransferMessage) {
				incomingMessage.replyMessage = saveTransferObjectAtServer(incomingMessage.data);
				incomingMessage.data = null;

			}
			sendData(convertToPacket(incomingMessage));
		} catch (IOException e) {

			System.out.println("Exception at RespondToUDPRequest");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception RespondToUDPRequest");
			e.printStackTrace();
		}
	}

	public String saveTransferObjectAtServer(Object o) {
		return server.saveTransferObjectAtServer(o);
	}

	public String getServerRecordCount() {
		return server.getShortFormOfServerName(server.serverName) + " " + server.calculateNumberOfRecords();

	}

	public byte[] convertToPacket(UDPMessage message) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] data = null;
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(message);
			data = byteArrayOutputStream.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public void sendData(byte[] dataToBeSent) {
		int dataLength = dataToBeSent.length;
		DatagramPacket replyPacket = new DatagramPacket(dataToBeSent, dataLength, receivedPacket.getAddress(),
				receivedPacket.getPort());
		DatagramSocket asocket = null;
		try {
			asocket = new DatagramSocket();
			asocket.send(replyPacket);
		} catch (SocketException e) {
			System.out.println("Error here at RespondToUDPRequest");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error here at RespondToUDPRequest");
			e.printStackTrace();
		} finally {
			if (asocket != null)
				asocket.close();
		}
	}

}
