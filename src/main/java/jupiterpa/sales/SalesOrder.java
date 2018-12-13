package jupiterpa.sales;

import lombok.*;
import lombok.experimental.*;

import jupiterpa.util.*;

@Data @Accessors(chain = true)
public class SalesOrder {
	EID purchaseOrderId;
	EID salesOrderId; 
	int partner;
	EID materialId;
	int quantity;
}
