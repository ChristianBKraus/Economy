package jupiterpa;

import java.util.*;
import lombok.*;
import lombok.experimental.*;

import jupiterpa.util.*;

public interface IWarehouse extends IService {
	
	List<MStock> getStock(List<EID> materialIds) throws EconomyException;
	MStock getStock(EID materialId) throws EconomyException;
	
	void postInitialStock(MStock stock) throws EconomyException;
	
	void reserveGoods(MIssueGoods goods) throws EconomyException;
	void postIssueGoods(MIssueGoods goods) throws EconomyException;
	
	void postReceivedGoods(MReceivedGoods goods) throws EconomyException;
	
	@Data @Accessors(chain = true) 
	public class MStock {
		EID materialId;
		int quantity;		
	}

	@Data @Accessors(chain = true)
	public class MIssueGoods {
		EID materialId;
		EID salesOrderId;
		int quantity;
	}
	@Data @Accessors(chain = true)
	public class MReceivedGoods {
		EID materialId;
		EID deliveryId;
		int quantity;
	}
}
