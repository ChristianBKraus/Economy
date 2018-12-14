package jupiterpa.integration;

import java.util.Collection;
import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.*;
import jupiterpa.IMasterDataDefinition.*;
import jupiterpa.ISales.*;
import jupiterpa.IWarehouse.MStock;
import jupiterpa.sales.SalesService;
import jupiterpa.util.Credentials;
import jupiterpa.util.EID;
import jupiterpa.util.EconomyException;
import jupiterpa.util.SystemService;
import jupiterpa.util.masterdata.MasterDataMaster;
import jupiterpa.util.masterdata.MasterDataSlave;
import jupiterpa.warehouse.WarehouseService; 

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"test"})
public class PurchaseIntegrationTest {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired ICompany company;
	@Autowired IPurchasing purchasing;
	@Autowired ISales sales;
	@Autowired IWarehouse warehouse;
	@Autowired IFinancials financials;
	
	@Autowired IMasterDataServer masterdata;
	@Autowired SystemService system;
	
	MasterDataMaster<Material> materialMaster;
	MasterDataSlave<Material> materialSlave;
	MasterDataMaster<MaterialSales> materialSalesMaster;
	
	Credentials serverCredentials = new Credentials(1);
	Credentials clientCredentials = new Credentials(2);
	
	@Before
	public void logging() {
//		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
//	    root.setLevel(Level.WARN);
	}
	
	@Before
	public void initializeServices() throws MasterDataException, EconomyException {
		logger.info("TEST initialize");
		
		company.initialize();		
		company.onboard(serverCredentials);
		company.onboard(clientCredentials);		
		
		system.logon(serverCredentials);		

		materialMaster = ( (WarehouseService) warehouse).getMaterialMaster(); 
		materialSlave = ( (SalesService) sales).getMaterialSlave(); 
		materialSalesMaster = ( (SalesService) sales).getMaterialSalesMaster(); 
	}
	
	@Test
	public void registerMaterial() throws EconomyException, MasterDataException {
		logger.info("TEST register material");

		system.logon(serverCredentials);		
		
		Material m1 = new Material(EID.get('M'),null,"Material 1");
		materialMaster.create(m1);
		
		Collection<Object> materials = masterdata.getAll(Material.TYPE);
		assertThat(materials, contains(m1));
	}
	
	@Test
	public void replicateMaterial() throws EconomyException, MasterDataException {
		logger.info("TEST replicateMaterial");

		// Create Material --> replicate
		Material m1 = new Material(EID.get('M'),null,"Material 1");
		materialMaster.create(m1);
		
		Material result = materialSlave.get(m1.getId());
		assertThat(result, equalTo(m1));
	}
	
	@Test 
	public void createDependent() throws EconomyException, MasterDataException {
		logger.info("TEST create Dependent");
		
		// Create Material --> replicate
		Material m1 = new Material(EID.get('M'),null,"Material 1");
		materialMaster.create(m1);
		
		// Create MaterialSales 
		MaterialSales ms1 = new MaterialSales(m1.getMaterialId(),"P",1.0,"EUR");
		materialSalesMaster.create(ms1);
		
		MaterialSales result = materialSalesMaster.get(ms1.getId());
		assertThat(result, equalTo(ms1));
	}
	
	@Test
	public void createAndGetProduct() throws EconomyException, MasterDataException {
		logger.info("TEST create and get product");
		
		// Create Material --> replicate
		Material m1 = new Material(EID.get('M'),null,"Material 1");
		materialMaster.create(m1);
		
		// Create MaterialSales 
		MaterialSales ms1 = new MaterialSales(m1.getMaterialId(),"P",1.0,"EUR");
		materialSalesMaster.create(ms1);
		
		//Check whether sales got it
		MProduct product = new MProduct();
		product.setMaterialId(ms1.getMaterialId());
		product.setDescription(m1.getDescription());
		product.setPrice(ms1.getPrice());
		product.setCurrency(ms1.getCurrency());
		List<MProduct> products = sales.getProducts();
		assertThat(products, contains(product));
	}
	
	private EID createMasterData(String description) throws EconomyException, MasterDataException {
		// Material
		Material m1 = new Material(EID.get('M'),null,description);
		materialMaster.create(m1);
		
		// MaterialSales 
		MaterialSales ms1 = new MaterialSales(m1.getMaterialId(),"P",1.0,"EUR");
		materialSalesMaster.create(ms1);
		
		return m1.getId();
	}
	
	@Test
	public void purchaseNotInStock() throws EconomyException, MasterDataException {
		logger.info("TEST purchase not in stock");
		
		// Preparation on server
		system.logon(serverCredentials);
		createMasterData("Material 1");
		
		// Actions of Client
		system.logon(clientCredentials);
    	List<ICompany.MProduct> products = company.getProducts(serverCredentials);
    	EID materialId = products.get(0).getMaterialId();
    	
    	boolean ex = false;
    	try {
    		purchasing.purchase(serverCredentials.getTenant(),materialId,1);
    	} catch (EconomyException x) {
    		//expected
    		ex = true;
    	}
    	assertThat(ex, equalTo(true));        
	}
	
	@Test
	public void purchase() throws EconomyException, MasterDataException {
		logger.info("TEST .......................................");
		logger.info("TEST purchase");
		logger.info("TEST .......................................");
		
		// Preparation on Server
		system.logon(serverCredentials); 
		EID id = createMasterData("Material 1");
		MStock stock = new MStock();
		stock.setMaterialId(id);
		stock.setQuantity(1);
		warehouse.postInitialStock(stock);
		
		// Preparation on Client
		system.logon(clientCredentials);
		purchasing.initializeBuyableGoods(serverCredentials.getTenant());
//		id = createMasterData("Material 2");
//		stock = new MStock();
//		stock.setMaterialId(id);
//		stock.setQuantity(0);
//		warehouse.postInitialStock(stock);
		
		// Action on Client
		List<ICompany.MProduct> products = company.getProducts(serverCredentials);
    	EID materialId = products.get(0).getMaterialId();
    	
   		purchasing.purchase(serverCredentials.getTenant(),materialId,1);
   		
   		// Stock of Server
		system.logon(serverCredentials);
   		stock = warehouse.getStock(materialId);
   		assertThat(stock.getQuantity(),equalTo(0));
   		
   		// Stock of Server
   		system.logon(clientCredentials);
   		List<MStock> stock2 = warehouse.getCompleteStock();
   		assertThat(stock2.size(), equalTo(1));
   		MStock item = stock2.get(0);
   		assertThat(item.getQuantity(),equalTo(1));   		
	}
}
