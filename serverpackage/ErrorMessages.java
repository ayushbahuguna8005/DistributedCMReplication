package serverpackage;
import helper.*;
import java.util.logging.Level;

public class ErrorMessages {
	
	static String locationError="Error occured at Server. Location can be either mtl, lvl or ddo";
	static String statusError="Error occured at Server. Status can be either 0 (inactive) or 1 active";
	
	public static String getSuccessTeacherInsertRecord(String recordID)
	{
		return "Teacher record successfully inserted. The record id of the new record is "+recordID;
	}
	
	public static String getSuccessStudentInsertRecord(String recordID)
	{
		return "Student record successfully inserted. The record id of the new record is "+recordID;
	}
	
	public static String getSameLocationError(String  managerID, String recordID,String serverName,Log  log)
	{
		String errorMessage="Record with record id "+recordID+" is already present in the "+serverName+" server. "+ " by " + managerID;
		log.logger.log(Level.INFO, errorMessage);
		return errorMessage;
	}
	
	public static String getNoRecordFoundError(String  managerID, String recordID,String serverName,Log  log)
	{
		String errorMessage= "Record with record id "+recordID+" is not found in the "+serverName+" server. "+ " by " + managerID;
		log.logger.log(Level.INFO, errorMessage);
		return errorMessage;
	}
	
	
	public static String getNoSuchServerError(String  managerID,String serverName,Log  log)
	{
		String errorMessage= "There is no such server as "+  serverName+". " +" by " + managerID; 
		log.logger.log(Level.INFO, errorMessage);
		return errorMessage;
	}
	
	public static String getSuccessTransferMessage(String recordID, String serverName, Log  log )
	{
		String errorMessage= "Success: Record with record id "+recordID+" successfully inserted in server "+serverName;
		log.logger.log(Level.INFO, errorMessage);
		return errorMessage;
	}
	
	public static String getSuccessTransferMessage(String managerID,String recordID, String serverName, Log  log)
	{
		String errorMessage="Success: Record with record id "+recordID+" successfully inserted in server "+serverName+". " +" by " + managerID;
		log.logger.log(Level.INFO, errorMessage);
		return errorMessage;
	}

	public static String getFailTransferMessage(String managerID, String recordID, String serverName, Log log) {
		String errorMessage="Failure: Record with record id "+recordID+" could not be transfered to server "+serverName+". " +" by " + managerID;
		log.logger.log(Level.INFO, errorMessage);
		return errorMessage;
	}
	
	public static String getFailTransferMessage(String recordID, String serverName, Log log) {
		String errorMessage="Failure: To insert record with record id "+recordID + " at server "+ serverName;
		log.logger.log(Level.INFO, errorMessage);
		return errorMessage;
	}

	public static String getSuccessTransferMessageWithNewRecordID(String recordID, String newRecordID,
			String serverName, Log log) {
		
		String errorMessage="Success: Record with "+recordID + " is change to record id "+newRecordID +" as there was alraedy a record with the former record id "+"at server "+ serverName;
		log.logger.log(Level.INFO, errorMessage);
		return errorMessage;
	}
}