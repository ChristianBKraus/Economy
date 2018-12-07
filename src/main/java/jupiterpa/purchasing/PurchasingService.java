package jupiterpa.purchasing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.Setter;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.util.*;
import jupiterpa.ISales.*;

@Service
public class PurchasingService implements IPurchasing {
	public String getName() { return "PURCHASING"; }
	
	@Autowired ICompany company;  
	
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		// do nothing
	}

	@Override
	public void purchase(EID materialId, int number) throws EconomyException {
		company.postPurchaseOrder(toPurchaseOrder(materialId,number));
	}
	
	MPurchaseOrder toPurchaseOrder(EID materialId, int quantity) {
		return new MPurchaseOrder()
				.setPurchaseOrderId(EID.get('P'))
				.setMaterialId(materialId)
 				.setQuantity(quantity);
	}
}
