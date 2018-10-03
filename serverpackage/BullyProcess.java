package serverpackage;

import helper.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.rudp.ReliableSocket;

public class BullyProcess {

	ReliableSocket socket;
	BufferedReader is;
	PrintWriter os;
	public String serverName;
	private String ErrorMessage = "";
	private HashMap<String, HashMap<String, java.lang.Object>> data = new HashMap<String, HashMap<String, java.lang.Object>>();
	private RandomNumberUtility randomNumberUtility = new RandomNumberUtility();
	List<Integer> udpPortOfOtherServers = null;
	HashMap<String, Integer> udpPortOfServersWithName = null;
	Log log;
	LockingUtility lockingUtility = new LockingUtility();
	int nodeID;
	String ALIVE = "alive";
	boolean stop = false;
	int udpPort = 0;

	int teacherRecordCounter = 10000;
	int studentRecordCount = 10000;

	HashMap<Integer, Integer> portDetailsOfOtherReplicas = new HashMap<>();

	public BullyProcess(ReliableSocket socket, int ID, String serverName, Integer serverPort, int udpPort, Log log,
			HashMap<Integer, Integer> portDetailsOfOtherReplicas) throws IOException {
		this.socket = socket;
		System.out.println(socket.getPort());
		System.out.println("Trying to connect to localhost " + serverPort);
		socket.connect(new InetSocketAddress("localhost", serverPort));
		System.out.println("Successfully connected");
		this.nodeID = ID;
		this.log = log;
		this.serverName = serverName;
		this.udpPort = udpPort;
		this.portDetailsOfOtherReplicas = portDetailsOfOtherReplicas;

		try {
			is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			os = new PrintWriter(socket.getOutputStream(), true);

			startReliableUDPPort(udpPort, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUDPPortOfOtherServers(List<Integer> udpPortOfOtherServers) {
		this.udpPortOfOtherServers = udpPortOfOtherServers;
	}

	public void setudpPortOfOtherServersWithName(HashMap<String, Integer> udpPortOfServersWithName) {
		this.udpPortOfServersWithName = udpPortOfServersWithName;
	}

	public String createSRecord(String managerID, String firstName, String lastName, String[] coursesRegistered,
			String status, String statusDate) {
		String key;
		String recordID;
		if (!validateFirstNameLastName(firstName, lastName)) {
			String message = "First Name and Last Name can not be empty and can not contain special symbol";
			log.logger.log(Level.INFO, message + " by " + managerID);
			return message;

		}
		if (!validateChangeRequest("status", status, RecordTypeEnum.Student)) {
			// Logger
			log.logger.log(Level.INFO, this.ErrorMessage + " by " + managerID);
			return this.ErrorMessage;
		} else {
			synchronized (this) {
				key = lastName.substring(0, 1);
				// recordID = "SR" +
				// randomNumberUtility.getUniqueRandomNumber();
				recordID = "SR" + studentRecordCount;
				studentRecordCount++;
				int i = -1;
				if (status.equalsIgnoreCase(StatusEnum.active.toString())) {
					i = 1;
				} else if (status.equalsIgnoreCase(StatusEnum.inactive.toString())) {
					i = 2;
				}

				StatusEnum statusEnum = StatusEnum.values()[i - 1];
				Student record = new Student(recordID, firstName, lastName, Arrays.asList(coursesRegistered),
						statusEnum, statusDate);

				if (data.containsKey(key)) {
					data.get(key).put(recordID, record);
				} else {
					HashMap<String, Object> value = new HashMap<>();
					value.put(recordID, record);
					data.put(key, value);
				}
			}
		}
		// Logger
		log.logger.log(Level.INFO, ErrorMessages.getSuccessStudentInsertRecord(recordID) + " by " + managerID);
		return ErrorMessages.getSuccessStudentInsertRecord(recordID);
	}

	public String createTRecord(String managerID, String firstName, String lastName, String address, String phone,
			String specialization, String location) {
		String key;
		String recordID;
		if (!validateFirstNameLastName(firstName, lastName)) {
			String message = "First Name and Last Name can not be empty and can not contain special symbol";
			log.logger.log(Level.INFO, message + " by " + managerID);
			return message;

		}
		if (!validateChangeRequest("phone", phone, RecordTypeEnum.Teacher)) {
			String tempErrorMessage = this.ErrorMessage;
			if (!validateChangeRequest("location", location, RecordTypeEnum.Teacher)) {
				tempErrorMessage += ". " + this.ErrorMessage + ".";
			}
			log.logger.log(Level.WARNING, tempErrorMessage + " by " + managerID);
			return tempErrorMessage;
		} else if (!validateChangeRequest("location", location, RecordTypeEnum.Teacher)) {
			log.logger.log(Level.WARNING, this.ErrorMessage + " by " + managerID);
			return this.ErrorMessage;
		}
		synchronized (this) {
			key = lastName.substring(0, 1);
			// recordID = "TR" + randomNumberUtility.getUniqueRandomNumber();
			recordID = "TR" + teacherRecordCounter;
			teacherRecordCounter++;
			Teacher record = new Teacher(recordID, firstName, lastName, address, phone, specialization, location);
			if (data.containsKey(key)) {
				data.get(key).put(recordID, record);
			} else {
				HashMap<String, Object> value = new HashMap<>();
				value.put(recordID, record);
				data.put(key, value);
			}
		}

		// Logger
		log.logger.log(Level.INFO, ErrorMessages.getSuccessTeacherInsertRecord(recordID) + " by " + managerID);
		return ErrorMessages.getSuccessTeacherInsertRecord(recordID);
	}

	public String editRecord(String managerID, String recordID, String fieldName, String newValue) {
		this.ErrorMessage = "";
		Object o = null;
		fieldName = fieldName.toLowerCase().trim();
		o = getRecord(recordID);
		if (o != null) {
			if (lockingUtility.isRecordLocked(recordID)) {
				String errorMessage = "";
				// What if when we try to find the locking reason and at that
				// same time some process removes the recordID from the
				// lockedData list
				LockReason reason = lockingUtility.getLockingReason(recordID);
				if (reason == null) {
					// throw new RuntimeException("LockReason is null");
				} else if (reason == LockReason.CurrentlyBeingEdited) {
					errorMessage = "The record (" + recordID + ") can not be edited as it is currently being edited.";
				} else if (reason == LockReason.CurrentlyBeingTransfered) {
					errorMessage = "The record (" + recordID
							+ ") can not be edited as it is currently being transfered.";
				}

				log.logger.log(Level.INFO, errorMessage + " by " + managerID);
				return errorMessage;
			} else {
				lockingUtility.addToLockList(recordID, LockReason.CurrentlyBeingEdited);

				/*
				 * try { Thread.sleep(2000); } catch (InterruptedException e1) {
				 * e1.printStackTrace(); }
				 */

				if (recordID.toLowerCase().startsWith("tr")) {

					synchronized (this) {
						Teacher obj = (Teacher) o;
						if (validateChangeRequest(fieldName, newValue, RecordTypeEnum.Teacher)) {
							if (fieldName.equals("address")) {
								obj.address = newValue;
							} else if (fieldName.equals("phone")) {
								obj.phone = newValue;
							} else if (fieldName.equals("location")) {
								obj.location = ServerNameEnum.valueOf(newValue);
							}
							this.ErrorMessage = "Successfully update the field " + fieldName + " with the new value "
									+ newValue;
						}
					}
				} else if (recordID.toLowerCase().startsWith("sr")) {
					synchronized (this) {
						Student obj = (Student) o;
						if (validateChangeRequest(fieldName, newValue, RecordTypeEnum.Student)) {
							if (fieldName.equals("status")) {
								StatusEnum prev = obj.status;
								obj.status = StatusEnum.values()[Integer.parseInt(newValue) - 1];
								if (obj.status != prev) {
									DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
									LocalDateTime now = LocalDateTime.now();
									String statusDate = dtf.format(now);
									obj.statusDate = statusDate;
								}
							} else if (fieldName.equals("courses")) {
								obj.coursesRegistered.add(newValue);

							}
							this.ErrorMessage = "Successfully update the field " + fieldName + " with the new value "
									+ newValue;
						}
					}

				}
				this.ErrorMessage += " for record Id: " + recordID;
				log.logger.log(Level.INFO, this.ErrorMessage + " by " + managerID);
				lockingUtility.removeFromLockList(recordID);
				return this.ErrorMessage;

			}

		} else {
			log.logger.log(Level.INFO, "No record found with record id:" + recordID + " by " + managerID);
			return "No record found with record id: " + recordID;
		}
	}

	public String newGetRecordCounts(String managerID) {
		String count = getShortFormOfServerName(serverName) + " " + calculateNumberOfRecords();
		;

		return count;
	}

	public String getRecordCounts(String managerID) {
		String recordCountWithNameOfServer = getShortFormOfServerName(serverName) + " " + calculateNumberOfRecords();
		List<CompletableFuture<String>> futureReplyFromServers = new ArrayList<>();
		try {
			for (Integer udpport : udpPortOfOtherServers) {
				UDPMessage message = new UDPMessage(MessageType.RecordCount, null);
				UDPRequestSender udpRequest = new UDPRequestSender(message, udpport,
						InetAddress.getByName("localhost"));
				futureReplyFromServers.add(CompletableFuture.supplyAsync(() -> {
					try {
						udpRequest.sendRequest();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return udpRequest.getrecordCountWithNameOfOtherServer();
				}));
			}
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
			log.logger.log(Level.WARNING, "Socket exception occured by " + managerID);
		} catch (Exception ex) {
			log.logger.log(Level.WARNING, "Exception occured by " + managerID);
			System.out.println(ex.getMessage());
		}
		for (CompletableFuture<String> result : futureReplyFromServers) {
			try {
				recordCountWithNameOfServer += " " + result.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		log.logger.log(Level.INFO,
				"Record Count with Server Names : " + recordCountWithNameOfServer + " by " + managerID);
		return recordCountWithNameOfServer;
	}

	/*
	 * public synchronized boolean deleteRecord(String recordID) {
	 * 
	 * HashMap<String, Object> toBeRemoved = new HashMap<>();
	 * Iterator<Entry<String, HashMap<String, Object>>> entryIt =
	 * data.entrySet().iterator(); boolean recordDeleted = false; while
	 * (entryIt.hasNext()) { Entry<String, HashMap<String, Object>> next =
	 * entryIt.next(); if (next.getValue().containsKey(recordID)) { toBeRemoved
	 * = next.getValue(); break; } } try { Iterator<Entry<String, Object>>
	 * iterator = toBeRemoved.entrySet().iterator(); while (iterator.hasNext())
	 * { Entry<String, Object> rec = iterator.next(); if
	 * (rec.getKey().equals(recordID)) { iterator.remove(); recordDeleted =
	 * true; } } return recordDeleted; } catch (Exception e) {
	 * e.printStackTrace();
	 * System.out.println("Runtime error occured during deletion of record");
	 * return recordDeleted; } }
	 */

	public synchronized boolean deleteRecord(String recordID) {

		HashMap<String, Object> toBeRemoved = new HashMap<>();
		Iterator<Entry<String, HashMap<String, Object>>> entryIt = data.entrySet().iterator();
		recordID = recordID.toUpperCase();
		boolean recordDeleted = false;
		while (entryIt.hasNext()) {
			Entry<String, HashMap<String, Object>> next = entryIt.next();
			if (next.getValue().containsKey(recordID)) {
				toBeRemoved = next.getValue();
				break;
			}
		}
		try {
			Iterator<Entry<String, Object>> iterator = toBeRemoved.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> rec = iterator.next();
				if (rec.getKey().equals(recordID)) {
					iterator.remove();
					recordDeleted = true;
				}
			}
			return recordDeleted;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Runtime error occured during deletion of record");
			System.out.println("IS   RECORD DELETE " + recordDeleted);
			return recordDeleted;
		}
	}

	public String transferRecord(String managerID, String recordID, String transferServerName,
			int portNoOfLeaderOfAnotherBullyProcess) {
		Validation valid = new Validation();
		Object obj = getRecord(recordID);

		if (valid.isRecordPresentInTheServer(obj)) {
			if (!valid.doesManagerBelongToSameServer(managerID, transferServerName)) {
				if (valid.isTransferServerNameValid(transferServerName)) {

					if (lockingUtility.isRecordLocked(recordID)) {
						String errorMessage = "";
						// What if when we try to find the locking reason and at
						// that
						// same time some process removes the recordID from the
						// lockedData list
						LockReason reason = lockingUtility.getLockingReason(recordID);
						if (reason == null) {
							throw new RuntimeException("LockReason is null");
						} else if (reason == LockReason.CurrentlyBeingEdited) {
							errorMessage = "The record (" + recordID
									+ ") can not be transferred as it is currently being edited.";
						} else if (reason == LockReason.CurrentlyBeingTransfered) {
							errorMessage = "The record (" + recordID
									+ ") can not be transferred as it is currently being transfered.";
						}

						log.logger.log(Level.INFO, errorMessage + " by " + managerID);
						return errorMessage;
					}

					lockingUtility.addToLockList(recordID, LockReason.CurrentlyBeingTransfered);

					BullyMessagePacket packet = new BullyMessagePacket();
					if (obj instanceof Student) {
						packet.messageType = BullyMessageEnum.RequestCreateStudentRecord;
						packet.student = (Student) obj;
					} else if (obj instanceof Teacher) {
						packet.messageType = BullyMessageEnum.RequestCreateTecherRecord;
						packet.teacher = (Teacher) obj;
					}
					packet.isLeader = true;
					String packetString = jaxbObjectToXML(packet);
					String replyFromDifferentLeader = "";
					try {
						ReliableSocket socket = new ReliableSocket();
						socket.connect(new InetSocketAddress("localhost", portNoOfLeaderOfAnotherBullyProcess));
						BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						PrintWriter os = new PrintWriter(socket.getOutputStream(), true);
						os.println(packetString);
						os.flush();
						replyFromDifferentLeader = is.readLine();
						try {
							is.close();
							os.close();
							socket.close();
						} catch (IOException ex) {
							System.out.println("Issue in closing socket");
						}
						if (replyFromDifferentLeader.indexOf("successfully") != -1) {
							deleteRecord(recordID);
							BullyMessagePacket responsePacket = new BullyMessagePacket(
									BullyMessageEnum.RequestDeleteRecord);
							responsePacket.recordIDToDelete = recordID;

							for (Integer nodeID : portDetailsOfOtherReplicas.keySet()) {
								int portNoOfReplica = portDetailsOfOtherReplicas.get(nodeID);
								responsePacket.isLeader = false;
								String data = jaxbObjectToXML(responsePacket);
								ReplicaMessageSenderHandler replicaMessageSenderHandler = new ReplicaMessageSenderHandler(
										nodeID, portNoOfReplica, data, this);
								replicaMessageSenderHandler.start();
							}
							lockingUtility.removeFromLockList(recordID);
							log.logger.log(Level.INFO, "Record with record id " + recordID
									+ " successfully deleted as a result of transfer" + " by " + managerID);
							log.logger.log(Level.INFO, replyFromDifferentLeader + " by " + managerID);

						}

					} catch (Exception ex) {
						System.out.println("Exception occured here");
						return "RecordID" + recordID + " could not be transfered due to server error";
					}
					return "RecordID " + recordID + " successfully transfered";
				} else {
					return ErrorMessages.getNoSuchServerError(managerID, transferServerName, log);
				}
			} else {
				return ErrorMessages.getSameLocationError(managerID, recordID, transferServerName, log);
			}
		} else {
			return ErrorMessages.getNoRecordFoundError(managerID, recordID, serverName, log);
		}

	}

	public void startUDPPort(int udpPort, BullyProcess process) {
		UDPServer udpServer = new UDPServer(udpPort, this);
		udpServer.start();
	}

	public void startReliableUDPPort(int udpPort, BullyProcess process) {
		ReliableUDPPortUDPServer udpServer = new ReliableUDPPortUDPServer(udpPort, this);
		udpServer.start();
	}

	public String saveTransferObjectAtServer(Object o) {
		String key = "";
		String recordID = "";
		String tempRecordID = "";
		if (o instanceof Teacher) {
			Teacher t = (Teacher) o;
			key = t.lastName.substring(0, 1);
			recordID = t.recordID;
			tempRecordID = recordID;
			int numberPartOfRecordID = Integer.parseInt(recordID.substring(2));
			if (randomNumberUtility.isRandomNumberIsAlreadyPresent(numberPartOfRecordID)) {
				// create a new random number
				String newRecordID = "TR" + randomNumberUtility.getUniqueRandomNumber();
				recordID = newRecordID;
				t.recordID = newRecordID;
			}

		} else if (o instanceof Student) {
			Student s = (Student) o;
			key = s.lastName.substring(0, 1);
			recordID = s.recordID;
			tempRecordID = recordID;
			int numberPartOfRecordID = Integer.parseInt(recordID.substring(2));
			if (randomNumberUtility.isRandomNumberIsAlreadyPresent(numberPartOfRecordID)) {
				// create a new random number
				String newRecordID = "SR" + randomNumberUtility.getUniqueRandomNumber();
				recordID = newRecordID;
				s.recordID = newRecordID;
			}
		}

		String shortServerName = getShortFormOfServerName(this.serverName);
		if (isTransferObjectSuccessfulyStored(key, recordID, o)) {
			if (tempRecordID.equals(recordID)) {
				return ErrorMessages.getSuccessTransferMessage(recordID, shortServerName, log);
			} else {
				return ErrorMessages.getSuccessTransferMessageWithNewRecordID(recordID, tempRecordID, shortServerName,
						log);
			}
		}

		return ErrorMessages.getFailTransferMessage(tempRecordID, shortServerName, log);
	}

	public String getShortFormOfServerName(String serverName) {
		String shortName = "";
		if (serverName.equals("Montreal") || serverName.equalsIgnoreCase("mtl")) {
			shortName = "MTL";
		} else if (serverName.equals("Laval") || serverName.equalsIgnoreCase("lvl")) {
			shortName = "LVL";
		} else if (serverName.equals("Dollard-des-Ormeaux") || serverName.equalsIgnoreCase("ddo")) {
			shortName = "DDO";
		}
		return shortName;
	}

	public synchronized boolean isTransferObjectSuccessfulyStored(String key, String recordID, Object record) {
		try {
			if (data.containsKey(key)) {
				data.get(key).put(recordID, record);
			} else {
				HashMap<String, Object> value = new HashMap<>();
				value.put(recordID, record);
				data.put(key, value);
			}
		} catch (Exception ex) {
			System.out.println("Exception occured while trying to do save transfer object");
			return false;
		}

		return true;
	}

	int calculateNumberOfRecords() {
		int numberOfRecords = 0;
		for (Map.Entry<String, HashMap<String, java.lang.Object>> e : data.entrySet()) {
			numberOfRecords += e.getValue().size();
		}
		return numberOfRecords;
	}

	public boolean validateFirstNameLastName(String firstName, String lastName) {
		if ((firstName != null && !firstName.isEmpty()) && (lastName != null && !lastName.isEmpty())) {
			firstName = firstName.trim();
			lastName = lastName.trim();
			Pattern p = Pattern.compile("[^a-z]", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(firstName);
			boolean isfirstNotNameValid = m.find();
			m = p.matcher(lastName);
			boolean islastNotNameValid = m.find();
			if (isfirstNotNameValid || islastNotNameValid)
				return false;
		} else {
			return false;
		}
		return true;
	}

	private boolean validateChangeRequest(String fieldName, String newValue, RecordTypeEnum recordType) {
		fieldName = fieldName.trim();
		newValue = newValue.trim();
		if (recordType == RecordTypeEnum.Teacher) {
			if (fieldName.equals("address") || fieldName.equals("phone") || fieldName.equals("location")) {
				if (fieldName.equals("phone")) {
					try {
						Long.parseLong(newValue);
						if (newValue.length() == 10) {
							return true;
						} else {
							this.ErrorMessage = "Phone number must be 10 digit number";
							return false;
						}

					} catch (NumberFormatException ex) {
						this.ErrorMessage = "Phone number must be 10 digit number";
						return false;
					}

				} else if (fieldName.equals("location")) {
					try {
						ServerNameEnum.valueOf(newValue);
					} catch (IllegalArgumentException ex) {
						this.ErrorMessage = "Location can only be mtl, lvl or ddo";
						return false;
					}
				}
				return true;
			}
		} else if (recordType == RecordTypeEnum.Student) {
			if (fieldName.equals("course") || fieldName.equals("status")) {
				if (fieldName.equals("status")) {
					try {
						int i = -1;
						if (newValue.equalsIgnoreCase(StatusEnum.active.toString())) {
							i = 1;
						} else if (newValue.equalsIgnoreCase(StatusEnum.inactive.toString())) {
							i = 2;
						}

						System.out.println(i);
						if (i != 1 && i != 2) {
							this.ErrorMessage = "Incorrect input for status. For active type 1 and for inactive type 2";
							return false;
						}
					} catch (Exception ex) {
						System.out.println("Number format excpetion");

						this.ErrorMessage = newValue
								+ " Incorrect input for status xx. For active type 1 and for inactive type 2";
						return false;
					}
				}
				return true;
			}

		}
		this.ErrorMessage = "Either there is no field with " + fieldName + " or it can not be changed";
		return false;
	}

	private synchronized Object getRecord(String recordID) {
		Object o = null;
		for (Map.Entry<String, HashMap<String, Object>> entry : data.entrySet()) {
			for (Map.Entry<String, Object> innerEntry : entry.getValue().entrySet()) {
				if (innerEntry.getKey().equalsIgnoreCase(recordID)) {
					o = innerEntry.getValue();
					break;
				}
			}
			if (o != null) {
				break;
			}
		}
		return o;
	}

	public void run1() {

		try {
			String nodeIDandServerID = nodeID + ":" + serverName + ":" + udpPort;
			System.out.println("Writing node info  " + nodeIDandServerID);
			os.println(nodeIDandServerID);
			os.flush();
			System.out.println("Wrote node info  " + nodeIDandServerID);
			int count, progress = 0;
			String response;
			String requestResult;
			BullyMessagePacket responsePacket;
			while (true) {
				response = is.readLine();
				System.out.println("Received new message");
				System.out.println("To unmarshall : " + response);
				responsePacket = unmarshal1(response);
				if (responsePacket.messageType == BullyMessageEnum.Heartbeat) {
					os.println(response);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestCreateTecherRecord) {
					Teacher tempTeacher = responsePacket.getTeacher();
					if (responsePacket.isLeader) { // responsePacket.replicaInfo
													// != null
						System.out.println("Got here. About to send data to replica");
						for (Integer nodeID : portDetailsOfOtherReplicas.keySet()) {
							int portNoOfReplica = portDetailsOfOtherReplicas.get(nodeID);
							responsePacket.isLeader = false;
							String data = jaxbObjectToXML(responsePacket);
							ReplicaMessageSenderHandler replicaMessageSenderHandler = new ReplicaMessageSenderHandler(
									nodeID, portNoOfReplica, data, this);
							replicaMessageSenderHandler.start();
						}

					}

					requestResult = this.createTRecord(responsePacket.managerID, tempTeacher.firstName,
							tempTeacher.lastName, tempTeacher.address, tempTeacher.phone, tempTeacher.specialization,
							tempTeacher.location.toString());
					System.out.println("Data after createTRecord : " + this.data);
					os.println(requestResult);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestCreateStudentRecord) {
					Student tempStudent = responsePacket.student;

					if (responsePacket.isLeader) { // responsePacket.replicaInfo
													// != null
						System.out.println("Got here. About to send data to replica");
						for (Integer nodeID : portDetailsOfOtherReplicas.keySet()) {
							int portNoOfReplica = portDetailsOfOtherReplicas.get(nodeID);
							responsePacket.isLeader = false;
							String data = jaxbObjectToXML(responsePacket);
							ReplicaMessageSenderHandler replicaMessageSenderHandler = new ReplicaMessageSenderHandler(
									nodeID, portNoOfReplica, data, this);
							replicaMessageSenderHandler.start();
						}
					}

					String tempCourses[] = new String[tempStudent.coursesRegistered.size()];
					tempCourses = tempStudent.coursesRegistered.toArray(tempCourses);
					requestResult = this.createSRecord(responsePacket.managerID, tempStudent.firstName,
							tempStudent.lastName, tempCourses, tempStudent.status.toString(), tempStudent.statusDate);
					System.out.println("Data after createSRecord : " + this.data);
					os.println(requestResult);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestTransferRecord) {
					TransferRecord tempTransfer = responsePacket.transferRecord;
					requestResult = this.transferRecord(responsePacket.managerID, tempTransfer.recordID,
							tempTransfer.serverName, tempTransfer.getPortNoOfLeaderOfAnotherBullyProcess());
					System.out.println("Data after TransferRecord : " + this.data);
					os.println(requestResult);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestEditRecord) {
					EditRecord tempEdit = responsePacket.editRecord;
					if (responsePacket.isLeader) { // responsePacket.replicaInfo
						// != null
						System.out.println("Got here. About to send data to replica");
						for (Integer nodeID : portDetailsOfOtherReplicas.keySet()) {
							int portNoOfReplica = portDetailsOfOtherReplicas.get(nodeID);
							responsePacket.isLeader = false;
							String data = jaxbObjectToXML(responsePacket);
							ReplicaMessageSenderHandler replicaMessageSenderHandler = new ReplicaMessageSenderHandler(
									nodeID, portNoOfReplica, data, this);
							replicaMessageSenderHandler.start();
						}

					}
					requestResult = this.editRecord(responsePacket.managerID, tempEdit.recordID, tempEdit.fieldName,
							tempEdit.newValue);
					System.out.println("Data after EditRecord : " + this.data);
					os.println(requestResult);
					os.flush();
				} else if (responsePacket.messageType == BullyMessageEnum.RequestGetRecordCount) {

					requestResult = this.newGetRecordCounts(responsePacket.managerID);
					System.out.println("Data after EditRecord : " + this.data);
					os.println(requestResult);
					os.flush();
				}

				if (stop) {
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("Exiting process: " + nodeID + " and closing the socket");
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static String jaxbObjectToXML(BullyMessagePacket packet) {
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
			xmlString = xmlString.replace("\n", "").replace("\r", "");

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return xmlString;

	}

	// If anything is changed here remember to change this in
	// ReliableUDPPortUDPServer
	public static BullyMessagePacket unmarshal1(String xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(BullyMessagePacket.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		StringReader reader = new StringReader(xml);

		BullyMessagePacket packet = (BullyMessagePacket) jaxbUnmarshaller.unmarshal(reader);

		return packet;
	}

	public synchronized void removeReplicaInformation(int nodeIDOfReplicaThatFailed) {
		portDetailsOfOtherReplicas.remove(nodeIDOfReplicaThatFailed);
	}

}
