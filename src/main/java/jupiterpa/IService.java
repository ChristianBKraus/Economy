package jupiterpa;

import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.util.EconomyException;

public interface IService {
	public String getName();
	public void initialize() throws EconomyException, MasterDataException;
	public void onboard(Integer tenant) throws MasterDataException;
}
