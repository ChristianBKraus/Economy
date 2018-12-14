package jupiterpa.sales;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
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
	@Autowired SystemService systemService;
	
	@Getter MasterDataSlave<Material> material;
	@Getter MasterDataMaster<MaterialSales> materialSales;
	
	// Initialize
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		material = new MasterDataSlave<Material>(Material.TYPE, masterData, systemService);
		materialSales = new MasterDataMaster<MaterialSales>(MaterialSales.TYPE, masterData, systemService);
		materialSales.addParent(material);
	}
	@Override
	public void onboard(Credentials credentials) throws MasterDataException {
		material.onboard(credentials.getTenant());
		materialSales.onboard(credentials.getTenant());
	}
	
	// Queries
	@Override
	public List<MProduct> getProducts() {
		return materialSales.values().stream().map( 
				ms -> toProduct(ms)
			).collect(Collectors.toList());
	}
	@Override
	public MProduct getProduct(EID materialId) throws EconomyException {
		checkMaterial(materialId);
		MaterialSales ms = materialSales.get(materialId);		
		return toProduct(ms);
	}

	// Process
	@Override
	public EID postOrder(MOrder order) throws EconomyException {
		validate(order);
		SalesOrder salesOrder = fromOrder(order);
		
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
	MOrder validate(MOrder order) throws EconomyException {
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
		SalesOrder fromOrder(MOrder order);
		IWarehouse.MIssueGoods toIssueGoods(SalesOrder order);
		IFinancials.MSalesOrder toFinancialsDocument(SalesOrder order);
		MProduct toProduct(MaterialSales materialSales);
	}
	SalesOrder fromOrder(MOrder order) {
		return mapper.fromOrder(order)
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
