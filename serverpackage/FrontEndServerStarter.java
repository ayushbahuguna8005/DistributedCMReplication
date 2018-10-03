package serverpackage;

import java.io.File;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import ServerOperationApp.ServerOperationIDL;
import ServerOperationApp.ServerOperationIDLHelper;
import helper.Log;

public class FrontEndServerStarter {
	public static void main(String[] args) {
		try {
			new File("./FrontEndlogs").mkdirs();
			Log frontEndLog = new Log("./FrontEndlogs/FrontEndlog.txt", "FrontEndLog");
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);
			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();
			// create servant and register it with the ORB
			FrontEndServer frontEnd = new FrontEndServer(frontEndLog);
			frontEnd.setORB(orb);
			org.omg.CORBA.Object frontEndRef = rootpoa.servant_to_reference(frontEnd);
			ServerOperationIDL frontEndServer = ServerOperationIDLHelper.narrow(frontEndRef);
			// get the root naming context
			// NameServiceinvokes the name service
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExtwhich is part of the Interoperable Naming
			// Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			// bind the Object Reference in Naming
			String name = "FE";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, frontEndServer);
			// wait for invocations from clients
			orb.run();
		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
		System.out.println("Class Management Server Exiting ...");
	}
}
