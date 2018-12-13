package jupiterpa;

import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.util.Credentials;
import jupiterpa.util.EconomyException;

public interface IService {
	public String getName();
	public void initialize() throws EconomyException, MasterDataException;
	public void onboard(Credentials credemtials) throws MasterDataException;
}
