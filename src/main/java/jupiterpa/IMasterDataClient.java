package jupiterpa;

import jupiterpa.IMasterDataServer.MasterDataException;

public interface IMasterDataClient {	
	void initialLoad() throws MasterDataException;	
	void invalidate(IMasterDataServer.EIDTyped id) throws MasterDataException; 
}
