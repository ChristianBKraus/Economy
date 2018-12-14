package jupiterpa.util.masterdata;

import java.util.Collection;

import jupiterpa.IMasterDataDefinition;
import jupiterpa.util.EID;

public interface IMasterDataRepository<T extends IMasterDataDefinition.Type> {
	String getType();
	
	boolean containsKey(EID id);
	T get(EID id);
	Collection<T> values();
}
