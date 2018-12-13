package jupiterpa.financials;

import org.springframework.stereotype.Service;

import jupiterpa.*;
import jupiterpa.util.*;

import jupiterpa.ICompany.*;
import jupiterpa.IMasterDataServer.MasterDataException;

@Service
public class FinancialsService implements IFinancials {
	public String getName() { return "FINANCIALS"; }
	
	@Override
	public void initialize() throws EconomyException, MasterDataException {
		// do nothing
	}
	@Override
	public void onboard(Credentials credentials) throws MasterDataException {
		// do nothing		
	}
	
	@Override
	public void postInvoice(MInvoice invoice) throws EconomyException {
		// TODO Auto-generated method stub
	}

	@Override
	public void postPayment(MPayment payment) throws EconomyException {
		// TODO Auto-generated method stub
	}

	@Override
	public void postInitialGoods(MMaterialDocument goods) throws EconomyException {
		
	}

	@Override
	public void postIssueGoods(MMaterialDocument goods) throws EconomyException {
		// TODO
	}

	@Override
	public void postReceivedGoods(MMaterialDocument goods) throws EconomyException {
		// TODO
	}
	@Override
	public void salesOrder(MSalesOrder order) throws EconomyException {
		// TODO Auto-generated method stub
	}


}
