package serverpackage;

public class Validation {

	public boolean doesManagerBelongToSameServer(String managerID, String serverName)
	{
		managerID=managerID.toLowerCase().trim();
		serverName=serverName.toLowerCase().trim();
		if(managerID.substring(0, 3).equals(serverName))
		{
			return true;
		}
		return false;
	}
	
	public boolean isTransferServerNameValid(String transerServerName)
	{
		try {
			ServerNameEnum.valueOf(transerServerName.trim());
		} catch (IllegalArgumentException ex) {
			return false;
		}
		return true;
	}
	
	public boolean isRecordPresentInTheServer(Object record)
	{
		if(record==null)
		{
			return false;
		}
		return true;
	}
	
}
