package jupiterpa;

import lombok.*;
import lombok.experimental.*;

import jupiterpa.util.*;
import jupiterpa.ICompany.*;

public interface IFinancials extends IService {
	void salesOrder(MSalesOrder order) throws EconomyException;
	
	void postInvoice(MInvoice invoice) throws EconomyException;
	void postPayment(MPayment payment) throws EconomyException;
	
	void postInitialGoods(MMaterialDocument goods) throws EconomyException;
	void postIssueGoods(MMaterialDocument goods) throws EconomyException;
	void postReceivedGoods(MMaterialDocument goods) throws EconomyException;
	
	@Data @Accessors(chain = true)
	public class MSalesOrder {
		EID purchaseOrderId;
		EID salesOrderId; 
		EID materialId;
		int partner;
		int quantity;
	}
	@Data @Accessors(chain = true)
	public class MMaterialDocument {
		EID documentNumber;
		EID materialId;
		int quantity;
		EID salesOrderId;
		EID deliveryId;
		int partner;
	}
}
