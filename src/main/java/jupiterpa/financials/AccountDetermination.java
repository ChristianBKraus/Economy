package jupiterpa.financials;

import static jupiterpa.financials.Account.BANK;
import static jupiterpa.financials.Account.CASH;
import static jupiterpa.financials.Account.EQUITY;
import static jupiterpa.financials.Account.EXPENSE_FOREIGN_MATERIAL;
import static jupiterpa.financials.Account.EXPENSE_HELPER_MATERIAL;
import static jupiterpa.financials.Account.EXPENSE_PACKAGING;
import static jupiterpa.financials.Account.EXPENSE_PRODUCTION_MATERIAL;
import static jupiterpa.financials.Account.EXPENSE_RAW_MATERIAL;
import static jupiterpa.financials.Account.FINISHED_MATERIAL;
import static jupiterpa.financials.Account.FOREIGN_MATERIAL;
import static jupiterpa.financials.Account.GRIR;
import static jupiterpa.financials.Account.HELPER_MATERIAL;
import static jupiterpa.financials.Account.MISC_MATERIAL;
import static jupiterpa.financials.Account.PAYABLES;
import static jupiterpa.financials.Account.PRODUCTION_MATERIAL;
import static jupiterpa.financials.Account.RAW_MATERIAL;
import static jupiterpa.financials.Account.RECEIVABLES;
import static jupiterpa.financials.Account.REVENUE_OWN_GOODS;
import static jupiterpa.financials.Account.REVENUE_OWN_SERVICES;
import static jupiterpa.financials.Account.REVENUE_TRADE_GOODS;
import static jupiterpa.financials.Account.UNFINISHED_MATERIAL;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import jupiterpa.util.EconomyException;

public class AccountDetermination {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Marker MARKER = MarkerFactory.getMarker("UTILITY");
	
	Map<String,Account> accounts = new HashMap<String,Account>();

	public AccountDetermination() {
		accounts.put("MM/RAW", RAW_MATERIAL);
		accounts.put("MM/FOREIGN", FOREIGN_MATERIAL);
		accounts.put("MM/HELPER", HELPER_MATERIAL);
		accounts.put("MM/PRODUCTION", PRODUCTION_MATERIAL);
		accounts.put("MM/MISC", MISC_MATERIAL);
		accounts.put("MM/UNFINISHED", UNFINISHED_MATERIAL);
		accounts.put("MM/FINISHED", FINISHED_MATERIAL);
		
		
		accounts.put("MR/GOODS", REVENUE_OWN_GOODS);
		accounts.put("MR/TRADE", REVENUE_TRADE_GOODS);
		accounts.put("MR/SERVICES", REVENUE_OWN_SERVICES);
		 
		accounts.put("ME/RAW", EXPENSE_RAW_MATERIAL);
		accounts.put("ME/FOREIGN", EXPENSE_FOREIGN_MATERIAL);
		accounts.put("ME/HELPER", EXPENSE_HELPER_MATERIAL);
		accounts.put("ME/PRODUCTION", EXPENSE_PRODUCTION_MATERIAL);
		accounts.put("ME/PACKAGING", EXPENSE_PACKAGING);
		
		accounts.put("GRIR", GRIR);
		
		accounts.put("AR", RECEIVABLES);
		accounts.put("AP", PAYABLES);
		
		accounts.put("BANK", BANK);
		accounts.put("CASH", CASH);
		accounts.put("EQUITY", EQUITY);
	}	
		
	public Account determine(String account_class, String sub_class) throws EconomyException {				
		String key = account_class;
		if (sub_class.equals("") == false) {
			key = account_class + "/" + sub_class;
		}
		Account result = accounts.get(key);
		if (result == null) {
			logger.error(MARKER,"Account Determination for {}/{} not failed",account_class, sub_class);
			throw new EconomyException("Account Determination for %s/%s not failed",account_class, sub_class);
		}
		return result;
	}
}
