package serverpackage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import helper.*;

import net.rudp.ReliableSocket;


public class StarterMTL1 {

	public static void main(String[] args) {

		try {
			new File("./Serverlogs").mkdirs();
			Log mtlLog = new Log("./Serverlogs/MtlServerLog.txt", "MtlServer");
			/*Log ddoLog = new Log("./Serverlogs/DdoServerLog.txt", "DdoServer");
			Log lvlLog = new Log("./Serverlogs/LvlServerLog.txt", "LvlServer");*/
			
			HashMap<Integer, Integer> portDetailsOfOtherReplicas=new HashMap<>();
			portDetailsOfOtherReplicas.put(2,6668);
			portDetailsOfOtherReplicas.put(3,6671);
			BullyProcess process1 = new BullyProcess(new ReliableSocket(), 1, "mtl",7500, 6665, mtlLog,portDetailsOfOtherReplicas);
			//BullyProcess process2 = new BullyProcess(new ReliableSocket(), 2);
			// BullyProcess process3 = new BullyProcess(new ReliableSocket(),3);
			process1.run1();
			//process2.start();

			//process2.stop = true;
			// process3.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
