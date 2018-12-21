package jupiterpa.financials;

import jupiterpa.util.EID;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FinancialsDocumentItem { 
	int lineNumber;

	int account;
	EID invoiceId;

	int quantity;
	Double amount;
	String currency;

	EID salesOrderId;
	EID purchaseOrderId;
	EID deliveryId;
	EID materialId;
	int partner;
}
