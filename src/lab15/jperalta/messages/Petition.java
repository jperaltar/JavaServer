package lab15.jperalta.messages;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class Petition {
	private String resource;
	
	public Petition(String resource) {
		this.resource = resource;
	}
	
	public byte [] readFile(String resource) throws Exception {
		File myFile = new File(resource);
		byte [] serialFile = new byte [(int) myFile.length()];
				
		BufferedInputStream fd = new BufferedInputStream(
						new FileInputStream(myFile.getPath()));
		fd.read(serialFile, 0, serialFile.length);
		fd.close();
		return serialFile;
	}
	
	public static String rcvPetition(BufferedReader disreader) throws Exception {
		char[] buffer = new char[1024];
		String resource = null;
		
		String petition = disreader.readLine();
		String verb = petition.split(" ")[0];
		if("GET".equals(verb)) {
			resource = petition.split(" ")[1];
			resource = resource.substring(1);
		}
		
		//Read the rest of the petition
		disreader.read(buffer);
		buffer = null;
		
		return resource;
	}
	
	public byte[] processPetition(OutputStream dos) throws Exception {
		String format = "HTTP/1.1 200 OK\r\n\r\n";
		
		byte [] formatArray = format.getBytes();
		byte [] byteArray = readFile(resource);
		
		byte [] totalArray = new byte [formatArray.length + byteArray.length];
		
		System.arraycopy(formatArray, 0, totalArray, 0, formatArray.length);
		System.arraycopy(byteArray, 0, totalArray, formatArray.length, byteArray.length);

		return totalArray;
	}
	
	public String getResource() {
		return resource;
	}
}
