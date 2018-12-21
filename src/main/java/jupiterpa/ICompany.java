package jupiterpa;

import java.util.List;
import lombok.*;
import lombok.experimental.*;

import jupiterpa.util.*;

public interface ICompany extends IService {
	
	List<MProduct> getProducts(Credentials credentials);
	MProduct getProduct(Credentials credentials, EID materialId) throws EconomyException;
	
	EID postOrder(Credentials credentials, MOrder order) throws EconomyException;
	void postDelivery(Credentials credentials, MDelivery delivery) throws EconomyException;
	void postInvoice(Credentials credentials, MInvoice invoice) throws EconomyException;
	void postPayment(Credentials credentials, MPayment payment) throws EconomyException;
			
	@Data @Accessors(chain = true)
	public class MProduct {
		EID materialId; 
		String description;
		Double price;
		String currency;
	} 
	
	@Data @Accessors(chain = true)
	public class MOrder {
		EID orderId;
		EID materialId;
		int quantity;
	}

	@Data @Accessors(chain = true)
	public class MDelivery {
		EID salesOrderId;
		EID materialId;
		int quantity;
	}
	
	@Data @Accessors(chain = true)
	public class MInvoice {
		EID invoiceId;
		EID orderId;
		Double amount;
		String currency;
		EID materialId;
		int partner;
		Long quantity;
	}
	
	@Data @Accessors(chain = true)
	public class MPayment {
		EID invoiceId;
		int partner;
		Double amount;
		String currency;
	}
	
}
