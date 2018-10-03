package serverpackage;

public class TransferRecord {

	String recordID;
	String serverName;
	int portNoOfLeaderOfAnotherBullyProcess;

	public int getPortNoOfLeaderOfAnotherBullyProcess() {
		return portNoOfLeaderOfAnotherBullyProcess;
	}

	public void setPortNoOfLeaderOfAnotherBullyProcess(int portNoOfLeaderOfAnotherBullyProcess) {
		this.portNoOfLeaderOfAnotherBullyProcess = portNoOfLeaderOfAnotherBullyProcess;
	}

	public TransferRecord() {
	}

	public TransferRecord(String recordID, String serverName) {
		this.recordID = recordID;
		this.serverName = serverName;
	}

	public String getRecordID() {
		return recordID;
	}

	public void setRecordID(String recordID) {
		this.recordID = recordID;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}
