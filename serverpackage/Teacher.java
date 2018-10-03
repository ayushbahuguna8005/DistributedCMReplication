package serverpackage;

import java.io.Serializable;

public class Teacher implements Serializable {
	String recordID;
	String firstName;
	String lastName;
	String address;
	String phone;
	String specialization;
	ServerNameEnum location;
	
	public Teacher() {
	}

	public Teacher(String recordID, String firstName, String lastName, String address, String phone,
			String specialization, String location) {
		this.recordID = recordID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.phone = phone;
		this.specialization = specialization;
		try {
			this.location = ServerNameEnum.valueOf(location);
		} catch (IllegalArgumentException ex) {
			System.out.println(ErrorMessages.locationError);
			ex.printStackTrace();
		}
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public ServerNameEnum getLocation() {
		return location;
	}

	public void setLocation(ServerNameEnum location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "Teacher [recordID=" + recordID + ", firstName=" + firstName + ", lastName=" + lastName + ", address="
				+ address + ", phone=" + phone + ", specialization=" + specialization + ", location=" + location + "]";
	}

}
