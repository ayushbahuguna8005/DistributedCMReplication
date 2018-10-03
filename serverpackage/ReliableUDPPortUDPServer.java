package serverpackage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.rudp.ReliableServerSocket;
import net.rudp.ReliableSocket;

public class ReliableUDPPortUDPServer extends Thread {
	int udpPort;
	BullyProcess process;

	public ReliableUDPPortUDPServer(int udpPort, BullyProcess process) {
		this.udpPort = udpPort;
		this.process = process;
	}

	public void run() {
		try {
			ReliableServerSocket serverSocket = new ReliableServerSocket(udpPort);
			System.out.println("UDP Thread started at port " + udpPort + " for server " + process.serverName);
			ReliableSocket socket = null;
			String response = "";
			String requestResult;
			BullyMessagePacket responsePacket;
			while (true) {
				socket = (ReliableSocket) serverSocket.accept();
				System.out.println("Leader successfully connected");
				BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter os = new PrintWriter(socket.getOutputStream(), true);
				System.out.println("Waiting for new message at BullyProcessThread");
				response = is.readLine();
				System.out.println("Received new message");
				responsePacket = unmarshal1(response);
				System.out.println("Unmarshalled the request UDP. Message type "+responsePacket.messageType);
				// System.out.println("Response from server : " + response + "
				// on process ID: " + nodeID);
				if (responsePacket.messageType == BullyMessageEnum.Heartbeat) {
					os.println(response);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestCreateTecherRecord) {
					Teacher tempTeacher = responsePacket.getTeacher();
					if (responsePacket.isLeader) { // responsePacket.replicaInfo
						// != null
						System.out.println("Got here. About to send data to replica");
						for (Integer nodeID : process.portDetailsOfOtherReplicas.keySet()) {
							int portNoOfReplica = process.portDetailsOfOtherReplicas.get(nodeID);
							responsePacket.isLeader = false;
							String data = process.jaxbObjectToXML(responsePacket);
							ReplicaMessageSenderHandler replicaMessageSenderHandler = new ReplicaMessageSenderHandler(
									nodeID, portNoOfReplica, data, process);
							replicaMessageSenderHandler.start();
						}

					}
					requestResult = process.createTRecord(responsePacket.managerID, tempTeacher.firstName,
							tempTeacher.lastName, tempTeacher.address, tempTeacher.phone, tempTeacher.specialization,
							tempTeacher.location.toString());
					System.out.println("Inside ReliableUDP thread " + requestResult);
					os.println(requestResult);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestCreateStudentRecord) {
					Student tempStudent = responsePacket.student;
					if (responsePacket.isLeader) { // responsePacket.replicaInfo
						// != null
						System.out.println("Got here. About to send data to replica");
						for (Integer nodeID : process.portDetailsOfOtherReplicas.keySet()) {
							int portNoOfReplica = process.portDetailsOfOtherReplicas.get(nodeID);
							responsePacket.isLeader = false;
							String data = process.jaxbObjectToXML(responsePacket);
							ReplicaMessageSenderHandler replicaMessageSenderHandler = new ReplicaMessageSenderHandler(
									nodeID, portNoOfReplica, data, process);
							replicaMessageSenderHandler.start();
						}

					}
					String tempCourses[] = new String[tempStudent.coursesRegistered.size()];
					tempCourses = tempStudent.coursesRegistered.toArray(tempCourses);
					if (responsePacket.isLeader) { // responsePacket.replicaInfo
													// != null
						// TODO: Write code to send data to replicas.

						// A small thread that keeps track of the request to the
						// replica. If reply is not recieved remove
						// the replica from its list

					}
					requestResult = process.createSRecord(responsePacket.managerID, tempStudent.firstName,
							tempStudent.lastName, tempCourses, tempStudent.status.toString(), tempStudent.statusDate);
					System.out.println("Inside ReliableUDP thread " + requestResult);
					os.println(requestResult);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestEditRecord) {
					EditRecord tempEdit = responsePacket.editRecord;
					if (responsePacket.isLeader) { // responsePacket.replicaInfo
													// != null
						// TODO: Write code to send data to replicas

					}
					requestResult = process.editRecord(responsePacket.managerID, tempEdit.recordID, tempEdit.fieldName,
							tempEdit.newValue);
					System.out.println("Inside ReliableUDP thread " + requestResult);
					os.println(requestResult);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestGetRecordCount) {

					requestResult = process.newGetRecordCounts(responsePacket.managerID);
					os.println(requestResult);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestDeleteRecord) {
					requestResult = process.deleteRecord(responsePacket.recordIDToDelete)
							? "RecordId " + responsePacket.recordIDToDelete + " successfully deleted"
							: "RecordId " + responsePacket.recordIDToDelete + " failed to delete";
					os.println(requestResult);
					os.flush();
				}

				is.close();
				os.close();
				socket.close();

			}

		} catch (Exception ex) {
			System.out.println("Exception occured here ReliableUDPPortUDPServer");
		}
	}

	// If anything is changed here remember to change this in
	// ReliableUDPPortUDPServer
	public static BullyMessagePacket unmarshal1(String xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(BullyMessagePacket.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		StringReader reader = new StringReader(xml);

		BullyMessagePacket packet = (BullyMessagePacket) jaxbUnmarshaller.unmarshal(reader);

		// System.out.println("Bully packet : ");
		// System.out.println(packet);
		return packet;
	}

}
