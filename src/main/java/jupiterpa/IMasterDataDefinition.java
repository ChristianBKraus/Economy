package jupiterpa;
import jupiterpa.util.EID;
import lombok.*;
import lombok.experimental.Accessors;


public interface IMasterDataDefinition {
	
	public interface Type {
		EID getId();
	}
	public interface HasParent {
		EID getParentId(String type);
	}
	
	@Data @AllArgsConstructor 
	public class Material implements Type {
		public static final String TYPE = "Material"; 
		EID materialId;
		EID externalId;
		String description;
		MaterialType materialtype;
		
		@Override public EID getId() { return materialId; }
		
		public enum MaterialType { RAW, UNFINIHSED, HELPER, FINISHED };
	}
	
	@Data @AllArgsConstructor
	public class MaterialSales implements Type, HasParent {
		public static final String TYPE = "MaterialSales"; 
		EID materialId;
		Double price;
		String currency;
		@Override public EID getId() { return materialId; }
		@Override public 
		EID getParentId(String type) {
			if (type == Material.TYPE) 
				return materialId;
			else 
				return null;
					
		}
	}

	@Data @AllArgsConstructor @NoArgsConstructor @Accessors(chain = true)
	public class MaterialPurchasing implements Type, HasParent {
		public static final String TYPE = "MaterialPurchasing"; 
		EID materialId;
		Double price;
		String currency;
		@Override public EID getId() { return materialId; }
		@Override public 
		EID getParentId(String type) {
			if (type == Material.TYPE) 
				return materialId;
			else 
				return null;
					
		}
	}
}
