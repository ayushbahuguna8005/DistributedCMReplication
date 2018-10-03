package serverpackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.rudp.ReliableSocket;

public class ProcessThread extends Thread{

	ReliableSocket socket;
	int nodeID;
	BufferedReader is;
	PrintWriter os;
	
	public ProcessThread(ReliableSocket socket, int nodeID) {
		this.nodeID = nodeID;
		this.socket = socket;
		try {
			is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			os = new PrintWriter(socket.getOutputStream(),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			nodeID = Integer.parseInt(is.readLine());
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	
	}
}
