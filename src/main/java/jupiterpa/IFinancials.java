package jupiterpa;

import lombok.*;
import lombok.experimental.*;

import jupiterpa.util.*;
import jupiterpa.ICompany.*;

public interface IFinancials extends IService {
	void postSalesOrder(MSalesOrder order) throws EconomyException;
	void postMaterialDocument(MMaterialDocument goods) throws EconomyException;
	void postPurchaseOrder(MPurchaseOrder order) throws EconomyException;
	
	void postInvoice(MInvoice invoice) throws EconomyException;
	void postPayment(MPayment payment) throws EconomyException;
	
	
	@Data @Accessors(chain = true)
	public class MSalesOrder {
		EID purchaseOrderId;
		EID salesOrderId; 
		EID materialId;
		int partner;
		int quantity;
	}
	
	@Data @Accessors(chain = true)
	public class MPurchaseOrder {
		EID purchaseOrderId;
		EID salesOrderId;
		int partner;
		EID materialId;
		int quantity;
		double amount;
		String currency;
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
