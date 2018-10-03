package serverpackage;

public class EditRecord {

	String recordID;
	String fieldName;
	String newValue;

	public EditRecord() {
	}

	public EditRecord(String recordID, String fieldName, String newValue) {
		this.fieldName = fieldName;
		this.newValue = newValue;
		this.recordID = recordID;
	}

	public String getRecordID() {
		return recordID;
	}

	public void setRecordID(String recordID) {
		this.recordID = recordID;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

}
