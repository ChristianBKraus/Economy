package jupiterpa.masterdata;

import java.util.*;

import jupiterpa.util.*;
import jupiterpa.IMasterDataServer.*;

public class MasterData {
	String type;
	Map<EID,Object> entries = new HashMap<EID,Object>();
	
	public MasterData(String type) {
		this.type = type;
	}
	
	public void put(EID key, Object entry) {
		entries.put(key, entry);
	}
	public void remove(EID key) throws MasterDataException {
		Object entry = entries.remove(key);
		if (entry == null) {
			throw new MasterDataException("Entry " + key + "does not exist");
		}
	}
	public Object get(EID key)  {
		return entries.get(key);
	}
	public Collection<Object> getAll() {
		return entries.values();
	}
}
