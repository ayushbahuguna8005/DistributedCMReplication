package serverpackage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPRequestSender {

	UDPMessage requestMsg;
	int serverPort;
	InetAddress serverAddress;
	private String recordCountWithNameOfOtherServer = "";
	private String replyOfTransferRequest = "";

	public UDPRequestSender(UDPMessage requestMsg, int serverPort, InetAddress serverAddress) {
		this.requestMsg = requestMsg;
		this.serverPort = serverPort;
		this.serverAddress = serverAddress;
	}

	public void sendRequest() throws IOException {
		DatagramSocket asocket = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		try {
			asocket = new DatagramSocket();
			objectOutputStream.writeObject(requestMsg);
			byte[] sendData = byteArrayOutputStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
			asocket.send(sendPacket);
			//System.out.println("Packet sent successfully");
			byte[] buffer = new byte[10000];
			DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
			asocket.receive(receivePacket);
			ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(buffer));
			try {
				UDPMessage replyMessage = (UDPMessage) iStream.readObject();
				if (replyMessage.message == MessageType.RecordCount) {
					recordCountWithNameOfOtherServer = replyMessage.replyMessage;
				} else if (replyMessage.message == MessageType.TransferMessage) {
					replyOfTransferRequest = replyMessage.replyMessage;
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("Error occured here UDPRequestSender->sendRequest->ClassNotFoundException ");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Error occured here UDPRequestSender->sendRequest->Exception ");
				e.printStackTrace();
			}

		} finally {
			if (asocket != null)
				asocket.close();
		}
	}

	/*
	 * public void sendRequest() throws IOException { DatagramSocket asocket =
	 * null; ByteArrayOutputStream byteArrayOutputStream = new
	 * ByteArrayOutputStream(); ObjectOutputStream objectOutputStream = new
	 * ObjectOutputStream(byteArrayOutputStream); try { asocket = new
	 * DatagramSocket(); if (requestMsg.message == MessageType.RecordCount) {
	 * byte[] sendMessage = "getRecordCount".getBytes(); DatagramPacket
	 * sendPacket = new DatagramPacket(sendMessage, sendMessage.length,
	 * serverAddress, serverPort); asocket.send(sendPacket); byte[] buffer = new
	 * byte[1000]; DatagramPacket receivePacket = new DatagramPacket(buffer,
	 * buffer.length); asocket.receive(receivePacket);
	 * recordCountWithNameOfOtherServer = new String(buffer).trim(); } else if
	 * (requestMsg.message == MessageType.TransferMessage) {
	 * 
	 * objectOutputStream.writeObject(requestMsg); byte[] buffer =
	 * byteArrayOutputStream.toByteArray(); DatagramPacket sendPacket = new
	 * DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
	 * asocket.send(sendPacket); } } finally { if (asocket != null)
	 * asocket.close(); } }
	 */

	public String getrecordCountWithNameOfOtherServer() {
		return recordCountWithNameOfOtherServer;
	}

	public String getreplyOfTransferRequest() {
		return replyOfTransferRequest;
	}

}
