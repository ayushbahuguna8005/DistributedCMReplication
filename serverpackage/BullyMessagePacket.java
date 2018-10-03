package serverpackage;

import java.io.Serializable;
import java.util.NavigableMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BullyMessagePacket")
public class BullyMessagePacket implements Serializable {

	String managerID;
	BullyMessageEnum messageType;
	int nodeID;
	String recordIDToDelete;
	
	@XmlElement(name = "recordIDToDelete")
	public String getRecordIDToDelete() {
		return recordIDToDelete;
	}

	public void setRecordIDToDelete(String recordIDToDelete) {
		this.recordIDToDelete = recordIDToDelete;
	}

	public void setLeader(boolean isLeader) {
		this.isLeader = isLeader;
	}

	Teacher teacher;
	Student student;
	TransferRecord transferRecord;
	EditRecord editRecord;
	//NavigableMap<Integer, NewClient> replicaInfo;
	boolean isLeader;
	//NavigableMap<Integer, ClientInformation> replicaInfo;

//	@XmlElement(name = "replicaInfo")
//	public NavigableMap<Integer, NewClient> getClientInfo() {
//		return replicaInfo;
//	}
//
//	public void setClientInfo(NavigableMap<Integer, NewClient> replicaInfo) {
//		this.replicaInfo = replicaInfo;
//	}

	@XmlElement(name = "messageType")
	public BullyMessageEnum getMessageType() {
		return messageType;
	}

	@XmlElement(name = "managerID")
	public String getManagerID() {
		return managerID;
	}
	
	@XmlElement(name = "isLeader")
	public boolean getIsLeader(){
		return isLeader;
	}
	
	public void setIsLeader(boolean isLeader){
		this.isLeader=isLeader;
	}

	@XmlElement(name = "transferRecord")
	public TransferRecord getTransferRecord() {
		return transferRecord;
	}

	public void setTransferRecord(TransferRecord transferRecord) {
		this.transferRecord = transferRecord;
	}

	@XmlElement(name = "editRecord")
	public EditRecord getEditRecord() {
		return editRecord;
	}

	public void setEditRecord(EditRecord editRecord) {
		this.editRecord = editRecord;
	}

	public void setManagerID(String managerID) {
		this.managerID = managerID;
	}

	public void setMessageType(BullyMessageEnum messageType) {
		this.messageType = messageType;
	}

	@XmlElement(name = "nodeID")
	public int getNodeID() {
		return nodeID;
	}

	@XmlElement(name = "teacher")
	public Teacher getTeacher() {
		return teacher;
	}

	@XmlElement(name = "student")
	public Student getStudent() {
		return student;
	}

	public BullyMessagePacket() {
	}

	public BullyMessagePacket(BullyMessageEnum messageType) {
		this.messageType = messageType;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	@Override
	public String toString() {
		return "BullyMessagePacket [messageType=" + messageType + ", nodeID=" + nodeID + ", teacher=" + teacher
				+ ", student=" + student + "]";
	}

}
