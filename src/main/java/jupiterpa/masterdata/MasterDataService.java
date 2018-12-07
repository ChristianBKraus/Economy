package jupiterpa.masterdata;

import java.util.*;
import org.springframework.stereotype.Service;

import jupiterpa.*;

@Service
public class MasterDataService implements IMasterDataServer {
	Map<String,MasterData> types = new HashMap<String,MasterData>();
	Map<String,List<IMasterDataClient>> clientsOfType = new HashMap<String,List<IMasterDataClient>>();
	List<IMasterDataClient> clients = new ArrayList<IMasterDataClient>();

	@Override
	public void registerType(String type, IMasterDataClient client) throws MasterDataException {
		MasterData existing = types.get(type); 
		if (existing != null) {
			throw new MasterDataException("Type " + type + " already registered");
		}
		MasterData md = new MasterData(type);
		types.put(type, md);
		clientsOfType.put(type, new ArrayList<IMasterDataClient>());
		if (clients.contains(client) == false) 
			clients.add(client);
	}

	@Override
	public void registerClient(String type, IMasterDataClient client) throws MasterDataException {
		List<IMasterDataClient> list = clientsOfType.get(type); 
		if ( list == null) { 
			throw new MasterDataException("Type " + type + " not registered");
		}
		list.add(client);
	}
	
	@Override 
	public void reset() {
		types.clear();
		clientsOfType.clear();
		clients.clear();
	}

	@Override
	public Object get(EIDTyped key) throws MasterDataException {
		MasterData md = types.get(key.getType());
		if (md == null) {
			throw new MasterDataException("Type " + key.getType() + " not known");
		}
		return md.get(key.getId());
	}

	@Override
	public Collection<Object> getAll(String type) throws MasterDataException {
		MasterData md = types.get(type);
		if (md == null) {
			throw new MasterDataException("Type " + type + " not known");
		}
		return md.getAll();
	}
	
	void add(EIDTyped key, Object entry) throws MasterDataException {
		MasterData md = types.get(key.getType());
		if (md == null) 
			throw new MasterDataException("Type " + key.getType() + " does not exist");
		md.put(key.getId(), entry);
	}
	void remove(EIDTyped key) throws MasterDataException {
		MasterData md = types.get(key.getType());
		if (md == null) 
			throw new MasterDataException("Typ " + key.getType() + " does not exist");
		md.remove(key.getId());
	}
	
	void publish(EIDTyped key) throws MasterDataException {
		List<IMasterDataClient> clients = clientsOfType.get(key.getType());
		for (IMasterDataClient client : clients) { 
			client.invalidate(key);
		}
	}

	@Override
	public void post(EIDTyped key, Object entry) throws MasterDataException {
		add(key,entry);
		Dependency.add(key, null);
		publish(key);
	}
	
	@Override 
	public void postDependent(EIDTyped key, Object entry, EIDTyped parent) throws MasterDataException {
		add(key,entry);
		Dependency.add(key, parent);
		publish(key);
	}

	@Override
	public void delete(EIDTyped key) throws MasterDataException {
		Dependency.delete(key);
		remove(key);
		publish(key);
	}


}
