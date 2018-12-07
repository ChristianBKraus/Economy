package jupiterpa.company;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.util.*;
import lombok.Setter;
import jupiterpa.ISales.MProduct;
import jupiterpa.ISales.MPurchaseOrder;

@Service 
public class CompanyService implements ICompany {
	public String getName() { return "COMPANY"; }
	
	@Autowired IFinancials financials;
	@Autowired IPurchasing purchasing;
	@Autowired IWarehouse  warehouse;
	@Autowired ISales      sales;
	
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		// do nothing
	}
	
	@Override
	public List<MProduct> getProducts() {
		return sales.getProducts()
			.stream().map(p->mapper.map(p)).collect(Collectors.toList());		
	}
	@Override
	public MProduct getProduct(EID materialId) throws EconomyException {
		return mapper.map( sales.getProduct(materialId) );		
	}

	@Override
	public EID postPurchaseOrder(MPurchaseOrder order) throws EconomyException {
		return sales.postPurchaseOrder(order); 
	}

	@Override
	public void postDelivery(MDelivery delivery) throws EconomyException {
		warehouse.postReceivedGoods(toReceivedGoods(delivery));
	}

	@Override
	public void postInvoice(MInvoice invoice) throws EconomyException {
		financials.postInvoice(invoice);
	}

	@Override
	public void postPayment(MPayment payment) throws EconomyException {
		financials.postPayment(payment);
	}
	
	@Autowired CompanyMapper mapper;
	@Mapper(componentModel = "spring")
	public interface CompanyMapper {
		MProduct map(ISales.MProduct product);
		IWarehouse.MReceivedGoods map(ICompany.MDelivery delivery);
	}
	
	IWarehouse.MReceivedGoods toReceivedGoods(MDelivery delivery) {
		return mapper.map(delivery)
			.setDeliveryId(delivery.getSalesOrderId())
			.setQuantity(-1 * delivery.getQuantity());
	}
	
}
