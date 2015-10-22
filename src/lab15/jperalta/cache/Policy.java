package lab15.jperalta.cache;

import lab15.jperalta.cache.Cache.CacheElem;

public interface Policy {
	public abstract int getNode(CacheElem [] cache);
}
