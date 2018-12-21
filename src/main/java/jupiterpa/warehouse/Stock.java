package jupiterpa.warehouse;

import lombok.*;
import lombok.experimental.*;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import jupiterpa.util.*;
import jupiterpa.util.masterdata.TenantTable;

public class Stock {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Marker DB = MarkerFactory.getMarker("DB");

	SystemService system;
	TenantTable<Item> stock;
	TenantTable<Item> reserved;
	
	public Stock(SystemService system) {
		stock = new TenantTable<Item>(system);
		reserved = new TenantTable<Item>(system);
		this.system = system;
	}
	public void onboard(Integer tenant) {
		stock.onboard(tenant);
		reserved.onboard(tenant);
	}
	
	public Item get(EID materialId) {
		return stock.get().getOrDefault(materialId, new Item(materialId,0));
	}
	public Collection<Item> get() {
		return stock.get().values();
	}
	
	public void check(EID materialId, int quantity) throws EconomyException {
		Item item = stock.get().get(materialId);
		if (item == null) {
			throw new EconomyException("Material %s not in stock",materialId);
		}
		if (item.getQuantity() < quantity) {
			throw new EconomyException("Not enough material %s in stock",materialId);
		}
	}
	public void reserve(EID materialId, int quantity) {
		Item item = reserved.get().get(materialId);
		if (item == null) {
			reserved.get().put(materialId,new Item(materialId,quantity));
		} else {
			item.setQuantity(item.getQuantity()+quantity);
			if (item.getQuantity() == 0) {
				reserved.get().remove(materialId);
			} else {
				reserved.get().replace(materialId, item);
			};
		}
	}
	public void change(MaterialDocument doc, boolean wasReserved) {
		Item item = stock.get().get(doc.getMaterialId());
		if (item == null) {
			item = new Item(doc.getMaterialId(),doc.getQuantity()); // !!!!!!!!!!!!!
		} else {
			item.setQuantity(item.getQuantity() + doc.getQuantity());
		}
		stock.get().put(doc.getMaterialId(), item);
		logger.info(DB,"Stock change of {} by {} to {}",doc.getMaterialId(),doc.getQuantity(),item.getQuantity());
		logger.trace(DB,"Stock-{}: {}", system.getCredentials().getTenant(),stock.get().values());
		if (wasReserved)
			reserve(doc.getMaterialId(),-1 * doc.getQuantity());
	}


	@Data @Accessors(chain = true) 
	public class Item {		
		EID materialId;
		int quantity;
		
		public Item(EID materialId, int quantity) {
			this.materialId = materialId;
			this.quantity = quantity;
		}
	}
}
