package jupiterpa.sales;

import org.mapstruct.Mapper;
import jupiterpa.util.*;
import jupiterpa.IMasterDataDefinition.*;
import jupiterpa.util.masterdata.MasterDataClient;

import jupiterpa.ISales.*;
import jupiterpa.IFinancials;
import jupiterpa.IWarehouse;

public class SalesTransformation {
	MasterDataClient<Material> material;
	MasterDataClient<MaterialSales> materialSales;
	SalesMapper mapper;
	
	public SalesTransformation(MasterDataClient<Material> material, 
			     MasterDataClient<MaterialSales> materialSales,
			     SalesMapper mapper) {
		this.material = material;
		this.materialSales = materialSales;
		this.mapper = mapper;
	}
	
	// Validate
	MOrder validate(MOrder order) throws EconomyException {
		checkMaterial(order.getMaterialId());
		return order;
	}
	void checkMaterial(EID materialId) throws EconomyException {
		if (materialSales.containsKey(materialId) == false) 
			throw new EconomyException("Material %s does not have sales information", materialId);
	}

	// Transformation 
	
	@Mapper(componentModel = "spring")
	public interface SalesMapper {
		SalesOrder toSalesOrder(MOrder order);
		IWarehouse.MIssueGoods toIssueGoods(SalesOrder order);
		IFinancials.MSalesOrder toFinancialsDocument(SalesOrder order);
		MProduct toProduct(MaterialSales materialSales);
	}
	SalesOrder toSalesOrder(MOrder order) throws EconomyException {
		validate(order);
		return mapper.toSalesOrder(order)
			.setSalesOrderId(EID.get('S'));
	}
	IWarehouse.MIssueGoods toIssueGoods(SalesOrder order) {
		return mapper.toIssueGoods(order);
	}
	IFinancials.MSalesOrder toFinancialsDocument(SalesOrder order) {
		return mapper.toFinancialsDocument(order);
	}
	MProduct toProduct(MaterialSales ms) {
		Material m = material.get(ms.getMaterialId());
		return mapper.toProduct(ms)
			.setDescription(m.getDescription());
	}
	
}
