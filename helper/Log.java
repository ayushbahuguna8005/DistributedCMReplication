package helper;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {

	public Logger logger;
	FileHandler fileHandler;
	String className;
	
	public Log(String fileName, String className) {
		this.className = className;
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Error Creating File");
				e.printStackTrace();
			}
		}
		try {
			fileHandler = new FileHandler(fileName, true);
			logger = Logger.getLogger(className);
			logger.addHandler(fileHandler);
			logger.setUseParentHandlers(false);
			SimpleFormatter simpleFormatter = new SimpleFormatter();
			fileHandler.setFormatter(simpleFormatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
