package serverpackage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.*;

import net.rudp.ReliableSocket;

public class ReplicaMessageSenderHandler extends Thread {
	int portNoOfReplica;
	int nodeIDOfReplica;
	String data;
	BullyProcess parentProcess;
	public ReplicaMessageSenderHandler(int nodeIDOfReplica,int portNoOfReplica, String data, BullyProcess parentProcess){
		this.portNoOfReplica=portNoOfReplica;
		this.nodeIDOfReplica=nodeIDOfReplica;
		this.data=data;
		this.parentProcess=parentProcess;
	} 
	
	
	public void run()
	{
		
		try{
			System.out.println("newThreadToSendDataToReplica to send data to replica started. Port no of replica is "+ portNoOfReplica+" nodeID of replica is "+nodeIDOfReplica);
			ReliableSocket socket =new ReliableSocket();
			System.out.println("Trying to connect to replica");
			socket.connect(new InetSocketAddress("localhost", portNoOfReplica),3000);
			BufferedReader is= new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter os = new PrintWriter(socket.getOutputStream(), true);
			os.println(data);
			os.flush();
			String newTempResponse=is.readLine();
			System.out.println("Got response from replica "+newTempResponse);
			is.close();
			os.close();
			socket.close();
		}
		catch(SocketTimeoutException  timeoutException)
		{
			System.out.println("Exception occured while trying to connect to Replica");
			System.out.println("Removing the failed replica from list of replicas");
			parentProcess.removeReplicaInformation(this.nodeIDOfReplica);
		}
		catch(Exception ex){
			System.out.println("Exception occured while trying to connect to Replica");
		}
		
	}

}



