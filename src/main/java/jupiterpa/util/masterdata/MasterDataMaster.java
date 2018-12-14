package jupiterpa.util.masterdata;

import java.util.*;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.*;
import jupiterpa.util.EID;
import jupiterpa.util.SystemService;

public class MasterDataMaster<T extends IMasterDataDefinition.Type> extends MasterDataClient<T> {

	public MasterDataMaster(String type, IMasterDataServer server, SystemService system) throws MasterDataException {
		super(type,server,system);
		server.registerType(type, this);
	}
	
	@Override
	public void onboard(Integer tenant) throws MasterDataException {
		super.onboard(tenant);
		server.registerType(type, this);
	}
	
	// IMasterDataClient
	// Maintenance
	public void create(T entry) throws MasterDataException {
		if ( data.get().get(entry.getId()) != null ) {
			throw new MasterDataException("Entry with id " + entry.getId() + " does already exist");
		}
		checkParent(entry);
		data.get().put(entry.getId(), entry);
		server.post( new EIDTyped(type,entry.getId()), entry); 
	}
	
	public void update(T entry) throws MasterDataException {
		if ( data.get().get(entry.getId()) == null) {
			throw new MasterDataException("Entry with id " + entry.getId() + " does not exist");
		}
		T new_entry = data.get().replace(entry.getId(), entry);
		if (new_entry == null) {
			throw new MasterDataException("Internal Error: "+ entry.getId() + " was not updated");
		}
		server.post( new EIDTyped(type,entry.getId()), entry); 
	}
	
	// Dependencies
	@SuppressWarnings("rawtypes")
	List<IMasterDataRepository> parents = new ArrayList<IMasterDataRepository>();
	@SuppressWarnings("rawtypes")
	public void addParent(IMasterDataRepository dep) {
		parents.add(dep);
	} 
	
	@SuppressWarnings("rawtypes")
	protected void checkParent(T entry) throws MasterDataException {
		IMasterDataDefinition.HasParent hasParent;
		try {
			hasParent = (IMasterDataDefinition.HasParent) entry;
		} catch (Exception x) {
			return;
		}
		for (IMasterDataRepository dep : parents) {
			if (hasParent != null) {
				EID id = hasParent.getParentId(dep.getType());
				if (id != null) {
					if (dep.containsKey(id) == false) {
						throw new MasterDataException("Parent with id " + id + " does not exist");
					}
				}
			}			
		}
	}
	
}
