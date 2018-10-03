//package serverpackage;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.PrintWriter;
//import java.io.StringReader;
//import java.io.StringWriter;
//import java.util.NavigableMap;
//import java.util.Random;
//import java.util.TreeMap;
//import java.util.concurrent.ConcurrentSkipListMap;
//import java.util.concurrent.TimeoutException;
//
//import javax.naming.TimeLimitExceededException;
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.Marshaller;
//import javax.xml.bind.Unmarshaller;
//import net.rudp.*;
//
//public class BullyServer extends Thread {
//
//	// public static TreeMap<Integer, ProcessThread> threadMap = new TreeMap<>();
//	public volatile NavigableMap<Integer, ClientInformation> clientInfo = new ConcurrentSkipListMap<>();
//	public ClientInformation clientLeader;
//	// public boolean leaderSelected;
//	static ReliableServerSocket serverSocket;
//	static ReliableSocket clientSocket;
//	private int port;
//	private String bullyServerGroupName;
//	private static BullyProcess process;
//	private static int MAX_TIMEOUT = 4000;
//	// private static BullyProcess leader;
//	// private static ProcessThread pThread;
//	private boolean isAliveCheckInProgress;
//	
//	public synchronized ClientInformation getLeader()
//	{
//		if(clientLeader!=null && !isAliveCheckInProgress)
//		{
//			return clientLeader;
//		}
//		else{
//			return null;
//		}
//	}
//
//	public BullyServer(int portNo,String bullyServerGroupName) {
//		try {
//			
//			serverSocket = new ReliableServerSocket(portNo);
//			System.out.println(bullyServerGroupName+ " started at port "+portNo);
//			this.port=portNo;
//			this.bullyServerGroupName=bullyServerGroupName;
//			
//			//System.out.println("Server started at port "+portNo);
//		} catch (IOException e) {
//			System.out.println("Error: " + e.getMessage());
//		}
//	}
//	
//	public void run(){
//
//		/*try {
//			serverSocket = new ReliableServerSocket(this.port);
//			System.out.println("Server started at port 7500");
//		} catch (IOException e) {
//			System.out.println("Error: " + e.getMessage());
//		}*/
//
//		Thread checkForLeaderInMap = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				System.out.println("Thread for checking the map started");
//				while (true) {
//					//System.out.println("Client empty : " + clientInfo.isEmpty());
//					if (!clientInfo.isEmpty()) {
//						//System.out.println("Checked client map not empty");
//						selectLeader();
//						//System.out.println("From thread checkForLeader Leader : " + clientLeader);
//						try {
//							Thread.sleep(3000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		});
//		checkForLeaderInMap.setDaemon(false);
//		checkForLeaderInMap.start();
//
//		while (true) {
//			try {
//				Thread.sleep(300);
//				System.out.println("Waiting for new connection");
//				ReliableSocket socket = (ReliableSocket) serverSocket.accept();
//				System.out.println("Client Connected");
//				BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//				int nodeID = Integer.parseInt(is.readLine());
//				//System.out.println("Id received by server :" + nodeID);
//
//				ClientInformation newClient = new ClientInformation(socket, nodeID);
//				clientInfo.put(nodeID, newClient);
//				System.out.println("OK");
//				Thread.sleep(300);
//				selectLeader();
//			} catch (Exception e) {
//				System.out.println("Error line 67 : " + e.getMessage() + " " + e.getStackTrace());
//			}
//
//		}
//	}
//
//	public void selectLeader() {
//		isAliveCheckInProgress=true;
//		NavigableMap<Integer, ClientInformation> threadMap2 = clientInfo.descendingMap();
//		System.out.println("Map : " + threadMap2);
//		//String alive = "alive";
//		String response = "";
//		BullyMessagePacket responsePacket = null;
//		
//		BullyMessagePacket packet = new BullyMessagePacket(BullyMessageEnum.Heartbeat);
//		String alive = jaxbObjectToXML(packet);
//		//text = text.replace("\n", "").replace("\r", "");
//		alive = alive.replace("\n", "").replace("\r", "");
//		//System.out.println("Sending packet:");
//		//System.out.println(alive);
//		
//		for (Integer clientID : threadMap2.keySet()) {
//			long start = System.currentTimeMillis();
//			ClientInformation clientInformation = threadMap2.get(clientID);
//			clientInformation.writeToClient(alive);
//
//			try {
//				response = clientInformation.readFromClient(MAX_TIMEOUT);
//				responsePacket = unmarshal1(response);
//				//System.out.println("Response from  bully process : " + response);
//				if (responsePacket.messageType == (BullyMessageEnum.Heartbeat)) {
//					clientLeader = clientInformation;
//				}	break;
//			} catch (TimeLimitExceededException e1) {
//				continue;
//			} catch (IOException e) {
//				System.out.println("IO Error line 94: " + e.getMessage());
//			} catch (JAXBException e) {
//				System.out.println("Error unmarshalling : " +e.getMessage());
//			} catch (NullPointerException e) {
//				clientInfo.remove(clientID);
//				continue;
//			}
//
//		}
//		isAliveCheckInProgress=false;
//		System.out.println("Leader: " + clientLeader);
//	}
//	private static String jaxbObjectToXML(BullyMessagePacket packet) {
//		String xmlString = "";
//		try {
//			JAXBContext context = JAXBContext.newInstance(BullyMessagePacket.class);
//			Marshaller m = context.createMarshaller();
//			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // To format XML
//			StringWriter sw = new StringWriter();
//			m.marshal(packet, sw);
//			xmlString = sw.toString();
//
//		} catch (JAXBException e) {
//			e.printStackTrace();
//		}
//
//		return xmlString;
//
//	}
//
//	public static BullyMessagePacket unmarshal1(String xml) throws JAXBException {
//		JAXBContext jaxbContext = JAXBContext.newInstance(BullyMessagePacket.class);
//		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//
//		StringReader reader = new StringReader(xml);
//		BullyMessagePacket packet = (BullyMessagePacket) jaxbUnmarshaller.unmarshal(reader);
//
//		return packet;
//	}
//
//}
//
