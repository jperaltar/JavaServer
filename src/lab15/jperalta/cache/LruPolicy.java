package lab15.jperalta.cache;

import lab15.jperalta.cache.Cache.CacheElem;

public class LruPolicy implements Policy{

	public int getNode(CacheElem[] cache) {
		int chosen = 0;
		
		System.err.println("LRU");
		for(int i = 1; i < cache.length; i++) {
			if(cache[i].lastAccessed.compareTo(cache[chosen].lastAccessed) < 0){
				chosen = i;
			}
		}
		return chosen;
	}
}
