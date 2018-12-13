package jupiterpa;

import java.util.*;
import lombok.*;

import jupiterpa.util.*;

public interface IMasterDataServer {
	
	void registerType(String type, IMasterDataClient client)  throws MasterDataException;
	void registerClient(String type, IMasterDataClient client)  throws MasterDataException;
	
	void reset();	
	
	Object get(EIDTyped key) throws MasterDataException;
	Collection<Object> getAll(String type) throws MasterDataException;
	
	void post(EIDTyped key, Object entry)  throws MasterDataException;
	void postDependent(EIDTyped key, Object entry, EIDTyped parent) throws MasterDataException;
	void delete(EIDTyped key)  throws MasterDataException;
	
	@Data @AllArgsConstructor
	public class EIDTyped {
		String type;
		EID    id;
		@Override 
		public String toString() {
			return id  + "/" + type;
		}
	}
	
	@Data @AllArgsConstructor 
	public class TenantType {
		int tenant;
		String type;
	}
	
	public class MasterDataException extends Exception {
		private static final long serialVersionUID = 1L;
		public MasterDataException(String msg) { super(msg); };
	}
}
