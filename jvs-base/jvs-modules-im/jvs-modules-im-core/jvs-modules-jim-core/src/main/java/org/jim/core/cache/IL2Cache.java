package org.jim.core.cache;

import java.io.Serializable;

/**
 * @author WChao
*/
public interface IL2Cache {
	
	public void putL2Async(String key, Serializable value);
}
