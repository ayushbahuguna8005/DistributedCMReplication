package serverpackage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import helper.*;

import net.rudp.ReliableSocket;


public class StarterLVL1 {

	public static void main(String[] args) {

		try {
			new File("./Serverlogs").mkdirs();
			//Log mtlLog = new Log("./Serverlogs/MtlServerLog.txt", "MtlServer");
			//Log ddoLog = new Log("./Serverlogs/DdoServerLog.txt", "DdoServer");
			Log lvlLog = new Log("./Serverlogs/LvlServerLog.txt", "LvlServer");
			HashMap<Integer, Integer> portDetailsOfOtherReplicas=new HashMap<>();
			portDetailsOfOtherReplicas.put(2,6669);
			portDetailsOfOtherReplicas.put(3,6672);
			BullyProcess process1 = new BullyProcess(new ReliableSocket(), 1, "lvl",7500, 6666, lvlLog,portDetailsOfOtherReplicas);
			System.out.println("HERE");
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
