package jupiterpa.financials;

public enum Account {
/// Umlaufvermögen
	/// Materialbestand
	// 20 Roh Hilfs und Betriebsstoffe
	RAW_MATERIAL(2000),
	FOREIGN_MATERIAL(2010),
	HELPER_MATERIAL(2020), 
	PRODUCTION_MATERIAL(2030),
	MISC_MATERIAL(2040),
	// 21 unfertige Erzeugnisse
	UNFINISHED_MATERIAL(2100),
	UNFINISHED_SERVICES(2190),
	// 22 fertige Erzeugnisse
	FINISHED_MATERIAL(2200),
	FINISHED_TRADE_GOODS(2280),
	// 23 geleistete Anzahlungen a Vorräten
	DOWN_PAYMENT_MATERIAL(2300),
	
	/// Forderungen an Lieferungen
	RECEIVABLES(2400),
	
	// Bank
	BANK(2800),
	CASH(2880),	
	
/// Eigenkapital
	EQUITY(3000),
	
/// Verbindlichkeiten
	PAYABLES(4400),

	
/// Erträge
	/// Umsatzerträge
	REVENUE_OWN_GOODS(5000),
	REVENUE_OWN_SERVICES(5050),
	// Umsatzerlöse für Waren
	REVENUE_TRADE_GOODS(5100),
		
	/// Bestandsveränderungen
	INVENTORY_CHANGE(5200),
	INVENTORY_CHANGE_UNFINISHED(5201),
	INVENTORY_CHANGE_FINISHED(5202),
	
	
/// Aufwand
	// MaterialAufwand
	EXPENSE_RAW_MATERIAL(6000),
	EXPENSE_FOREIGN_MATERIAL(6010),
	EXPENSE_HELPER_MATERIAL(6020), 
	EXPENSE_PRODUCTION_MATERIAL(6030),
	EXPENSE_PACKAGING(6040),
		
	/// Verrechnungskonto
	GRIR(7999);

	int number;
	Account(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

}
