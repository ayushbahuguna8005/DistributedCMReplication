package serverpackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeoutException;

import javax.naming.TimeLimitExceededException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.rudp.*;

public class BullyServerV2 extends Thread {

	// public static TreeMap<Integer, ProcessThread> threadMap = new
	// TreeMap<>();
	public volatile NavigableMap<Integer, ClientInformation> clientMTL = new ConcurrentSkipListMap<>();
	public volatile NavigableMap<Integer, ClientInformation> clientLVL = new ConcurrentSkipListMap<>();
	public volatile NavigableMap<Integer, ClientInformation> clientDDO = new ConcurrentSkipListMap<>();
	public ClientInformation clientLeaderMTL;
	public ClientInformation clientLeaderLVL;
	public ClientInformation clientLeaderDDO;
	// public boolean leaderSelected;
	static ReliableServerSocket serverSocket;
	static ReliableSocket clientSocket;
	private int port;
	private String bullyServerGroupName;
	private static BullyProcess process;
	private static int MAX_TIMEOUT = 4000;
	// private static BullyProcess leader;
	// private static ProcessThread pThread;
	private boolean isAliveCheckInProgress;

	public synchronized ClientInformation getLeader(String managerID) {
		managerID = managerID.toLowerCase();
		String managerPrefix = managerID.substring(0, 3);
		if (managerPrefix.equals("mtl")) {
			return clientLeaderMTL;
		} else if (managerPrefix.equals("lvl")) {
			return clientLeaderLVL;
		} else {
			return clientLeaderDDO;
		}

	}
	

	
	


	public BullyServerV2(int portNo, String bullyServerGroupName) {
		try {

			serverSocket = new ReliableServerSocket(portNo);
			System.out.println(bullyServerGroupName + " started at port " + portNo);
			this.port = portNo;
			this.bullyServerGroupName = bullyServerGroupName;

			// System.out.println("Server started at port "+portNo);
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	public void run() {

		/*
		 * try { serverSocket = new ReliableServerSocket(this.port);
		 * System.out.println("Server started at port 7500"); } catch
		 * (IOException e) { System.out.println("Error: " + e.getMessage()); }
		 */

		Thread checkForLeaderInMTL = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Thread for checking leader in the MTL map started");
				while (true) {
					//System.out.println("MTL leader selection proccess");
					if (!clientMTL.isEmpty()) {
						 System.out.println("Checked client MTL not empty");
						selectLeader("mtl");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}
			}
		});
		checkForLeaderInMTL.setDaemon(false);
		checkForLeaderInMTL.start();

		Thread checkForLeaderInLVL = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Thread for checking leader in the LVL map started");
				while (true) {
					//System.out.println("LVL leader selection proccess");
					if (!clientLVL.isEmpty()) {
						 System.out.println("Checked client LVL not empty");
						selectLeader("lvl");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		checkForLeaderInLVL.setDaemon(false);
		checkForLeaderInLVL.start();

		Thread checkForLeaderInDDO = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Thread for checking leader in the DDO map started");
				while (true) {
					//System.out.println("DDO leader selection proccess");
					if (!clientDDO.isEmpty()) {
						System.out.println("Checked client DDO not empty");
						selectLeader("ddo");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		checkForLeaderInDDO.setDaemon(false);
		checkForLeaderInDDO.start();

		while (true) {
			try {
				Thread.sleep(300);
				System.out.println("Waiting for new connection");
				ReliableSocket socket = (ReliableSocket) serverSocket.accept();
				System.out.println("Client Connected");
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						try {
							BufferedReader is;
							is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							String nodeIDandServerID = is.readLine();
							String[] tempIDName = nodeIDandServerID.split(":");
							int nodeID = Integer.parseInt(tempIDName[0].trim());
							String serverName = tempIDName[1].trim().toLowerCase();
							int portNo=Integer.parseInt(tempIDName[2].trim());
							System.out.println("Node ID and Server ID received by server :" + nodeIDandServerID);

							ClientInformation newClient = new ClientInformation(socket, nodeID,portNo);
							if (serverName.equals("mtl")) {
								clientMTL.put(nodeID, newClient);
							} else if (serverName.equals("lvl")) {
								
								System.out.println("Map entry in lvl");
								clientLVL.put(nodeID, newClient);
							} else if (serverName.equals("ddo")) {
								System.out.println("Map entry in ddo");
								clientDDO.put(nodeID, newClient);
							}
							System.out.println("OK");
							Thread.sleep(300);
							selectLeader(serverName);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				};
				Thread nodeInformationReadThread=new Thread(runnable);
				nodeInformationReadThread.start();

			} catch (Exception e) {
				System.out.println("Error line 67 : " + e.getMessage() + " "  );
				e.getStackTrace();
			}

		}
	}

	public synchronized void selectLeader(String serverName) {

		isAliveCheckInProgress = true;
		NavigableMap<Integer, ClientInformation> threadMap2;

		if (serverName.equals("mtl")) {
			threadMap2 = clientMTL.descendingMap();
		} else if (serverName.equals("lvl")) {
			threadMap2 = clientLVL.descendingMap();
		} else {
			threadMap2 = clientDDO.descendingMap();
		}

		System.out.println("Map : " + threadMap2);
		// String alive = "alive";
		String response = "";
		BullyMessagePacket responsePacket = null;

		BullyMessagePacket packet = new BullyMessagePacket(BullyMessageEnum.Heartbeat);
		String alive = jaxbObjectToXML(packet);
		// text = text.replace("\n", "").replace("\r", "");
		alive = alive.replace("\n", "").replace("\r", "");
		// System.out.println("Sending packet:");
		// System.out.println(alive);

		for (Integer clientID : threadMap2.keySet()) {
			long start = System.currentTimeMillis();
			ClientInformation clientInformation = threadMap2.get(clientID);
			clientInformation.writeToClient(alive);

			try {
				response = clientInformation.readFromClient(MAX_TIMEOUT);
				responsePacket = unmarshal1(response);
				// System.out.println("Response from bully process : " +
				// response);
				if (responsePacket.messageType == (BullyMessageEnum.Heartbeat) && serverName.equals("mtl")) {
					clientLeaderMTL = clientInformation;
				} else if (responsePacket.messageType == (BullyMessageEnum.Heartbeat) && serverName.equals("lvl")) {
					clientLeaderLVL = clientInformation;
				} else if (responsePacket.messageType == (BullyMessageEnum.Heartbeat) && serverName.equals("ddo")) {
					clientLeaderDDO = clientInformation;
				}
				break;
			} catch (TimeLimitExceededException e1) {
				continue;
			} catch (IOException e) {
				System.out.println("IO Error line 94: " + e.getMessage());
			} catch (JAXBException e) {
				System.out.println("Error unmarshalling : " + e.getMessage());
			} catch (NullPointerException e) {
				if (serverName.equals("mtl")) {
					clientMTL.remove(clientID);
				} else if (serverName.equals("lvl")) {
					clientLVL.remove(clientID);
				} else {
					clientDDO.remove(clientID);
				}
				// clientInfo.remove(clientID);
				continue;
			}

		}
		isAliveCheckInProgress = false;
		System.out.println("Leader selected ");
	}
	
	public synchronized boolean getisAliveCheckInProgress(){
		return isAliveCheckInProgress;
	}
	

	private static String jaxbObjectToXML(BullyMessagePacket packet) {
		String xmlString = "";
		try {
			JAXBContext context = JAXBContext.newInstance(BullyMessagePacket.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // To
																			// format
																			// XML
			StringWriter sw = new StringWriter();
			m.marshal(packet, sw);
			xmlString = sw.toString();

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return xmlString;

	}

	public static BullyMessagePacket unmarshal1(String xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(BullyMessagePacket.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		StringReader reader = new StringReader(xml);
		BullyMessagePacket packet = (BullyMessagePacket) jaxbUnmarshaller.unmarshal(reader);

		return packet;
	}

}

//
