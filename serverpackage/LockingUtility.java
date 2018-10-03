package serverpackage;

import java.util.*;

public class LockingUtility {

	HashMap<String, LockReason> lockedData=new HashMap<String, LockReason>();
	
	
	public synchronized boolean isRecordLocked(String recordID)
	{
		return lockedData.containsKey(recordID);
	}
	
	public synchronized void addToLockList(String recordID,LockReason reason)
	{
		lockedData.put(recordID,reason);
	}
	
	public synchronized void removeFromLockList(String recordID)
	{
		boolean isRemoved=lockedData.remove(recordID) != null;
		if(!isRemoved)
			throw new RuntimeException("The Record ID was not locked. Something must have gone wrong");
		
	}
	
	public synchronized LockReason getLockingReason(String recordID)
	{
		
		LockReason reason=lockedData.get(recordID);
		return reason;
	}
	
}

enum LockReason
{
	CurrentlyBeingEdited,
	CurrentlyBeingTransfered
}
