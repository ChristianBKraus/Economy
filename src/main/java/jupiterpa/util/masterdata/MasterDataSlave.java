package jupiterpa.util.masterdata;

import org.springframework.stereotype.Service;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.*;


public class MasterDataSlave<T extends IMasterDataDefinition.Type> extends MasterDataClient<T> {

	public MasterDataSlave(String type, IMasterDataServer server) throws MasterDataException {
		super(type,server);
		getServer().registerClient(type, this);
	}
		
	// IMasterDataClient
	
}
