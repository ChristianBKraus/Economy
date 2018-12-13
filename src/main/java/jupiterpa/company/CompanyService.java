package jupiterpa.company;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.util.*;
import jupiterpa.ISales.MProduct;

@Service 
public class CompanyService implements ICompany {
	public String getName() { return "COMPANY"; }
	
	@Autowired IFinancials financials;
	@Autowired IPurchasing purchasing;
	@Autowired IWarehouse  warehouse;
	@Autowired ISales      sales;
	@Autowired SystemService system;
	
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		// do nothing
	}
	@Override
	public void onboard(Integer tenant) throws MasterDataException {
		// do nothing		
	}
	
	@Override
	public List<MProduct> getProducts(Credentials credentials) {
		Credentials old = system.getCredentials();
		system.logon(credentials);
		
		List<MProduct> products = sales.getProducts()
			.stream().map(p->mapper.map(p)).collect(Collectors.toList());
		
		system.logon(old);
		return products;
	}
	@Override
	public MProduct getProduct(Credentials credentials, EID materialId) throws EconomyException {
		Credentials old = system.getCredentials();
		system.logon(credentials);
		
		MProduct product =  mapper.map( sales.getProduct(materialId) );		

		system.logon(old);
		return product;
	}

	@Override
	public EID postOrder(Credentials credentials, MOrder order) throws EconomyException {
		Credentials old = system.getCredentials();
		order.setPartner(old.getTenant());
		system.logon(credentials);

		EID id =	 sales.postOrder(order);
		
		system.logon(old);
		return id;
	}

	@Override
	public void postDelivery(Credentials credentials, MDelivery delivery) throws EconomyException {
		Credentials old = system.getCredentials();
		system.logon(credentials);
		
		purchasing.postDelivery( toDelivery(delivery, old.getTenant()) );

		system.logon(old);
	}

	@Override
	public void postInvoice(Credentials credentials, MInvoice invoice) throws EconomyException {
		Credentials old = system.getCredentials();
		system.logon(credentials);

		financials.postInvoice(invoice);

		system.logon(old);
	}

	@Override
	public void postPayment(Credentials credentials, MPayment payment) throws EconomyException {
		Credentials old = system.getCredentials();
		system.logon(credentials);

		financials.postPayment(payment);

		system.logon(old);
	}
	
	@Autowired CompanyMapper mapper;
	@Mapper(componentModel = "spring")
	public interface CompanyMapper {
		MProduct map(ISales.MProduct product);
		IPurchasing.MDelivery map(ICompany.MDelivery delivery);
	}
	
	IPurchasing.MDelivery toDelivery(ICompany.MDelivery delivery, Integer partner) {
		return mapper.map(delivery).setPartner(partner);
	}

	
}
