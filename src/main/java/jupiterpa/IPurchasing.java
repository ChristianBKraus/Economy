package jupiterpa;

import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.util.*;
import lombok.Data;
import lombok.experimental.Accessors;

public interface IPurchasing extends IService {
	void initializeBuyableGoods(int seller)  throws MasterDataException, EconomyException;
	
	EID purchase(int seller, EID materialId, int number) throws EconomyException;
	void postDelivery(MDelivery delivery) throws EconomyException;
	
	@Data @Accessors(chain = true)
	public class MDelivery {
		EID salesOrderId;
		EID materialId;
		int partner;
		int quantity;
	}

}
