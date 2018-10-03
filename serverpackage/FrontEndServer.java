package serverpackage;

import helper.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.TimeLimitExceededException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.logging.Level;
import org.omg.CORBA.ORB;

import ServerOperationApp.ServerOperationIDLPOA;

public class FrontEndServer extends ServerOperationIDLPOA {
	private String ErrorMessage = "";
	Log frontEndLog;
	BullyServerV2 connectionServer;

	public FrontEndServer(Log frontEndLog) {
		this.frontEndLog = frontEndLog;
		connectionServer = new BullyServerV2(7500, "Connection Server");
		connectionServer.start();
		System.out.println("Front end and bully server started");
	}

	private ORB orb;

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	public void shutdown() {
		orb.shutdown(false);
	}

	public ClientInformation getCorrectLeader(String managerID) {
		if(connectionServer.getisAliveCheckInProgress())
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ClientInformation correctLeader = connectionServer.getLeader(managerID);
		return correctLeader;
	}

	public String createSRecord(String managerID, String firstName, String lastName, String[] coursesRegistered,
			String status, String statusDate) {
		String message = "";
		if (!validateFirstNameLastName(firstName, lastName)) {
			message = "First Name and Last Name can not be empty and can not contain special symbol";
			frontEndLog.logger.log(Level.INFO, message + " by " + managerID);
			return message;

		}
		if (!validateChangeRequest("status", status, RecordTypeEnum.Student)) {
			// Logger
			frontEndLog.logger.log(Level.INFO, this.ErrorMessage + " by " + managerID);
			return this.ErrorMessage;
		}

		ClientInformation leader = getCorrectLeader(managerID);
		if (leader != null) {
			StatusEnum tempStatus;
			if (status.equals("1")) {
				tempStatus = StatusEnum.active;
			} else {
				tempStatus = StatusEnum.inactive;
			}
			Student tempStudent = new Student("-1", firstName, lastName, Arrays.asList(coursesRegistered), tempStatus,
					statusDate);
			BullyMessagePacket packet = new BullyMessagePacket(BullyMessageEnum.RequestCreateStudentRecord);
			packet.student = tempStudent;
			packet.isLeader=true;
			String packetString = jaxbObjectToXML(packet);

			leader.writeToClient(packetString);
			try {
				message = leader.readFromClient(2000);
			} catch (TimeLimitExceededException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return message;
	}

	public String createTRecord(String managerID, String firstName, String lastName, String address, String phone,
			String specialization, String location) {

		if (!validateFirstNameLastName(firstName, lastName)) {
			String message = "First Name and Last Name can not be empty and can not contain special symbol";
			frontEndLog.logger.log(Level.INFO, message + " by " + managerID);
			return message;

		}
		if (!validateChangeRequest("phone", phone, RecordTypeEnum.Teacher)) {
			String tempErrorMessage = this.ErrorMessage;
			if (!validateChangeRequest("location", location, RecordTypeEnum.Teacher)) {
				tempErrorMessage += ". " + this.ErrorMessage + ".";
			}
			frontEndLog.logger.log(Level.WARNING, tempErrorMessage + " by " + managerID);
			return tempErrorMessage;
		} else if (!validateChangeRequest("location", location, RecordTypeEnum.Teacher)) {
			frontEndLog.logger.log(Level.WARNING, this.ErrorMessage + " by " + managerID);
			return this.ErrorMessage;
		}

		ClientInformation leader = getCorrectLeader(managerID);
		String message = "";
		
		if(leader==null){
			throw new RuntimeException("Leader null");
		}
		if (leader != null) {
			Teacher tempTeacher = new Teacher("-1", firstName, lastName, address, phone, specialization, location);
			BullyMessagePacket packet = new BullyMessagePacket(BullyMessageEnum.RequestCreateTecherRecord);
			packet.teacher = tempTeacher;
			
			packet.isLeader=true;
			String packetString = jaxbObjectToXML(packet);
            System.out.println("FE: "+packetString);
            System.out.println(leader.getMyNodeID());
			leader.writeToClient(packetString);
			try {
				message = leader.readFromClient(2000);
			} catch (TimeLimitExceededException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return message;
	}

	public String editRecord(String managerID, String recordID, String fieldName, String newValue) {
		// Validation

		ClientInformation leader = getCorrectLeader(managerID);
		String message = "";

		EditRecord tempEdit = new EditRecord(recordID, fieldName, newValue);
		BullyMessagePacket packet = new BullyMessagePacket(BullyMessageEnum.RequestEditRecord);
		packet.editRecord = tempEdit;
		packet.isLeader=true;
		String packetString = jaxbObjectToXML(packet);

		leader.writeToClient(packetString);
		try {
			message = leader.readFromClient(2000);
		} catch (TimeLimitExceededException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return message;
	}

	public String getRecordCounts(String managerID) {
		ClientInformation leader = getCorrectLeader(managerID);
		ClientInformation anotherleader1 = null;
		ClientInformation anotherleader2 = null;
		if (managerID.substring(0, 3).equalsIgnoreCase("mtl")) {
			anotherleader1 = getCorrectLeader("lvl");
			anotherleader2 = getCorrectLeader("ddo");
		} else if (managerID.substring(0, 3).equalsIgnoreCase("lvl")) {
			anotherleader1 = getCorrectLeader("mtl");
			anotherleader2 = getCorrectLeader("ddo");
		} else if (managerID.substring(0, 3).equalsIgnoreCase("ddo")) {
			anotherleader1 = getCorrectLeader("mtl");
			anotherleader2 = getCorrectLeader("lvl");
		}

		String message = "";
		BullyMessagePacket packet = new BullyMessagePacket(BullyMessageEnum.RequestGetRecordCount);
		String packetString = jaxbObjectToXML(packet);
		if (leader != null) {
			leader.writeToClient(packetString);
		}
		if (anotherleader1 != null) {
			anotherleader1.writeToClient(packetString);
		}
		if (anotherleader2 != null) {
			anotherleader2.writeToClient(packetString);
		}

		try {
			if (leader != null) {
				message = leader.readFromClient(2000);
			}
			if (anotherleader1 != null) {
				message +=" "+ anotherleader1.readFromClient(2000);
			}
			if (anotherleader2 != null) {
				message +=" "+ anotherleader2.readFromClient(2000);
			}
		} catch (TimeLimitExceededException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return message;
	}

	public String transferRecord(String managerID, String recordID, String transferServerName) {
		// Validation

		ClientInformation leader = getCorrectLeader(managerID);
		String message = "";
		ClientInformation transferLeader= getCorrectLeader(transferServerName);
		if(transferLeader==null){
			return "Invalid transfer server name";
		}
		TransferRecord tempTransfer = new TransferRecord(recordID, transferServerName);
		tempTransfer.setPortNoOfLeaderOfAnotherBullyProcess(transferLeader.getMyUDPPortNo());
		BullyMessagePacket packet = new BullyMessagePacket(BullyMessageEnum.RequestTransferRecord);
		packet.transferRecord = tempTransfer;
		packet.isLeader=true;
		packet.managerID=managerID;
		String packetString = jaxbObjectToXML(packet);

		leader.writeToClient(packetString);
		try {
			message = leader.readFromClient(2000);
		} catch (TimeLimitExceededException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return message;
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
						int i = Integer.parseInt(newValue);
						if (i != 1 && i != 2) {
							this.ErrorMessage = "Incorrect input for status. For active type 1 and for inactive type 2";
							return false;
						}
					} catch (Exception ex) {
						this.ErrorMessage = "Incorrect input for status. For active type 1 and for inactive type 2";
						return false;
					}
				}
				return true;
			}

		}
		this.ErrorMessage = "Either there is no field with " + fieldName + " or it can not be changed";
		return false;
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
			xmlString = xmlString.replace("\n", "").replace("\r", "");

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return xmlString;

	}

}
