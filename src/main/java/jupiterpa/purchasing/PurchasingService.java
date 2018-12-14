package jupiterpa.purchasing;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.util.*;
import jupiterpa.util.masterdata.MasterDataSlave;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.IMasterDataDefinition.Material;

import jupiterpa.IWarehouse.MReceivedGoods;
import jupiterpa.ICompany.MOrder;

@Service
public class PurchasingService implements IPurchasing {
	public String getName() { return "PURCHASING"; }
	
	@Autowired ICompany company;  
	@Autowired IWarehouse warehouse;
	
	@Autowired SystemService system;
	@Autowired IMasterDataServer masterData;
	
	MasterDataSlave<Material> material;
	
	// Initialize
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		material = new MasterDataSlave<Material>(Material.TYPE,masterData,system);
	}
	@Override
	public void onboard(Credentials credentials) throws MasterDataException {
		material.onboard(credentials.getTenant());
	}

	// Operations
	@Override
	public EID purchase(int seller,EID materialId, int number) throws EconomyException {
		return company.postOrder(new Credentials(seller), toOrder(materialId,number));
	}

	@Override
	public void initializeBuyableGoods(int seller) throws MasterDataException, EconomyException {
		for (ICompany.MProduct product : company.getProducts(new Credentials(seller)) ) {
			warehouse.createMaterial(fromProduct(product));
		}		
	}

	@Override
	public void postDelivery(MDelivery delivery) throws EconomyException {
		
		// warehouse
		MReceivedGoods goods = toReceivedGoods(delivery);
		warehouse.postReceivedGoods(goods);
	}
	
	// Transformation
	@Autowired PurchasingMapper mapper;
	@Mapper(componentModel = "spring")
	public interface PurchasingMapper {
		IWarehouse.MReceivedGoods map(MDelivery delivery);
	}
	
	IWarehouse.MReceivedGoods toReceivedGoods(MDelivery delivery) {
		Material internal = null;
		for (Material m : material.values()) {
			if (m.getExternalId() == delivery.getMaterialId()) {
				internal = m;
			}
		}
		
		return mapper.map(delivery)
			.setMaterialId(internal.getMaterialId())
			.setDeliveryId(delivery.getSalesOrderId())
			.setQuantity(-1 * delivery.getQuantity());
	}

	ICompany.MOrder toOrder(EID materialId, int quantity) {
		return new MOrder()
				.setMaterialId(materialId)
 				.setQuantity(quantity);
	}
	
	Material fromProduct(ICompany.MProduct product) {
		return new Material(EID.get('M'), product.getMaterialId(),product.getDescription() );
	}

}
