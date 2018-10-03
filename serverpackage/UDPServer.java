package serverpackage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;



public class UDPServer extends Thread {

	private int udpPort = -1;
	BullyProcess server = null;

	public UDPServer(int udpPort, BullyProcess server) {
		this.udpPort = udpPort;
		this.server = server;
	}

	public void run() {
		System.out.println("UDP Thread started at port " + udpPort + " for server " + server.serverName);
		DatagramSocket asocket = null;
		try {
			asocket = new DatagramSocket(udpPort);
			byte[] receiveBuffer = null;
			DatagramPacket recievePacket = null;
			while (true) {
				receiveBuffer = new byte[10000];
				recievePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				asocket.receive(recievePacket);
				new RespondToUDPRequest(recievePacket, server).start();
			}

		} catch (SocketException ex) {
			System.out.println(ex.getMessage());
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {
			if (asocket != null)
				asocket.close();
		}
	}
}
