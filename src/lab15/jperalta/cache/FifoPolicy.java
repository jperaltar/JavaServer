package lab15.jperalta.cache;

import lab15.jperalta.cache.Cache.CacheElem;

public class FifoPolicy implements Policy{
	
	public int getNode(CacheElem [] cache) {
		int chosen = 0;
		
		System.err.println("FIFO");
		for(int i = 0; i < cache.length; i++) {
			System.err.println(cache[i].order);
			if(cache[i].order < cache[chosen].order)
				chosen = i;
		}
		return chosen;
	}
}
