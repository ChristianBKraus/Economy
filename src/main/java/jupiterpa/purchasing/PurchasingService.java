package jupiterpa.purchasing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.util.*;
import jupiterpa.util.masterdata.MasterDataMaster;
import jupiterpa.util.masterdata.MasterDataSlave;
import lombok.Getter;
import jupiterpa.*;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.IMasterDataDefinition.Material;
import jupiterpa.IMasterDataDefinition.MaterialPurchasing;
import jupiterpa.IWarehouse.MReceivedGoods;
import jupiterpa.purchasing.PurchasingTransformation.PurchasingMapper;

@Service
public class PurchasingService implements IPurchasing {
	public String getName() { return "PURCHASING"; }
	
	@Autowired ICompany company;  
	@Autowired IWarehouse warehouse;
	@Autowired IFinancials financials;
	
	@Autowired SystemService system;
	@Autowired IMasterDataServer masterData;
	
	MasterDataSlave<Material> material;
	@Getter MasterDataMaster<MaterialPurchasing> materialPurchasing;
	
	@Autowired PurchasingMapper mapper;
	PurchasingTransformation transformation;
	
	// Initialize
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		material = new MasterDataSlave<Material>(Material.TYPE,masterData,system);
		materialPurchasing = new MasterDataMaster<MaterialPurchasing>(MaterialPurchasing.TYPE,masterData,system);
	
		materialPurchasing.addParent(material);
		
		transformation = new PurchasingTransformation(material,mapper);
	}
	@Override 
	public void onboard(Credentials credentials) throws MasterDataException {
		material.onboard(credentials.getTenant());
		materialPurchasing.onboard(credentials.getTenant());
	}

	// Operations
	@Override
	public EID purchase(int seller,EID internalMaterialId, int quantity) throws EconomyException {
		Material m = material.get(internalMaterialId);
		EID externalMaterialId = m.getExternalId();
		EID orderId = company.postOrder(new Credentials(seller), transformation.toOrder(externalMaterialId,quantity));
		ICompany.MProduct product = company.getProduct(new Credentials(seller), externalMaterialId);
		PurchaseOrder purchaseOrder = new PurchaseOrder(EID.get('P'),orderId,seller,internalMaterialId,quantity,product.getPrice()*quantity, product.getCurrency());
		financials.postPurchaseOrder(transformation.toFinancials(purchaseOrder)); 
		return orderId;
	}

	@Override
	public void initializeBuyableGoods(int seller) throws MasterDataException, EconomyException {
		for (ICompany.MProduct product : company.getProducts(new Credentials(seller)) ) {
			Material mat = transformation.toMaterial(product);
			warehouse.createMaterial(mat);
			
			MaterialPurchasing mp = transformation.toMaterialPurchasing(product,mat); 
			materialPurchasing.create(mp);
		}		
	}

	@Override
	public void postDelivery(MDelivery delivery) throws EconomyException {
		// PurchaseOrder
		
		// Warehouse
		MReceivedGoods goods = transformation.toReceivedGoods(delivery);
		warehouse.postReceivedGoods(goods);
	}
	

}
