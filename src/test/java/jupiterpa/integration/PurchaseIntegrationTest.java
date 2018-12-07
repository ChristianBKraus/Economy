package jupiterpa.integration;

import java.util.Collection;
import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ch.qos.logback.classic.Level;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.*;
import jupiterpa.IMasterDataDefinition.*;
import jupiterpa.ISales.*;
import jupiterpa.IWarehouse.MStock;
import jupiterpa.sales.SalesService;
import jupiterpa.util.EID;
import jupiterpa.util.EconomyException;
import jupiterpa.util.masterdata.MasterDataMaster;
import jupiterpa.util.masterdata.MasterDataSlave;
import jupiterpa.warehouse.WarehouseService; 

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"test"})
public class PurchaseIntegrationTest {

	@Autowired ICompany company;
	@Autowired IPurchasing purchasing;
	@Autowired ISales sales;
	@Autowired IWarehouse warehouse;
	
	@Autowired IMasterDataServer masterdata;
	
	MasterDataMaster<Material> materialMaster;
	MasterDataSlave<Material> materialSlave;
	MasterDataMaster<MaterialSales> materialSalesMaster;
	
	@Before
	public void logging() {
//		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
//	    root.setLevel(Level.WARN);
	}
	
	@Before
	public void initializeServices() throws MasterDataException, EconomyException {
		masterdata.reset();
		
		warehouse.initialize();
		sales.initialize();
		
		materialMaster = ( (WarehouseService) warehouse).getMaterialMaster(); 
		materialSlave = ( (SalesService) sales).getMaterialSlave(); 
		materialSalesMaster = ( (SalesService) sales).getMaterialSalesMaster(); 
	}
	
	@Test
	public void registerMaterial() throws EconomyException, MasterDataException {

		Material m1 = new Material(EID.get('M'),"Material 1");
		materialMaster.create(m1);
		
		Collection<Object> materials = masterdata.getAll(Material.TYPE);
		assertThat(materials, contains(m1));
	}
	
	@Test
	public void replicateMaterial() throws EconomyException, MasterDataException {
		// Create Material --> replicate
		Material m1 = new Material(EID.get('M'),"Material 1");
		materialMaster.create(m1);
		
		Material result = materialSlave.get(m1.getId());
		assertThat(result, equalTo(m1));
	}
	
	@Test 
	public void createDependent() throws EconomyException, MasterDataException {
		// Create Material --> replicate
		Material m1 = new Material(EID.get('M'),"Material 1");
		materialMaster.create(m1);
		
		// Create MaterialSales 
		MaterialSales ms1 = new MaterialSales(m1.getMaterialId(),"P",1.0,"EUR");
		materialSalesMaster.create(ms1);
		
		MaterialSales result = materialSalesMaster.get(ms1.getId());
		assertThat(result, equalTo(ms1));
	}
	
	@Test
	public void createAndGetProduct() throws EconomyException, MasterDataException {
		// Create Material --> replicate
		Material m1 = new Material(EID.get('M'),"Material 1");
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
		Material m1 = new Material(EID.get('M'),description);
		materialMaster.create(m1);
		
		// MaterialSales 
		MaterialSales ms1 = new MaterialSales(m1.getMaterialId(),"P",1.0,"EUR");
		materialSalesMaster.create(ms1);
		
		return m1.getId();
	}
	
	@Test
	public void purchaseNotInStock() throws EconomyException, MasterDataException {
		createMasterData("Material 1");
		
    	List<MProduct> products = company.getProducts();
    	EID materialId = products.get(0).getMaterialId();
    	
    	boolean ex = false;
    	try {
    		purchasing.purchase(materialId,1);
    	} catch (EconomyException x) {
    		//expected
    		ex = true;
    	}
    	assertThat(ex, equalTo(true));        
	}
	
	@Test
	public void purchase() throws EconomyException, MasterDataException {
		EID id = createMasterData("Material 1");
		MStock stock = new MStock();
		stock.setMaterialId(id);
		stock.setQuantity(1);
		warehouse.postInitialStock(stock);
		
    	List<MProduct> products = company.getProducts();
    	EID materialId = products.get(0).getMaterialId();
    	
   		purchasing.purchase(materialId,1);
   		
   		stock = warehouse.getStock(materialId);
   		assertThat(stock.getQuantity(),equalTo(1)); // different company
	}
}
