package jupiterpa.financials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.*;
import jupiterpa.util.*;
import jupiterpa.util.masterdata.MasterDataSlave;
import jupiterpa.ICompany.*;
import jupiterpa.IMasterDataDefinition.*;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.financials.FinancialsTransformation.FinancialsMapper;

@Service
public class FinancialsService implements IFinancials {
	public String getName() { return "FINANCIALS"; }
	
	@Autowired IMasterDataServer masterData;
	@Autowired SystemService system;
	
	@Autowired FinancialsMapper mapper;
	FinancialsTransformation transformation;
	
	MasterDataSlave<Material> material;
	MasterDataSlave<MaterialSales> materialSales;
	MasterDataSlave<MaterialPurchasing> materialPurchasing;
	
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		material = new MasterDataSlave<Material>(Material.TYPE, masterData, system);
		materialSales = new MasterDataSlave<MaterialSales>(MaterialSales.TYPE, masterData, system);
		materialPurchasing = new MasterDataSlave<MaterialPurchasing>(MaterialPurchasing.TYPE, masterData, system);

		transformation = new FinancialsTransformation(mapper, material, materialSales, materialPurchasing);
		
	}
	@Override
	public void onboard(Credentials credentials) throws MasterDataException {
		material.onboard(credentials.getTenant());
		materialSales.onboard(credentials.getTenant());
		materialPurchasing.onboard(credentials.getTenant());
	}
	
	@Override
	public void postInvoice(MInvoice invoice) throws EconomyException {
		
		// Invoice 
		FinancialsDocument doc = transformation.toDocument(invoice,true);		
		post(doc);
		
		// Payment
		doc = transformation.toDocument(invoice,false);		
		post(doc);
	}

	@Override 
	public void postPayment(MPayment payment) throws EconomyException {
		FinancialsDocument doc = transformation.toDocument(payment);
		 
		post(doc);
	}

	@Override
	public void postMaterialDocument(MMaterialDocument matDoc) throws EconomyException {
		FinancialsDocument doc = transformation.toDocument(matDoc);
		
		post(doc);				
	}

	@Override
	public void postSalesOrder(MSalesOrder order) throws EconomyException {
		FinancialsDocument doc = transformation.toDocument(order);
		
		post(doc);				
	}

	private void post(FinancialsDocument doc) {
		
	}
	@Override
	public void postPurchaseOrder(MPurchaseOrder order) throws EconomyException {
		FinancialsDocument doc;
		doc = transformation.toDocument(order);
	}

}
