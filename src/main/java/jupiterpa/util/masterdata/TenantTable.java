package jupiterpa.util.masterdata;

import java.util.HashMap;
import java.util.Map;

import jupiterpa.util.EID;
import jupiterpa.util.SystemService;

public class TenantTable<T> {
	Map<Integer,Map<EID,T>> table = new HashMap<Integer,Map<EID,T>>();
	
	SystemService system; 
	
	public TenantTable(SystemService system) {
		this.system = system;
	}
	public void onboard(Integer tenant) {
		if (system != null)
			table.put(tenant, new HashMap<EID,T>());
	}
	
	public Map<EID,T> get() {
		Integer tenant = 0;
		if (system != null)
			if (system.getCredentials() != null) 
				tenant = system.getCredentials().getTenant();
		if (table.containsKey(tenant) == false) {
			table.put(tenant, new HashMap<EID,T>());
		};			
		return table.get(tenant);

	}
}
