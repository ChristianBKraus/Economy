package jupiterpa.warehouse;

import org.mapstruct.Mapper;

import jupiterpa.IFinancials;
import jupiterpa.ICompany.MDelivery;
import jupiterpa.IWarehouse.MIssueGoods;
import jupiterpa.IWarehouse.MReceivedGoods;
import jupiterpa.IWarehouse.MStock;

@Mapper(componentModel = "spring")
public interface WarehouseMapper { 
	
	MaterialDocument fromStock(MStock stock);
	MaterialDocument fromIssuedGoods(MIssueGoods goods); 
	MaterialDocument fromReceivedGoods(MReceivedGoods goods); 
	
	IFinancials.MMaterialDocument toFinancialsDocument(MaterialDocument doc); 
	MStock toStock(Stock.Item item); 
	MDelivery toDelivery(MaterialDocument doc); 
}
