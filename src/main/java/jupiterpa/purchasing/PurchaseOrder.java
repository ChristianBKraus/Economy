package jupiterpa.purchasing;

import jupiterpa.util.EID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class PurchaseOrder {
	EID purchaseOrderId;
	EID salesOrderId;
	int partner;
	EID materialId;
	int quantity;
	double amount;
	String currency;
}
