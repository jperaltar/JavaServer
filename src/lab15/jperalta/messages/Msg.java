package lab15.jperalta.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import lab15.jperalta.cache.Cache;

/**
 * Principal messages:
 * 		(tag) FLUSH
 * 		(tag) DEL : key (String)
 * 		(tag) --RDEL : if "" == DEL could delete key
 * 		(tag) LIST
 * 		(tag) --RLIST : keys(String[])
 * 		(tag) POLICY : policy (int)
 * 		(tag) --RPOLICY : old (int)
 * 		(tag) QUIT
 */

public class Msg {
	public static final byte FLUSH = 60;
	public static final byte DEL = 61;
	public static final byte RDEL = 62;
	public static final byte LIST = 63;
	public static final byte RLIST = 64;
	public static final byte POLICY = 65;
	public static final byte RPOLICY = 66;
	public static final byte QUIT = 67;
	
	public int tag;
	public final byte type;
	
	private static int newtag;
	
	protected Msg(int tag, byte type){
		this.tag = tag;
		this.type = type;
	}
	

	public static String typeName(byte typ){
		switch(typ){
		case FLUSH: return "Flush";
		case DEL: return "Del";
		case RDEL: return "Rdel";
		case LIST: return "List";
		case RLIST: return "Rlist";
		case POLICY: return "Policy";
		case RPOLICY: return "Rpolicy";
		case QUIT: return "Quit";
		default: throw new RuntimeException("typname: bad msg type");
		}
	}
	
	public String toString() {
		return super.toString() +
				String.format("Tag: %s, Type: %s\n", tag, typeName(type));
	}
	
	protected static synchronized int newtag() {
		newtag++;
		return newtag;
	}
	
	public void sendTo(DataOutputStream dos) {
		try {
			Little.writeIntLE(dos,tag);
			dos.writeByte(type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String readString(DataInputStream dis) {
		int strlen;
		String str = "";
		byte[] b = new byte[256];
		
		try {
			strlen = Little.readIntLE(dis);
			if(strlen > 0) {
				dis.readFully(b, 0, strlen);
				str = new String(b, 0, strlen);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return str;
	}
	
	public void writeString(DataOutputStream dos, String str) {
		int len = 0;
		byte[] bytes;
		try {
			if(str != null)
				bytes = str.getBytes("UTF-8");
			else
				bytes = "".getBytes();
			len = bytes.length;
			Little.writeIntLE(dos,len);
			dos.write(bytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Msg rcvFrom(DataInputStream dis) {
		int tag = 0;
		byte type = 0;
		
		try {
			tag = Little.readIntLE(dis);
		} catch (Exception e) {
			return null;
		}
		
		try {
			type = dis.readByte();
			
			switch(type) {
			case FLUSH: return new Flush(tag);
			case DEL: return new Del(tag, dis);
			case RDEL: return new Rdel(tag, dis);
			case LIST: return new List(tag);
			case RLIST: return new Rlist(tag, dis);
			case POLICY: return new Policy(tag, dis);
			case RPOLICY: return new Rpolicy(tag, dis);
			case QUIT: return new Quit(tag);
			default: throw new RuntimeException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public byte getType() {
		return type;
	}
	
	/**
	 * FLUSH - Empties cache
	 */
	
	public static class Flush extends Msg {
		public Flush() {
			super(newtag(), FLUSH);
		}
		
		protected Flush(int tag) {
			super(tag, FLUSH);
		}
		
		public void process(Cache cache) {
			synchronized(cache) {
				cache.empty();
			}
		}
	}
	
	/**
	 * DEL - Deletes cache element
	 */
	
	public static class Del extends Msg {
		public String key;
		
		public Del(String key) {
			super(newtag(), DEL);
			this.key = key;
		}

		protected Del(int tag, DataInputStream dis) {
			super(tag, DEL);
			key = readString(dis);
		}
		
		public void sendTo(DataOutputStream dos) {
			synchronized(dos) {
				super.sendTo(dos);
				writeString(dos, key);
			}
		}
		
		public String toString() {
			return super.toString() +
					String.format("Key: %s", key);
		}
		
		public Msg process(Cache cache) {
			Rdel m;
			String rMsg = "";
			
			synchronized(cache) {
				if(cache.delete(key) == null)
					rMsg = "Not Found";
			}
			m = new Rdel(rMsg, tag);
			return m;
		}
	}
	
	/**
	 * RDEL - Reply msg for Del msgs 
	 */
	
	public static class Rdel extends Msg {
		public String msg;

		public Rdel(String value, int tag) {
			super(tag, RDEL);
			this.msg = value;
		}
		
		protected Rdel(int tag, DataInputStream dis) {
			super(tag, RDEL);
			msg = readString(dis);
		}
		
		public String toString() {
			return super.toString() +
					String.format("Value: %s", msg);
		}
		
		public void sendTo(DataOutputStream dos) {
			synchronized(dos) {
				super.sendTo(dos);
				writeString(dos, msg);
			}
		}
	}
	
	/**
	 * LIST - Shows list of cache elements
	 */
	
	public static class List extends Msg {
		public List() {
			super(newtag(), LIST);
		}
		
		protected List(int tag) {
			super(tag, LIST);
		}
		
		public Msg process(Cache cache) {
			String [] names;
			int [] sizes;
			long [] times;
			Rlist m;
			
			synchronized(cache){
				names = cache.getNames();
				sizes = cache.getSizes();
				times = cache.getTimes();
			}
			
			m = new Rlist(names, sizes, times, tag);
			return m;
		}
	}
	
	/**
	 * RLIST - Reply msg for List msgs 
	 */
	
	public static class Rlist extends Msg {
		public String [] names;
		public int [] sizes;
		public long [] times;

		public Rlist(String [] names, int [] sizes, long [] times, int tag) {
			super(tag, RLIST);
			this.names = names;
			this.sizes = sizes;
			this.times = times;
		}
		
		protected Rlist(int tag, DataInputStream dis) {
			super(tag, RLIST);
			
			try {
				int num = Little.readIntLE(dis);
				names = new String[num];
				sizes = new int[num];
				times = new long[num];
				for(int i = 0; i < num; i++) {
					names[i] = readString(dis);
					sizes[i] = Little.readIntLE(dis);
					times[i] = Little.readLongLE(dis);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public void sendTo(DataOutputStream dos) {
			synchronized(dos) {
				super.sendTo(dos);
				try {
					Little.writeIntLE(dos, names.length);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				for(int i = 0; i < names.length; i++){
					writeString(dos, names[i]);
					Little.writeIntLE(dos, sizes[i]);
					Little.writeLongLE(dos, times[i]);
				}
			}
		}
	}
	
	/**
	 * POLICY - Msg to change policy
	 */
	
	public static class Policy extends Msg {
		int policy;
		
		public Policy(int policy) {
			super(newtag(), POLICY);
			this.policy = policy;
		}
		
		protected Policy(int tag, DataInputStream dis) {
			super(tag, POLICY);
			try {
				policy = Little.readIntLE(dis);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public void sendTo(DataOutputStream dos) {
			synchronized(dos) {
				super.sendTo(dos);
				try {
					Little.writeIntLE(dos,policy);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		public Msg process(Cache cache) {
			Rpolicy m;
			
			synchronized(cache) {
				cache.changePolicy(policy);
			}
			m = new Rpolicy(policy, tag);
			return m;
		}
	}
	
	/**
	 * RPOLICY - Reply msg for Policy msgs
	 */
	
	public static class Rpolicy extends Msg {
		public int oldPolicy;

		public Rpolicy(int old, int tag) {
			super(tag, RPOLICY);
			oldPolicy = old;
		}
		
		protected Rpolicy(int tag, DataInputStream dis) {
			super(tag, RPOLICY);
			
			try {
				oldPolicy = Little.readIntLE(dis);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public void sendTo(DataOutputStream dos) {
			synchronized(dos) {
				super.sendTo(dos);
				try {
					Little.writeIntLE(dos,oldPolicy);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * QUIT - Close the service
	 */
	
	public static class Quit extends Msg {
		public Quit() {
			super(newtag(), QUIT);
		}
		
		protected Quit(int tag) {
			super(tag, QUIT);
		}
	}
}
