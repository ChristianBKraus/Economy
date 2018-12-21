package jupiterpa.warehouse;

import java.util.*;
import lombok.Getter;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.*; 
import jupiterpa.IMasterDataDefinition.Material;
import jupiterpa.IMasterDataServer.MasterDataException;

import jupiterpa.util.*;
import jupiterpa.util.masterdata.MasterDataMaster;

import jupiterpa.warehouse.Stock.Item;
import jupiterpa.warehouse.WarehouseTransformation.WarehouseMapper;

@Service
public class WarehouseService implements IWarehouse { 

	public String getName() { return "WAREHOUSE"; }
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Marker DB = MarkerFactory.getMarker("DB");
		
	@Autowired IFinancials financials; 
	@Autowired ICompany company;
	
	@Autowired IMasterDataServer masterData;
	@Autowired SystemService systemService;
	
	Stock stock;
    @Getter MasterDataMaster<Material> material;
    
    @Autowired WarehouseMapper mapper;
    WarehouseTransformation transformation;
    
    // Initialize 
	@Override
	public void initialize() throws EconomyException, MasterDataException {
    	material = new MasterDataMaster<Material>(Material.TYPE,masterData, systemService);
    	stock  = new Stock(systemService);
    	
    	transformation = new WarehouseTransformation(material,mapper);
	}
	@Override
	public void onboard(Credentials credentials) throws MasterDataException {
		material.onboard(credentials.getTenant());
		stock.onboard(credentials.getTenant());
	}
    
	// Query
	@Override
	public List<MStock> getStock(List<EID> ids) throws EconomyException {
		List<MStock> result = new ArrayList<MStock>();		
		for (EID id : ids) {
			transformation.checkMaterial(id);
			MStock entry = transformation.toStock(stock.get(id));
			result.add(entry);
		};
		return result;
	}
	@Override
	public MStock getStock(EID id) throws EconomyException {
		transformation.checkMaterial(id);
		return transformation.toStock(stock.get(id));
	}
	@Override 
	public List<MStock> getCompleteStock() throws EconomyException {
		ArrayList<MStock> result = new ArrayList<MStock>();
		for (Item item :  stock.get()) {
			result.add(transformation.toStock(item));
		}
		return result;
	}

	// Master Data	
	@Override
	public void createMaterial(Material material) throws MasterDataException, EconomyException {
		this.material.create(material);
		MStock s = new MStock();
		s.setMaterialId(material.getMaterialId());
		s.setQuantity(0);
		postInitialStock(s);
	}	
	
	// Operations
	@Override
	public void postInitialStock(MStock initialStock) throws EconomyException {
		MaterialDocument doc = transformation.toMaterialDocument(initialStock);
		
		post(doc);
		stock.change(doc,false);
		
		if (initialStock.getQuantity() != 0)
			financials.postMaterialDocument(transformation.toFinancialsDocument(doc));
	}
	@Override
	public void reserveGoods(MIssueGoods goods) throws EconomyException {		
		stock.check(goods.getMaterialId(),goods.getQuantity());
		stock.reserve(goods.getMaterialId(),goods.getQuantity());
	}
	@Override
	public void postIssueGoods(MIssueGoods goods) throws EconomyException {
		MaterialDocument doc = transformation.toMaterialDocument(goods);
		
		post(doc);
		stock.change(doc, true);
		
		financials.postMaterialDocument(transformation.toFinancialsDocument(doc));
		company.postDelivery(new Credentials(doc.getPartner()), transformation.toDelivery(doc));
	}
	@Override
	public void postReceivedGoods(MReceivedGoods goods) throws EconomyException {
		MaterialDocument doc = transformation.toMaterialDocument(goods);
		
		stock.change(doc, false);
		
		financials.postMaterialDocument(transformation.toFinancialsDocument(doc));
	}
	
	//Post
	void post(MaterialDocument doc) { 
		logger.info(DB," POST {}",doc);
	} 
		
}
