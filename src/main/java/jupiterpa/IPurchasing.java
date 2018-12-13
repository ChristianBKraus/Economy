package jupiterpa;

import jupiterpa.ICompany.MDelivery;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.util.*;
import lombok.Data;
import lombok.experimental.Accessors;

public interface IPurchasing extends IService {
	void initializeBuyableGoods(int seller)  throws MasterDataException, EconomyException;
	
	void purchase(int seller, EID materialId, int number) throws EconomyException;
	void postDelivery(MDelivery delivery) throws EconomyException;
	
	@Data @Accessors(chain = true)
	public class MDelivery {
		EID salesOrderId;
		EID materialId;
		int partner;
		int quantity;
	}

}
