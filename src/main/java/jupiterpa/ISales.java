package jupiterpa;

import java.util.*;
import lombok.*;
import lombok.experimental.*;
import jupiterpa.util.*;

public interface ISales extends IService {
	
	List<MProduct> getProducts();
	MProduct getProduct(EID materialId) throws EconomyException;
	
	EID postOrder(MOrder order) throws EconomyException;
	
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
		int partner;
		EID materialId;
		int quantity;
	}
	

}
