package lab15.jperalta.cache;

import java.util.Random;

import lab15.jperalta.cache.Cache.CacheElem;

public class RandomPolicy implements Policy{

	public int getNode(CacheElem[] cache) {
		Random rand = new Random();
		
		System.err.println("Random");
		return rand.nextInt(cache.length);
	}
	
}
