package jupiterpa.masterdata;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import jupiterpa.IMasterDataServer;
import jupiterpa.IMasterDataServer.EIDTyped;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.IMasterDataClient;
import jupiterpa.util.EID;
import lombok.Data;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"mock","test"})
public class MasterDataTest {
	IMasterDataServer md;
	ServiceMock master;
	ServiceMock dependent;
	
	public class ServiceMock implements  IMasterDataClient {
		String name;
		List<EIDTyped> invalidated = new ArrayList<EIDTyped>();
		public ServiceMock(String name) {
			this.name = name;
		}
		
		@Override
		public void invalidate(EIDTyped id) {
			invalidated.add(id);
		}

		@Override
		public void initialLoad() throws MasterDataException {
			// TODO Auto-generated method stub
			
		}
	}
	
	@Data
	public class DataMock {
		EIDTyped eid;
	}
	
	@Before
	public void setup() {
		md = new MasterDataService();
		master = new ServiceMock("Master");
		dependent = new ServiceMock("Dep");
	}
	private DataMock getMock(String type, EID id) throws MasterDataException {
		EIDTyped eid = new EIDTyped(type,id);
		DataMock data = new DataMock();
		data.setEid(eid);
		return data;
	}
	
	@Test
	public void oneMaster() throws MasterDataException {
		
		md.registerType("T1", master);
		
		DataMock input = getMock("T1",EID.get('1'));		
		md.post(input.getEid(),input);
		DataMock result = (DataMock) md.get(input.getEid());
		assertThat(result, equalTo(input));
		
	}
	@Test 
	public void masterClient() throws MasterDataException {
		
		md.registerType("T1", master);
		md.registerClient("T1", dependent);
		
		DataMock input = getMock("T1",EID.get('1'));
		md.post(input.getEid(), input);
		assertThat(dependent.invalidated, contains(input.getEid()));
	}
	
	@Test 
	public void lateDependent() throws MasterDataException {
		md.registerType("T1", master);

		DataMock input1 = getMock("T1",EID.get('1'));
		md.post(input1.getEid(), input1);

		md.registerClient("T1", dependent);
		
		DataMock input2 = getMock("T1",EID.get('1'));
		md.post(input2.getEid(), input2);
		
		assertThat(dependent.invalidated, contains(input2.getEid()));
		assertThat(dependent.invalidated.size(), equalTo(1));
	}
	
	@Test
	public void postDependent() throws MasterDataException {
		
		md.registerType("T1", master);
		md.registerType("T2", master);
		
		md.registerClient("T1", dependent);
		md.registerClient("T2", dependent);

		DataMock input1 = getMock("T1",EID.get('1'));
		md.post(input1.getEid(), input1);

		DataMock input2 = getMock("T2",EID.get('2'));
		md.postDependent(input2.getEid(), input2, input1.getEid());
		
		assertThat(dependent.invalidated, hasItem(input1.getEid()));
		assertThat(dependent.invalidated, hasItem(input2.getEid()));
	}
	
	@Test
	public void delete() throws MasterDataException {
		
		md.registerType("T1", master);		
		md.registerClient("T1", dependent);

		DataMock input1 = getMock("T1",EID.get('1'));
		md.post(input1.getEid(), input1);
		
		md.delete(input1.getEid());
		
		assertThat( md.getAll("T1").size(), equalTo(0));

	}
	
	@Test 
	public void deleteDependent() throws MasterDataException {
		
		md.registerType("T1", master);
		md.registerType("T2", master);
		
		md.registerClient("T1", dependent);
		md.registerClient("T2", dependent);

		DataMock input1 = getMock("T1",EID.get('1'));
		md.post(input1.getEid(), input1);
		
		DataMock input2 = getMock("T2",EID.get('2'));
		md.postDependent(input2.getEid(), input2, input1.getEid());

		md.delete(input2.getEid());
		
		assertThat( md.getAll("T2").size(), equalTo(0));
		assertThat( md.getAll("T1").size(), equalTo(1));

		md.delete(input1.getEid());
		
		assertThat( md.getAll("T1").size(), equalTo(0));
	}
	
	@Test
	public void doubleRegistration() {
		try {
			md.registerType("T1", master);
			md.registerType("T1", dependent);
		}
		catch (MasterDataException x) {
			return;
		}
		fail("Exception missing");		
	}
	
	@Test 
	public void unknownTypeRegistration() {
		try {
			md.registerType("T1", master);
			md.registerClient("T2", dependent);
		}
		catch (MasterDataException x) {
			return;
		}
		fail("Exception missing");				
	}
	
	@Test
	public void unknownTypePost() {
		try {
			DataMock entry = getMock("T1",EID.get('1'));
			md.post(entry.getEid(), entry);
		}
		catch (MasterDataException x) {
			return;
		}
		fail("Exception missing");		
	}
	
	@Test
	public void unknownParentPost() {
		try {
		md.registerType("T1", master);
		md.registerType("T2", master);
		
		md.registerClient("T1", dependent);
		md.registerClient("T2", dependent);

		DataMock input1 = getMock("T1",EID.get('1'));
		md.post(input1.getEid(), input1);
		} catch (MasterDataException x) {
			fail("Unexpected Exception");
		}
		try {
			EIDTyped unknown = new EIDTyped("T",EID.get('T'));
			DataMock input2 = getMock("T2",EID.get('2'));
			md.postDependent(input2.getEid(), input2, unknown);
		} catch (MasterDataException x) {
			return;
		}
		fail("Exception missing");
	}
	
	@Test
	public void dependentNotDeleted() {
		DataMock input1 = null;
		try {
		md.registerType("T1", master);
		md.registerType("T2", master);
		
		md.registerClient("T1", dependent);
		md.registerClient("T2", dependent);

		input1 = getMock("T1",EID.get('1'));
		md.post(input1.getEid(), input1);
		
		DataMock input2 = getMock("T2",EID.get('2'));
		md.postDependent(input2.getEid(), input2, input1.getEid());
		} catch (MasterDataException x) {
			fail("Unexpected Exception");
		}

		try {
			md.delete(input1.getEid());
		} catch (MasterDataException x) {
			return;
		}
		fail("Exception missing");
	}
	
	@Test
	public void deleteUnknown() {
		DataMock input1 = null;
		DataMock input2 = null; 
		try {
		md.registerType("T1", master);
		md.registerType("T2", master);
		
		md.registerClient("T1", dependent);
		md.registerClient("T2", dependent);

		input1 = getMock("T1",EID.get('1'));
		md.post(input1.getEid(), input1);

		input2 = getMock("T1",EID.get('1'));

		} catch (MasterDataException x) {
			fail("Unexpected Exception");
		}

		try {
			md.delete(input2.getEid());
		} catch (MasterDataException x) {
			return;
		}
		fail("Exception missing");
	}
}
