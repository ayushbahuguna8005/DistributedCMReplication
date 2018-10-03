package serverpackage;

import java.io.Serializable;

public enum BullyMessageEnum implements Serializable{
	
	NodeId,
	Heartbeat,
	RequestCreateStudentRecord,
	RequestCreateTecherRecord,
	RequestTransferRecord,
	RequestEditRecord,
	RequestGetRecordCount,
	RequestDeleteRecord
	
}
