package jupiterpa.financials;

import org.mapstruct.Mapper;

import jupiterpa.ICompany;
import jupiterpa.ICompany.*;
import jupiterpa.IFinancials.*;
import jupiterpa.IMasterDataDefinition.*;
import jupiterpa.IMasterDataDefinition.Material.MaterialType;
import jupiterpa.util.*;
import jupiterpa.util.masterdata.MasterDataClient;

import static jupiterpa.financials.Account.*;

public class FinancialsTransformation {

	FinancialsMapper mapper;
	MasterDataClient<Material> material;
	MasterDataClient<MaterialSales> materialSales;
	MasterDataClient<MaterialPurchasing> materialPurchasing;
	AccountDetermination accountDetermination = new AccountDetermination();

	public FinancialsTransformation(FinancialsMapper mapper, MasterDataClient<Material> material,
			MasterDataClient<MaterialSales> materialSales, MasterDataClient<MaterialPurchasing> materialPurchasing) {
		this.mapper = mapper;
		this.material = material;
		this.materialSales = materialSales;
		this.materialPurchasing = materialPurchasing;
	}

	@Mapper(componentModel = "spring")
	public interface FinancialsMapper {
		FinancialsDocumentItem copy(FinancialsDocumentItem documentItem);
		FinancialsDocumentItem toDocumentItem(MMaterialDocument materialDocument);
		FinancialsDocumentItem toDocumentItem(MSalesOrder salesOrder);
		FinancialsDocumentItem toDocumentItem(MPurchaseOrder purchaseOrder);
		
		FinancialsDocumentItem toDocumentItem(MInvoice invoice);
		FinancialsDocumentItem toDocumentItem(MPayment payment);
		ICompany.MInvoice toInvoice(FinancialsDocument document);
		ICompany.MPayment toPayment(FinancialsDocument document);
	}
	
	private FinancialsDocument create(TransactionType transactionType, FinancialsDocumentItem item,
			Account account1, Account account2) {
		FinancialsDocument doc = new FinancialsDocument();
		doc.setDocumentNumber(EID.get('F'));
		doc.setTransactionType(transactionType);

		// Item
		item.setLineNumber(1);
		item.setAccount(account1.getNumber());
		doc.items.add(item);

		FinancialsDocumentItem item2 = mapper.copy(item);
		item2.setLineNumber(2);
		item2.setAccount(account2.getNumber());
		item2.setAmount(item2.getAmount() * -1);
		doc.items.add(item2);

		return doc;
	}

	////// Summary
	// Warenausgang:      Revenue     --> Goods        (MaterialDocument)
	// Ausgangsrechnung:  Receivables --> Revenue      (SalesOrder)
	// Eingangszahlung:   Bank        --> Receivables  (Payment)
	
	// Wareneingang.      Goods       --> WERE         (MaterialDocument)
	// Eingangsrechnung   WERE        --> Payable      (Invoice)
	// Ausgangszahlung    Payable     --> Bank         (Invoice delayed)
	///// Summary
	
	
	// Warenausgang:      Revenue     --> Goods 
	// Wareneingang.      Goods       --> WERE
	public FinancialsDocument toDocument(MMaterialDocument materialDocument)
			throws EconomyException {
		FinancialsDocumentItem item = mapper.toDocumentItem(materialDocument);
		Material m = material.get(materialDocument.getMaterialId());
		Account material_account, opposite_account;
	
		material_account = accountDetermination.determine("MM",m.getMaterialtype().toString());
		if (materialDocument.getQuantity() > 0) {
			opposite_account = GRIR;
		} else {
			opposite_account = REVENUE_OWN_GOODS;
		}
		switch (m.getMaterialtype()) {
		case RAW:
			if (materialDocument.getQuantity() > 0) {
				MaterialPurchasing mp = materialPurchasing.get(materialDocument.getMaterialId());
				item.setAmount(item.getQuantity() * mp.getPrice());
				item.setCurrency(mp.getCurrency());
			} else {
				item.setAmount(0.0);
				item.setCurrency("EUR");
			}
			break;
		case FINISHED:
			if (materialDocument.getQuantity() > 0) {
				MaterialSales ms = materialSales.get(materialDocument.getMaterialId());
				item.setAmount(item.getQuantity() * ms.getPrice());
				item.setCurrency(ms.getCurrency());
			} else {
				item.setAmount(0.0);
				item.setCurrency("EUR");
			};
			break;
		default:
			assert (false);
		}
		return create(TransactionType.MATERIAL_DOCUMENT, item, material_account, opposite_account); 
	}

	// Ausgangsrechnung:  Receivables --> Revenue
	public FinancialsDocument toDocument(MSalesOrder salesOrder)
			throws EconomyException {
		FinancialsDocumentItem item = mapper.toDocumentItem(salesOrder);
		if (salesOrder.getQuantity() > 0) {
			MaterialSales ms = materialSales.get(salesOrder.getMaterialId());
			item.setAmount(item.getQuantity() * ms.getPrice());
			item.setCurrency("EUR");
		} else {
			item.setAmount(0.0);
			item.setCurrency("EUR");
		}
		Account opposite_account = REVENUE_OWN_GOODS;
		//accountDetermination.determine("ME", material.get(salesOrder.getMaterialId()).getMaterialtype().toString());
		return create(TransactionType.SALES_ORDER, item, RECEIVABLES, opposite_account);
	}

	// predictive
	public FinancialsDocument toDocument(MPurchaseOrder purchaseOrder)
			throws EconomyException {
		FinancialsDocumentItem item = mapper.toDocumentItem(purchaseOrder);
		EID materialId = purchaseOrder.getMaterialId();
		Material m = material.get(materialId);
		MaterialType t = m.getMaterialtype();
		Account material_account = accountDetermination.determine("MM",t.toString());
		return create(TransactionType.PURCHASE_ORDER, item, material_account, BANK);
	}
	
	// Eingangszahlung:   Bank        --> Receivables  (Payment)
	public FinancialsDocument toDocument(MPayment payment) throws EconomyException {
		return create(TransactionType.PAYMENT, mapper.toDocumentItem(payment), BANK, RECEIVABLES);
	}

	// Eingangsrechnung   WERE        --> Payable      (Invoice)
	// Ausgangszahlung    Payable     --> Bank         (Invoice delayed)
	public FinancialsDocument toDocument(MInvoice invoice, boolean inv) throws EconomyException {
		if (inv) {
			return create(TransactionType.INVOICE, mapper.toDocumentItem(invoice), GRIR, PAYABLES);
		} else {
			return create(TransactionType.INVOICE, mapper.toDocumentItem(invoice), PAYABLES, BANK);
		}
	}

	public ICompany.MPayment toPayment(FinancialsDocument document) {
		return mapper.toPayment(document);
	}

	public ICompany.MInvoice toInvoice(FinancialsDocument document) {
		return mapper.toInvoice(document);
	}
}
