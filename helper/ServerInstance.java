package helper;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import ServerOperationApp.ServerOperationIDL;
import ServerOperationApp.ServerOperationIDLHelper;

public class ServerInstance {

	public static ServerOperationIDL getServerInstance(String managerID,NamingContextExt ncRef) {
		ServerOperationIDL operations = null;
		String lookUpString="FE";

		try {
			operations= ServerOperationIDLHelper.narrow(ncRef.resolve_str(lookUpString));
		} catch (NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Error in binding");
		}
		return operations;
	}

}

















//String managerAcroynm = managerID.substring(0, 3).toLowerCase();
//ServerOperationIDL operations = null;
//String lookUpString = "";
//if (managerAcroynm.equals("mtl")) {
//	lookUpString = "mtl";
//
//} else if (managerAcroynm.equals("lvl")) {
//	lookUpString = "lvl";
//} else {
//	lookUpString = "ddo";
//}

