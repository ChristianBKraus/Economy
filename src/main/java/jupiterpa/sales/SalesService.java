package jupiterpa.sales;

import java.util.*;
import org.mapstruct.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.*;
import jupiterpa.util.*;
import jupiterpa.util.masterdata.*;
import jupiterpa.IMasterDataDefinition.*;
import jupiterpa.IMasterDataServer.MasterDataException;

@Service
public class SalesService implements ISales {
	
	public String getName() { return "SALES"; }
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Marker DB = MarkerFactory.getMarker("DB");
	
	@Autowired IWarehouse warehouse;
	@Autowired IFinancials financials;
	@Autowired IMasterDataServer masterData;
	MasterDataSlave<Material> material;
	MasterDataMaster<MaterialSales> materialSales;
	
	public MasterDataSlave<Material> getMaterialSlave() {
		return material;
	}
	public MasterDataMaster<MaterialSales> getMaterialSalesMaster() {
		return materialSales;
	}
	
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		material = new MasterDataSlave<Material>(Material.TYPE, masterData);
		materialSales = new MasterDataMaster<MaterialSales>(MaterialSales.TYPE, masterData);
		materialSales.addParent(material);
	}
	
	// Queries
	@Override
	public List<MProduct> getProducts() {
		List<MProduct> result = new ArrayList<MProduct>();
		for (MaterialSales ms : materialSales.values()) {
			Material m = material.get(ms.getMaterialId());
			result.add( toProduct(m,ms));
		}
		return result;
	}
	@Override
	public MProduct getProduct(EID materialId) throws EconomyException {
		checkMaterial(materialId);
		Material m = material.get(materialId);
		MaterialSales ms = materialSales.get(materialId);		
		return toProduct(m,ms);
	}

	// Process
	@Override
	public EID postPurchaseOrder(MPurchaseOrder purchaseOrder) throws EconomyException {
		validate(purchaseOrder);
		SalesOrder salesOrder = fromPurchaseOrder(purchaseOrder);
		
		post(salesOrder);
		
		IWarehouse.MIssueGoods issueGoods = toIssueGoods(salesOrder);
		warehouse.reserveGoods(issueGoods);
		warehouse.postIssueGoods(issueGoods);
		
		financials.salesOrder(toFinancialsDocument(salesOrder));
		return salesOrder.getSalesOrderId();
	}
	
	// Post
	void post(SalesOrder order) {
		logger.info(DB," POST {}",order);
	}
	
	// Validate
	MPurchaseOrder validate(MPurchaseOrder order) throws EconomyException {
		checkMaterial(order.getMaterialId());
		return order;
	}
	void checkMaterial(EID materialId) throws EconomyException {
		if (materialSales.containsKey(materialId) == false) 
			throw new EconomyException("Material %s does not have sales information", materialId);
	}

	// Transformation 
	@Autowired SalesMapper mapper;
	
	@Mapper(componentModel = "spring")
	public interface SalesMapper {
		SalesOrder fromPurchaseOrder(MPurchaseOrder order);
		IWarehouse.MIssueGoods toIssueGoods(SalesOrder order);
		IFinancials.MSalesOrder toFinancialsDocument(SalesOrder order);
		MProduct toProduct(MaterialSales materialSales);
	}
	SalesOrder fromPurchaseOrder(MPurchaseOrder order) {
		return mapper.fromPurchaseOrder(order)
			.setSalesOrderId(EID.get('S'));
	}
	IWarehouse.MIssueGoods toIssueGoods(SalesOrder order) {
		return mapper.toIssueGoods(order);
	}
	IFinancials.MSalesOrder toFinancialsDocument(SalesOrder order) {
		return mapper.toFinancialsDocument(order);
	}
	MProduct toProduct(Material m, MaterialSales ms) {
		return mapper.toProduct(ms)
			.setDescription(m.getDescription());
	}
	

}
