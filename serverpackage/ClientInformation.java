package serverpackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.naming.TimeLimitExceededException;

import net.rudp.ReliableSocket;

class ClientInformation {
	private BufferedReader is;
	private PrintWriter os;
	private int myNodeID;
	private int myUDPPortNo;
	ReliableSocket socket;

	public ClientInformation() {

	}

	public ClientInformation(ReliableSocket socket, int myNodeID,int myUDPPortNo) {
		this.myNodeID = myNodeID;
		this.socket = socket;
		this.myUDPPortNo=myUDPPortNo;
		try {
			is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//is = new BufferedReaderWrapper(new InputStreamReader(socket.getInputStream()));
			os = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * public String readFromClient(int timeout) throws TimeoutException {
	 * System.out.println("Reading from " + myNodeID); try { long start =
	 * System.currentTimeMillis(); while (!is.ready()) { if
	 * (System.currentTimeMillis() - start > timeout) { throw new
	 * TimeoutException(); } } return is.readLine(); } catch (IOException e) {
	 * e.printStackTrace(); } return null; }
	 */

	public BufferedReader getIs() {
		return  is;
	}

	public void setIs(BufferedReader is) {
		this.is = is;
	}

	public PrintWriter getOs() {
		return os;
	}

	public void setOs(PrintWriter os) {
		this.os = os;
	}

	public int getMyNodeID() {
		return myNodeID;
	}
	
	

	public int getMyUDPPortNo() {
		return myUDPPortNo;
	}

	public void setMyUDPPortNo(int myUDPPortNo) {
		this.myUDPPortNo = myUDPPortNo;
	}

	public void setMyNodeID(int myNodeID) {
		this.myNodeID = myNodeID;
	}

	public ReliableSocket getSocket() {
		return socket;
	}

	public void setSocket(ReliableSocket socket) {
		this.socket = socket;
	}

	public String readFromClient(int timeout) throws IOException, TimeLimitExceededException {
		// System.out.println("Reading from " + myNodeID);
		return is.readLine();
	}

	public void writeToClient(String message) {
		// System.out.println("Writing to " + myNodeID + "message : " +
		// message);
		os.println(message);
		os.flush();

	}

	@Override
	public String toString() {
		return "ClientInformation [ myNodeID = " + myNodeID + "]";
	}

	// public static void main(String[] args)
	// {
	// BullyServer bullyServer=new BullyServer(7500, "MTL");
	// bullyServer.start();
	// }

}
