package jupiterpa.util.masterdata;

import java.util.*;
import lombok.Getter;
import jupiterpa.*;
import jupiterpa.util.*;
import jupiterpa.IMasterDataServer.*;

public abstract class MasterDataClient<T extends IMasterDataDefinition.Type> 
	implements IMasterDataClient, IMasterDataRepository<T> {

	protected IMasterDataServer server;
	protected SystemService system;
	
	protected TenantTable<T> data;
	@Getter protected String type;
	
	// Initialize
	public MasterDataClient(String type, IMasterDataServer server, SystemService system) {
		this.type = type;
		this.server = server;
		this.system = system;
		this.data  = new TenantTable<T>(system);
	}
	public void onboard(Integer tenant) throws MasterDataException {
		this.data.onboard(tenant);
	}
	
	// Access
//	protected Map<EID,T> getData() {
//		return data.get();
//	}
	
	@Override
	public boolean containsKey(EID id) { 
		return data.get().containsKey(id);
	}
	@Override
	public T get(EID id) {
		return data.get().get(id);
	}
	@Override	
	public Collection<T> values() {
		return data.get().values();
	}
	
	
	// IMasterDataClient
	@SuppressWarnings("unchecked")
	@Override
	public void invalidate(EIDTyped id) {
		try {
			Object obj = server.get(id);
			if (id.getType() == type) { 
				T entry = (T) obj;
				T replaced = data.get().replace(entry.getId(),entry);
				if (replaced == null) {
					data.get().put(entry.getId(), entry);
				}
				return;
			}
			throw new MasterDataException("Type " + id.getType() + " of entry unknown");	
		} catch (MasterDataException e) {
			data.get().remove(id.getId());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialLoad() throws MasterDataException {

		Collection<Object> entries;
		entries = server.getAll(type);
		for (Object obj : entries) {
			T entry = (T) obj;
			data.get().put(entry.getId(),entry);
		}
	}

}
