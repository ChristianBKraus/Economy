package jupiterpa.util.masterdata;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.*;
import jupiterpa.util.SystemService;


public class MasterDataSlave<T extends IMasterDataDefinition.Type> extends MasterDataClient<T> {

	public MasterDataSlave(String type, IMasterDataServer server, SystemService system) throws MasterDataException {
		super(type,server, system);
		server.registerClient(type, this);
	}
	
	@Override
	public void onboard(Integer tenant) throws MasterDataException {
		super.onboard(tenant);
		server.registerClient(type, this);
	}
		
	// IMasterDataClient
	
}
