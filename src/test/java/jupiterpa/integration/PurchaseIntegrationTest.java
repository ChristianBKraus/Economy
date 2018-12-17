package jupiterpa.integration;

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
import jupiterpa.IWarehouse.MStock;
import jupiterpa.sales.SalesService;
import jupiterpa.util.Credentials;
import jupiterpa.util.EID;
import jupiterpa.util.EconomyException;
import jupiterpa.util.SystemService;
import jupiterpa.util.masterdata.MasterDataMaster;
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
	
	
	Credentials serverCredentials = new Credentials(1);
	Credentials clientCredentials = new Credentials(2);
		
	@Before
	public void initializeServices() throws MasterDataException, EconomyException {
		logger.info("TEST initialize");
		
		company.initialize();		
		company.onboard(serverCredentials);
		company.onboard(clientCredentials);		
		
		system.logon(clientCredentials);		
	}
		
	private EID createMasterData(Credentials credentials, String description) throws EconomyException, MasterDataException {
		Credentials old = system.getCredentials();
		system.logon(credentials);

		MasterDataMaster<Material> materialMaster = ( (WarehouseService) warehouse).getMaterial(); 
		MasterDataMaster<MaterialSales> materialSalesMaster = ( (SalesService) sales).getMaterialSales(); 

		// Material
		Material m1 = new Material(EID.get('M'),null,description);
		materialMaster.create(m1);
		
		// MaterialSales 
		MaterialSales ms1 = new MaterialSales(m1.getMaterialId(),1.0,"EUR");
		materialSalesMaster.create(ms1);
		
		system.logon(old);
		return m1.getId();
	}
	private void postInitialStock(Credentials credentials, EID materialId, int quantity) throws EconomyException {
		Credentials old = system.getCredentials();
		system.logon(credentials);

		MStock stock = new MStock();
		stock.setMaterialId(materialId);
		stock.setQuantity(quantity);
		warehouse.postInitialStock(stock);
		
		system.logon(old);
	}
	private void initializeBuyableGoods(Credentials credentials) throws MasterDataException, EconomyException {
		Credentials old = system.getCredentials();
		purchasing.initializeBuyableGoods(credentials.getTenant());
		system.logon(old);
	}
	
	@Test(expected = EconomyException.class)
	public void purchaseNotInStock() throws EconomyException, MasterDataException {
		logger.info("TEST .......................................");
		logger.info("TEST purchase not in stock");
		logger.info("TEST .......................................");
		
		// Preparation on server
		EID materialId = createMasterData(serverCredentials,"Material 1");
		
		// Trigger Exception
    	purchasing.purchase(serverCredentials.getTenant(),materialId,1);
	}
	
	@Test
	public void purchase() throws EconomyException, MasterDataException {
		logger.info("TEST .......................................");
		logger.info("TEST purchase");
		logger.info("TEST .......................................");

		// Preperations
		EID materialId = createMasterData(serverCredentials, "Material 1");
		postInitialStock(serverCredentials,materialId,1);
		initializeBuyableGoods(serverCredentials);
		
		// Action on Client
   		purchasing.purchase(serverCredentials.getTenant(), materialId, 1);
   		
   		/// Check
   		// Stock of Server
		system.logon(serverCredentials);
   		MStock stock = warehouse.getStock(materialId);
   		assertThat(stock.getQuantity(),equalTo(0));
   		
   		// Stock of Client
   		system.logon(clientCredentials);
   		List<MStock> stock2 = warehouse.getCompleteStock();
   		assertThat(stock2.size(), equalTo(1));
   		MStock item = stock2.get(0);
   		assertThat(item.getQuantity(),equalTo(1));   		
	}
}
