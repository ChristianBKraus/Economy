package jupiterpa.warehouse;

import lombok.*;
import lombok.experimental.*;

import jupiterpa.util.*;

@Data @Accessors(chain = true)
public class MaterialDocument {
	EID documentNumber;
	
	EID materialId;
	int quantity;
	
	EID salesOrderId;
	EID deliveryId;
	int partner;
}
