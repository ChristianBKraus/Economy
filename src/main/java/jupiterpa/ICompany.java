package jupiterpa;

import java.util.List;
import lombok.*;
import lombok.experimental.*;

import jupiterpa.util.*;
import jupiterpa.ISales.MProduct;
import jupiterpa.ISales.MPurchaseOrder;

public interface ICompany extends IService {
	
	List<MProduct> getProducts();
	MProduct getProduct(EID materialId) throws EconomyException;
	
	EID postPurchaseOrder(MPurchaseOrder order) throws EconomyException;
	void postDelivery(MDelivery delivery) throws EconomyException;
	void postInvoice(MInvoice invoice) throws EconomyException;
	void postPayment(MPayment payment) throws EconomyException;
			
	@Data @Accessors(chain = true)
	public class MDelivery {
		EID salesOrderId;
		EID materialId;
		int quantity;
	}
	
	@Data @AllArgsConstructor
	public class MInvoice {
		EID invoiceId;
		EID orderId;
		Double amount;
		String currency;
		EID materialId;
		Long quantity;
	}
	
	@Data @AllArgsConstructor
	public class MPayment {
		EID invoiceId;
		Double amount;
		String currency;
	}
	
}
