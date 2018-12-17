package jupiterpa.warehouse;

import org.mapstruct.Mapper;
import jupiterpa.IFinancials;
import jupiterpa.ICompany.MDelivery;
import jupiterpa.IMasterDataDefinition.Material;
import jupiterpa.IWarehouse.MIssueGoods;
import jupiterpa.IWarehouse.MReceivedGoods;
import jupiterpa.IWarehouse.MStock;
import jupiterpa.util.EID;
import jupiterpa.util.EconomyException;
import jupiterpa.util.masterdata.MasterDataClient;

public class WarehouseTransformation {

	WarehouseMapper mapper;
	MasterDataClient<Material> material;
	
	public WarehouseTransformation(MasterDataClient<Material> material, WarehouseMapper mapper) {
		this.mapper = mapper;
		this.material = material;
	}

	// Validate
	MStock validate(MStock stock) throws EconomyException {
		checkMaterial(stock.getMaterialId());
		return stock;
	}
	MIssueGoods validate(MIssueGoods goods) throws EconomyException {
		checkMaterial(goods.getMaterialId());
		return goods;		
	}
	MReceivedGoods validate(MReceivedGoods goods) throws EconomyException {
		checkMaterial(goods.getMaterialId());
		return goods;
	}
	void checkMaterial(EID materialId) throws EconomyException {
		if (material.containsKey(materialId) == false) {
			throw new EconomyException("Material %s unknown",materialId);
		}
	}
	
	// Transform
	@Mapper(componentModel = "spring")
	public interface WarehouseMapper { 
		
		MaterialDocument toMaterialDocument(MStock stock);
		MaterialDocument toMaterialDocument(MIssueGoods goods); 
		MaterialDocument toMaterialDocument(MReceivedGoods goods); 
		
		IFinancials.MMaterialDocument toFinancialsDocument(MaterialDocument doc); 
		MStock toStock(Stock.Item item); 
		MDelivery toDelivery(MaterialDocument doc); 
	}
	
	MaterialDocument toMaterialDocument(MStock stock) throws EconomyException {
		validate(stock);
		return mapper.toMaterialDocument(stock)
				.setDocumentNumber(EID.get('M'));
	}
	MaterialDocument toMaterialDocument(MIssueGoods goods) throws EconomyException {
		validate(goods);
		return mapper.toMaterialDocument(goods)
				.setDocumentNumber(EID.get('M'))
				.setQuantity(-1 * goods.getQuantity());
	}
	MaterialDocument toMaterialDocument(MReceivedGoods goods) throws EconomyException {
		validate(goods);
		return mapper.toMaterialDocument(goods)
				.setDocumentNumber(EID.get('M'));
	}
	
	IFinancials.MMaterialDocument toFinancialsDocument(MaterialDocument doc) {
		return mapper.toFinancialsDocument(doc);
	}	
	MStock toStock(Stock.Item item) {
		return mapper.toStock(item);
	}
	MDelivery toDelivery(MaterialDocument doc) {
		return mapper.toDelivery(doc);
	}
}
