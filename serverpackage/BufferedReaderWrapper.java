package serverpackage;

import java.io.BufferedReader;
import java.io.Reader;

public class BufferedReaderWrapper extends BufferedReader {

	public BufferedReaderWrapper(Reader in) {
		super(in);
		// TODO Auto-generated constructor stub
	}
	
	public BufferedReaderWrapper(){
		super(null);
	}

}
