package jupiterpa.purchasing;

import java.util.List;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jupiterpa.*;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.ISales.MProduct;
import jupiterpa.IWarehouse.MReceivedGoods;
import jupiterpa.ICompany.MOrder;
import jupiterpa.util.*;
import jupiterpa.util.masterdata.MasterDataSlave;
import jupiterpa.IMasterDataDefinition.Material;

@Service
public class PurchasingService implements IPurchasing {
	public String getName() { return "PURCHASING"; }
	
	@Autowired ICompany company;  
	@Autowired IWarehouse warehouse;
	@Autowired SystemService system;
	@Autowired IMasterDataServer masterData;
	MasterDataSlave<Material> material;
	
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		material = new MasterDataSlave<Material>(Material.TYPE,masterData,system);
	}

	@Override
	public void onboard(Credentials credentials) throws MasterDataException {
		material.onboard(credentials.getTenant());
	}

	@Override
	public EID purchase(int seller,EID materialId, int number) throws EconomyException {
		Credentials credentials = new Credentials(seller);
		EID purchaseId = company.postOrder(credentials, toOrder(materialId,number));
		return purchaseId;
	}
	
	MOrder toOrder(EID materialId, int quantity) {
		return new MOrder()
				.setMaterialId(materialId)
 				.setQuantity(quantity);
	}

	@Override
	public void initializeBuyableGoods(int seller) throws MasterDataException, EconomyException {
		List<MProduct> products = company.getProducts(new Credentials(seller));
		for (MProduct product : products) {
			// Material
			Material material = new Material(EID.get('M'),product.getMaterialId(),product.getDescription());
			warehouse.createMaterial(material);
			// implicitely creating zero stock
		}
		
	}

	@Override
	public void postDelivery(MDelivery delivery) throws EconomyException {
		
		// warehouse
		MReceivedGoods goods = toReceivedGoods(delivery);
		warehouse.postReceivedGoods(goods);
	}
	
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

}
