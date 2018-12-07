package jupiterpa.util.masterdata;

import jupiterpa.util.EID;

public interface IMasterDataRepository {
	String getType();
	boolean containsKey(EID id);
}
