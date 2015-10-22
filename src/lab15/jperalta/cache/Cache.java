package lab15.jperalta.cache;

import java.io.File;
import java.util.Date;

import lab15.jperalta.messages.Petition;

/**
 * Cache -- With a replacement method following
 * 			the replacement policy referred by policy. 
 */

public class Cache {
	public static final int size = 5;
	
	private CacheElem [] cache;
	private Policy policy;
	private int tag;
	
	private int nodesNum;
	
	public static final int FIFO = 60;
	public static final int LRU = 61;
	public static final int RND = 62;
	
	//Policy parameters
	private static int counter = 0;
	
	public static class CacheElem {
		String resource;
		byte [] content;
		
		//Replacement parameters
		int order;
		Date time;
		Date lastAccessed;
		
		public CacheElem(String r, byte [] c) {
			resource = r;
			content = c;
			order = counter;
			time = new Date();
			lastAccessed = new Date();
			counter++;
		}
		
		private void update(Petition p) {
			try {
				content = p.readFile(resource);
				time = new Date();
				System.err.println("Updated");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public Cache() {
		cache = new CacheElem[size];
		policy = new FifoPolicy();
		tag = FIFO;
		nodesNum = 0;
	}
	
	public static String getPolicy(int policy) {
		switch(policy){
		case FIFO: return "Fifo";
		case LRU: return "Lru";
		case RND: return "Random";
		default: return null;
		}
	}
	
	public byte [] get(String resource, Petition p) {
		byte [] content = null;
		String aux;
		
		for(int i = 0; i < cache.length; i++) {
			if(cache[i] != null) {
				aux = cache[i].resource;
				if(aux.equals(resource)) {
					long lastModified = new File(resource).lastModified(); 
					if( lastModified >= (cache[i].time).getTime()) {
						cache[i].update(p);
					}
					cache[i].lastAccessed = new Date();
					content = cache[i].content;
					break;
				}
			}
		}
		return content;
	}
	
	public void add(String r, byte [] c) {
		int pos;
		
		CacheElem newElem = new CacheElem(r, c);
		
		if(nodesNum == cache.length) {
			pos = policy.getNode(cache);
		}else {
			pos = emptySpace();
			nodesNum++;
		}
		System.err.println("Position to replace: " + pos);
		if(cache[pos] != null)
			System.err.println("Old node order: " + cache[pos].order);
		
		System.err.println("New node order: " + newElem.order);
		
		cache[pos] = newElem;
	}
	
	//For when the cache is not full yet
	
	private int emptySpace() {
		int pos = 0;
		
		System.err.println("Vacio");
		for(int i = 0; i < cache.length; i++) {
			if(cache[i] == null) {
				pos = i;
				break;
			}
		}
		return pos;
	}
	
	public int changePolicy(int policy) {
		int old = this.tag;
		
		switch(policy){
		case FIFO: this.policy = new FifoPolicy();
		tag = FIFO;
		break;
		case LRU: this.policy = new LruPolicy();
		tag = LRU;
		break;
		case RND: this.policy = new RandomPolicy();
		tag = RND;
		}
		return old;
	}
	
	public CacheElem delete(String resource) {
		CacheElem deleted = null;
		
		for(int i = 0; i < cache.length; i++) {
			if(cache[i] != null) {
				if(resource.equals(cache[i].resource)) {
					deleted = cache[i];
					cache[i] = null;
					nodesNum--;
				}
			}
		}
		return deleted;
	}
	
	private int elemCount() {
		int count = 0;
		
		for(int i = 0; i < cache.length; i++) {
			if(cache[i] != null)
				count++;
		}
		return count;
	}
	
	public void empty() {
		cache = new CacheElem[size];
		counter = 0;
		nodesNum = 0;
		if(this.elemCount() == 0)
			System.err.println("ok");
	}
	
	public String [] getNames() {
		String [] names = new String[nodesNum];
		int j = 0;
		
		for(int i = 0; i < cache.length; i++) {
			if(cache[i] != null) {
				names[j] = cache[i].resource;
				j++;
			}
		}
		return names;
	}
	
	public int [] getSizes() {
		int [] sizes = new int[nodesNum];
		int j = 0;
		
		for(int i = 0; i < cache.length; i++) {
			if(cache[i] != null) {
				sizes[j] = (cache[i].content).length;
				j++;
			}
		}
		return sizes;
	}
	
	public long [] getTimes() {
		long [] times = new long[nodesNum];
		int j = 0;
		
		for(int i = 0; i < cache.length; i++) {
			if(cache[i] != null) {
				times[j] = (cache[i].time).getTime();
				j++;
			}
		}
		return times;
	}
}
