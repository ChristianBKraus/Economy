package jupiterpa.purchasing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.util.*;
import jupiterpa.util.masterdata.MasterDataSlave;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.IMasterDataDefinition.Material;

import jupiterpa.IWarehouse.MReceivedGoods;
import jupiterpa.purchasing.PurchasingTransformation.PurchasingMapper;

@Service
public class PurchasingService implements IPurchasing {
	public String getName() { return "PURCHASING"; }
	
	@Autowired ICompany company;  
	@Autowired IWarehouse warehouse;
	
	@Autowired SystemService system;
	@Autowired IMasterDataServer masterData;
	
	MasterDataSlave<Material> material;
	
	@Autowired PurchasingMapper mapper;
	PurchasingTransformation transformation;
	
	// Initialize
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		material = new MasterDataSlave<Material>(Material.TYPE,masterData,system);
		
		transformation = new PurchasingTransformation(material,mapper);
	}
	@Override
	public void onboard(Credentials credentials) throws MasterDataException {
		material.onboard(credentials.getTenant());
	}

	// Operations
	@Override
	public EID purchase(int seller,EID materialId, int number) throws EconomyException {
		return company.postOrder(new Credentials(seller), transformation.toOrder(materialId,number));
	}

	@Override
	public void initializeBuyableGoods(int seller) throws MasterDataException, EconomyException {
		for (ICompany.MProduct product : company.getProducts(new Credentials(seller)) ) {
			warehouse.createMaterial(transformation.toMaterial(product));
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
