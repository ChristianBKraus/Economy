package jupiterpa.masterdata;

import java.util.*;
import lombok.*;

import jupiterpa.IMasterDataServer.*;

@EqualsAndHashCode
public class Dependency {
	
	EIDTyped id;
	@EqualsAndHashCode.Exclude
	EIDTyped parent;
	@EqualsAndHashCode.Exclude
	List<EIDTyped> childs = new ArrayList<EIDTyped>();

	static Map<EIDTyped,Dependency> dependencies = new HashMap<EIDTyped,Dependency>();
	
	private Dependency(EIDTyped id, EIDTyped parent) throws MasterDataException {
		this.id = id;
		this.parent = parent;
		if (parent != null) {
			Dependency dep = dependencies.get(parent);
			if (dep == null) {
				throw new MasterDataException("Parent " + parent + " does not exist for dependent object " + id);
			} else 
				dep.childs.add(id);
		}
	}
	public static void add(EIDTyped id, EIDTyped parent) throws MasterDataException {
		Dependency dep = new Dependency(id,parent);
		dependencies.put(id, dep);
	}
	public static void delete(EIDTyped id) throws MasterDataException {
		Dependency dep = dependencies.get(id);
		if (dep == null) 
			throw new MasterDataException("Dependency for " + id + " does not exist");
		if (dep.childs.size() > 0)
			throw new MasterDataException("For entry " + id + " there are still child dependencies");
		if (dep.parent != null) {
			Dependency parent = dependencies.get(dep.parent);
			boolean removed = parent.childs.remove(id);
			System.out.println(removed);
		}
		dependencies.remove(id,dep);
	}
}
