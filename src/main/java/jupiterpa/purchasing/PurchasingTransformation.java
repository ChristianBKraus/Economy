package jupiterpa.purchasing;

import org.mapstruct.Mapper;
import jupiterpa.ICompany;
import jupiterpa.IWarehouse;
import jupiterpa.ICompany.MOrder;
import jupiterpa.IMasterDataDefinition.Material;
import jupiterpa.IPurchasing.MDelivery;
import jupiterpa.util.EID;
import jupiterpa.util.masterdata.MasterDataClient;

public class PurchasingTransformation {
	
	MasterDataClient<Material> material;
	PurchasingMapper mapper;
	
	public PurchasingTransformation(MasterDataClient<Material> material, PurchasingMapper mapper) {
		this.material = material;
		this.mapper = mapper;
	}
	
	// Transformation
	@Mapper(componentModel = "spring")
	public interface PurchasingMapper {
		IWarehouse.MReceivedGoods toReceivedGoods(MDelivery delivery);
	}
	
	IWarehouse.MReceivedGoods toReceivedGoods(MDelivery delivery) {
		Material internal = null;
		for (Material m : material.values()) {
			if (m.getExternalId() == delivery.getMaterialId()) {
				internal = m;
			}
		}
		
		return mapper.toReceivedGoods(delivery)
			.setMaterialId(internal.getMaterialId())
			.setDeliveryId(delivery.getSalesOrderId())
			.setQuantity(-1 * delivery.getQuantity());
	}

	ICompany.MOrder toOrder(EID materialId, int quantity) {
		return new MOrder()
				.setMaterialId(materialId)
 				.setQuantity(quantity);
	}
	
	Material toMaterial(ICompany.MProduct product) {
		return new Material(EID.get('M'), product.getMaterialId(),product.getDescription() );
	}

}
