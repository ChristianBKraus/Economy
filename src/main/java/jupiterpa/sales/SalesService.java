package jupiterpa.sales;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
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
import jupiterpa.sales.Sales.SalesMapper;

@Service
public class SalesService implements ISales {
	
	public String getName() { return "SALES"; }
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Marker DB = MarkerFactory.getMarker("DB");
	
	@Autowired IWarehouse warehouse;
	@Autowired IFinancials financials;
	
	@Autowired IMasterDataServer masterData;
	@Autowired SystemService systemService;
	
	@Autowired SalesMapper mapper;
	
	@Getter MasterDataSlave<Material> material;
	@Getter MasterDataMaster<MaterialSales> materialSales;
	
	Sales sales;
	
	// Initialize
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		material = new MasterDataSlave<Material>(Material.TYPE, masterData, systemService);
		materialSales = new MasterDataMaster<MaterialSales>(MaterialSales.TYPE, masterData, systemService);
		
		materialSales.addParent(material);
		
		sales = new Sales(material,materialSales,mapper);
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
				ms -> sales.toProduct(ms)
			).collect(Collectors.toList());
	}
	@Override
	public MProduct getProduct(EID materialId) throws EconomyException {
		sales.checkMaterial(materialId);
		MaterialSales ms = materialSales.get(materialId);		
		return sales.toProduct(ms);
	}

	// Process
	@Override
	public EID postOrder(MOrder order) throws EconomyException {
		sales.validate(order);
		SalesOrder salesOrder = sales.toSalesOrder(order);
		
		post(salesOrder);
		
		IWarehouse.MIssueGoods issueGoods = sales.toIssueGoods(salesOrder);
		warehouse.reserveGoods(issueGoods);
		warehouse.postIssueGoods(issueGoods);
		
		financials.salesOrder(sales.toFinancialsDocument(salesOrder));
		return salesOrder.getSalesOrderId();
	}
	
	// Post
	void post(SalesOrder order) {
		logger.info(DB," POST {}",order);
	}
	
}
