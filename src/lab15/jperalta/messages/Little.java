package lab15.jperalta.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Little {
	
	private static byte[] longToByteArray(long value) {
	    return new byte[] {
	        (byte) (value >> 56),
	        (byte) (value >> 48),
	        (byte) (value >> 40),
	        (byte) (value >> 32),
	        (byte) (value >> 24),
	        (byte) (value >> 16),
	        (byte) (value >> 8),
	        (byte) value
	    };
	}
	
	private static long byteArrayToLong(byte [] b) {
		long value = 0;
		
		for (int i = 0; i < b.length; i++){
		   value += ((long) b[i] & 0xffL) << (8 * i);
		}
		return value;
	}
	
	public static void writeIntLE(DataOutputStream out, int value) {
		try {
			for(int i = 0; i < 4; i++) {
				out.writeByte((value >> i*8) & 0xFF);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	  
	}
	
	public static int readIntLE(DataInputStream in) {
		byte[] bytes = new byte[4];
		int value;
		
		try {
			in.readFully(bytes, 0, 4);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		value = ((bytes[0] & 0xFF)) | ((bytes[1] & 0xFF) << 8)
		        | ((bytes[2] & 0xFF) << 16) | (bytes[3] & 0xFF << 24);
		return value;
	}
	
	public static void writeLongLE(DataOutputStream out, long value) {		
		byte [] b = longToByteArray(value);
		
		try {
			for(int i = 7; i >= 0; i--) {
				out.write(b[i]);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static long readLongLE(DataInputStream in) {
		byte[] bytes = new byte[8];
		
		try {
			in.readFully(bytes, 0, 8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return byteArrayToLong(bytes);
	}
}
