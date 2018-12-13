package jupiterpa.warehouse;

import java.util.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.*; 
import jupiterpa.ICompany.*;
import jupiterpa.IMasterDataDefinition.Material;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.util.*;
import jupiterpa.util.masterdata.MasterDataMaster;
import jupiterpa.warehouse.Stock.Item;

@Service
public class WarehouseService implements IWarehouse { 
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Marker DB = MarkerFactory.getMarker("DB");
	
	public String getName() { return "WAREHOUSE"; }
	
	@Autowired IFinancials financials; 
	@Autowired ICompany company;
	@Autowired IMasterDataServer masterData;
	@Autowired SystemService systemService;
	
	Stock stock;
    MasterDataMaster<Material> material;
    
	@Override
	public void initialize() throws EconomyException, MasterDataException {
    	material = new MasterDataMaster<Material>(Material.TYPE,masterData, systemService);
    	stock  = new Stock(systemService);
	}
	public MasterDataMaster<Material> getMaterialMaster() {
		return material;
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
			checkMaterial(id);
			MStock entry = toStock(stock.get(id));
			result.add(entry);
		};
		return result;
	}
	@Override
	public MStock getStock(EID id) throws EconomyException {
		checkMaterial(id);
		return toStock(stock.get(id));
	}
	@Override 
	public List<MStock> getCompleteStock() throws EconomyException {
		ArrayList<MStock> result = new ArrayList<MStock>();
		for (Item item :  stock.get()) {
			result.add(toStock(item));
		}
		return result;
	}

	// Process
	@Override
	public void createMaterial(Material material) throws MasterDataException, EconomyException {
		this.material.create(material);
		MStock s = new MStock();
		s.setMaterialId(material.getMaterialId());
		s.setQuantity(0);
		postInitialStock(s);
	}	
	@Override
	public void postInitialStock(MStock initialStock) throws EconomyException {
		validate(initialStock);
		MaterialDocument doc = fromStock(initialStock);
		post(doc);
		stock.change(doc,false);
		financials.postInitialGoods(toFinancialsDocument(doc));
	}
	
	
	@Override
	public void reserveGoods(MIssueGoods goods) throws EconomyException {
		validate(goods);
		
		stock.check(goods.getMaterialId(),goods.getQuantity());
		stock.reserve(goods.getMaterialId(),goods.getQuantity());
	}

	@Override
	public void postIssueGoods(MIssueGoods goods) throws EconomyException {
		validate(goods);
		MaterialDocument doc = fromIssueGoods(goods);
		
		post(doc);
		stock.change(doc, true);
		
		financials.postIssueGoods(toFinancialsDocument(doc));

		company.postDelivery(new Credentials(doc.getPartner()), toDelivery(doc));
	}

	@Override
	public void postReceivedGoods(MReceivedGoods goods) throws EconomyException {
		validate(goods);
		MaterialDocument doc = fromReceivedGoods(goods);
		
		stock.change(doc, false);
		
		financials.postReceivedGoods(toFinancialsDocument(doc));
	}
	
	//Post
	void post(MaterialDocument doc) { 
		logger.info(DB," POST {}",doc);
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
	@Autowired
	WarehouseMapper mapper;
		
	MaterialDocument fromStock(MStock stock) {
		return mapper.fromStock(stock)
				.setDocumentNumber(EID.get('M'));
	}
	MaterialDocument fromIssueGoods(MIssueGoods goods) {
		return mapper.fromIssuedGoods(goods)
				.setDocumentNumber(EID.get('M'))
				.setQuantity(-1 * goods.getQuantity());
	}
	MaterialDocument fromReceivedGoods(MReceivedGoods goods) {
		return mapper.fromReceivedGoods(goods)
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
