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
import jupiterpa.sales.SalesTransformation.SalesMapper;

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
	
	SalesTransformation transformation;
	
	// Initialize
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		material = new MasterDataSlave<Material>(Material.TYPE, masterData, systemService);
		materialSales = new MasterDataMaster<MaterialSales>(MaterialSales.TYPE, masterData, systemService);
		
		materialSales.addParent(material);
		
		transformation = new SalesTransformation(material,materialSales,mapper);
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
				ms -> transformation.toProduct(ms)
			).collect(Collectors.toList());
	}
	@Override
	public MProduct getProduct(EID materialId) throws EconomyException {
		transformation.checkMaterial(materialId);
		MaterialSales ms = materialSales.get(materialId);		
		return transformation.toProduct(ms);
	}

	// Process
	@Override
	public EID postOrder(MOrder order) throws EconomyException {
		SalesOrder salesOrder = transformation.toSalesOrder(order);
		
		post(salesOrder);
		
		IWarehouse.MIssueGoods issueGoods = transformation.toIssueGoods(salesOrder);
		warehouse.reserveGoods(issueGoods);
		warehouse.postIssueGoods(issueGoods);
		
		financials.postSalesOrder(transformation.toFinancialsDocument(salesOrder));
		return salesOrder.getSalesOrderId();
	}
	
	// Post
	void post(SalesOrder order) {
		logger.info(DB," POST {}",order);
	}
	
}
