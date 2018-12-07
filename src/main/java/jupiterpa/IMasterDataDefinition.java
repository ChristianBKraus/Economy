package jupiterpa;
import jupiterpa.util.EID;
import lombok.*;


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
		String description;
		@Override public EID getId() { return materialId; }
	}
	
	@Data @AllArgsConstructor
	public class MaterialSales implements Type, HasParent {
		public static final String TYPE = "MaterialSales"; 
		EID materialId;
		String materialType;
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
