package serverpackage;

import java.io.Serializable;
import java.util.*;

public class Student implements Serializable {
	String managerID;
	String recordID;
	String firstName;
	String lastName;
	List<String> coursesRegistered = new ArrayList<String>();
	StatusEnum status;
	String statusDate;

	public Student() {
	}
	
	public Student(String recordID, String firstName, String lastName, List<String> coursesRegistered,
			StatusEnum status, String statusDate) {
		this.recordID = recordID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.coursesRegistered = coursesRegistered;
		this.statusDate = statusDate;
		this.status = status;
	}

	public String getManagerID() {
		return managerID;
	}

	public void setManagerID(String managerID) {
		this.managerID = managerID;
	}

	public String getRecordID() {
		return recordID;
	}

	public void setRecordID(String recordID) {
		this.recordID = recordID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public List<String> getCoursesRegistered() {
		return coursesRegistered;
	}

	public void setCoursesRegistered(List<String> coursesRegistered) {
		this.coursesRegistered = coursesRegistered;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public String getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(String statusDate) {
		this.statusDate = statusDate;
	}

	@Override
	public String toString() {
		return "Student [recordID=" + recordID + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", coursesRegistered=" + coursesRegistered + ", status=" + status + ", statusDate=" + statusDate
				+ "]";
	}

}
