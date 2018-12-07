package jupiterpa.warehouse;

import lombok.*;
import lombok.experimental.*;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import jupiterpa.util.*;

public class Stock {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Marker DB = MarkerFactory.getMarker("DB");

	Map<EID,Item> stock = new HashMap<EID,Item>();
	Map<EID,Item> reserved = new HashMap<EID,Item>();
	
	public Item get(EID materialId) {
		return stock.getOrDefault(materialId, new Item(materialId,0,""));
	}
	public List<Item> get(String type) {
		return stock.values().stream().filter( i -> i.getType() == "P" ).collect(Collectors.toList());
	}
	
	public void check(EID materialId, int quantity) throws EconomyException {
		Item item = stock.get(materialId);
		if (item == null) {
			throw new EconomyException("Material %s not in stock",materialId);
		}
		if (item.getQuantity() < quantity) {
			throw new EconomyException("Not enough material %s in stock",materialId);
		}
	}
	public void reserve(EID materialId, int quantity) {
		Item item = reserved.get(materialId);
		if (item == null) {
			reserved.put(materialId,new Item(materialId,quantity,""));
		} else {
			item.setQuantity(item.getQuantity()+quantity);
			if (item.getQuantity() == 0) {
				reserved.remove(materialId);
			} else {
				reserved.replace(materialId, item);
			};
		}
	}
	public void change(MaterialDocument doc, boolean wasReserved) {
		Item item = stock.get(doc.getMaterialId());
		if (item == null) {
			item = new Item(doc.getMaterialId(),doc.getQuantity(),"P"); // !!!!!!!!!!!!!
		} else {
			item.setQuantity(item.getQuantity() + doc.getQuantity());
		}
		stock.put(doc.getMaterialId(), item);
		logger.info(DB,"Stock change of {} by {} to {}",doc.getMaterialId(),doc.getQuantity(),item.getQuantity());
		if (wasReserved)
			reserve(doc.getMaterialId(),-1 * doc.getQuantity());
	}


	@Data @Accessors(chain = true) 
	public class Item {		
		EID materialId;
		int quantity;
		String type;
		
		public Item(EID materialId, int quantity, String type) {
			this.materialId = materialId;
			this.quantity = quantity;
			this.type = type;
		}
	}
}
