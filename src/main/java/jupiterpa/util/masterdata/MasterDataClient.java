package jupiterpa.util.masterdata;

import java.util.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import jupiterpa.*;
import jupiterpa.util.*;
import jupiterpa.IMasterDataServer.*;

public abstract class MasterDataClient<T extends IMasterDataDefinition.Type> 
	implements IMasterDataClient, IMasterDataRepository {

	IMasterDataServer masterData;
	
	@Getter Map<EID,T> data = new HashMap<EID,T>();
	@Getter String type;
	
	public MasterDataClient(String type, IMasterDataServer server) {
		this.type = type;
		this.masterData = server;
	}
	
	// Access
	@Override
	public boolean containsKey(EID id) { 
		return data.containsKey(id);
	}
	public T get(EID id) {
		return data.get(id);
	}
		
	public Collection<T> values() {
		return data.values();
	}
	protected IMasterDataServer getServer() { return masterData; } 
	
	
	// IMasterDataClient
	@SuppressWarnings("unchecked")
	@Override
	public void invalidate(EIDTyped id) {
		try {
			Object obj = masterData.get(id);
			if (id.getType() == type) { 
				T entry = (T) obj;
				T replaced = data.replace(entry.getId(),entry);
				if (replaced == null) {
					data.put(entry.getId(), entry);
				}
				return;
			}
			throw new MasterDataException("Type " + id.getType() + " of entry unknown");	
		} catch (MasterDataException e) {
			data.remove(id.getId());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialLoad() throws MasterDataException {

		Collection<Object> entries;
		entries = masterData.getAll(type);
		for (Object obj : entries) {
			T entry = (T) obj;
			data.put(entry.getId(),entry);
		}
	}

}
