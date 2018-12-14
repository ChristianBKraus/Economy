package jupiterpa.company;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jupiterpa.*;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.util.*;

@Service 
public class CompanyService implements ICompany {
	public String getName() { return "COMPANY"; }
	
	@Autowired IFinancials financials;
	@Autowired IPurchasing purchasing;
	@Autowired IWarehouse  warehouse;
	@Autowired ISales      sales;
	
	@Autowired IMasterDataServer masterData;
	@Autowired SystemService system;
	
	// Initialization
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		system.logoff();

		masterData.reset();		
		
		warehouse.initialize();
		sales.initialize();
		purchasing.initialize();
		financials.initialize();
	}
	@Override
	public void onboard(Credentials credentials) throws MasterDataException {
		Credentials old = system.getCredentials();
		system.logon(credentials);		
		
		warehouse.onboard(credentials);
		sales.onboard(credentials);
		purchasing.onboard(credentials);
		financials.onboard(credentials);
		
		system.logon(old);
	}
	
	// Queries
	@Override
	public List<MProduct> getProducts(Credentials credentials) {
		Credentials old = system.getCredentials();
		system.logon(credentials);
		
		List<MProduct> products = sales.getProducts()
			.stream().map(p->mapper.from(p)).collect(Collectors.toList());
		
		system.logon(old);
		return products;
	}
	@Override
	public MProduct getProduct(Credentials credentials, EID materialId) throws EconomyException {
		Credentials old = system.getCredentials();
		system.logon(credentials);
		
		MProduct product =  mapper.from( sales.getProduct(materialId) );		

		system.logon(old);
		return product;
	}

	// Operations
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
	
	@Override
	public EID postOrder(Credentials credentials, MOrder order) throws EconomyException {
		Credentials old = system.getCredentials();
		system.logon(credentials);

		EID id = sales.postOrder( to(order, old.getTenant()) );
		
		system.logon(old);
		return id;
	}

	@Override
	public void postDelivery(Credentials credentials, MDelivery delivery) throws EconomyException {
		Credentials old = system.getCredentials();
		system.logon(credentials);
		
		purchasing.postDelivery( to(delivery, old.getTenant()) );

		system.logon(old);
	}

	// Mapping
	@Autowired CompanyMapper mapper;
	@Mapper(componentModel = "spring")
	public interface CompanyMapper {
		ICompany.MProduct from(ISales.MProduct product);
		IPurchasing.MDelivery to(ICompany.MDelivery delivery);
		ISales.MOrder to(ICompany.MOrder order);
	}
	
	ICompany.MProduct from(ISales.MProduct product) {
		return mapper.from(product);
	}
	IPurchasing.MDelivery to(ICompany.MDelivery delivery, Integer partner) {
		return mapper.to(delivery).setPartner(partner);
	}
	ISales.MOrder to(ICompany.MOrder order, Integer partner) {
		return mapper.to(order).setPartner(partner);
	}

	
}
