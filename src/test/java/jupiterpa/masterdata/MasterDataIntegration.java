package jupiterpa.masterdata;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import jupiterpa.IMasterDataServer;
import jupiterpa.IMasterDataServer.EIDTyped;
import jupiterpa.IMasterDataServer.MasterDataException;
import jupiterpa.IMasterDataDefinition;
import jupiterpa.util.EID;
import jupiterpa.util.masterdata.MasterDataMaster;
import jupiterpa.util.masterdata.MasterDataSlave;
import lombok.AllArgsConstructor;
import lombok.Data;


@RunWith(SpringRunner.class)
@SpringBootTest
//@ActiveProfiles({"mock","test"})
public class MasterDataIntegration {
	@Autowired IMasterDataServer md;
	
	MasterDataMaster<DataMock> master;
	MasterDataMaster<DepMock> depMaster;
	MasterDataSlave<DataMock> slave;
	MasterDataSlave<DepMock> depSlave;
	
	@Data @AllArgsConstructor
	public class DataMock implements IMasterDataDefinition.Type {
		EIDTyped eid;
		String value;
		
		@Override public EID getId() { return eid.getId(); } 
	}
	private DataMock getMock(String type, EID id, String value) throws MasterDataException {
		EIDTyped eid = new EIDTyped(type,id);
		DataMock data = new DataMock(eid, value);
		return data;
	}

	@Data @AllArgsConstructor
	public class DepMock implements IMasterDataDefinition.Type , IMasterDataDefinition.HasParent {
		EIDTyped eid;
		String value;
		
		@Override public EID getId() { return eid.getId(); } 
		@Override public EID getParentId(String type) { return eid.getId(); }
	}
	private DepMock getDepMock(String type, EID id, String value) throws MasterDataException {
		EIDTyped eid = new EIDTyped(type,id);
		DepMock data = new DepMock(eid, value);
		return data;
	}

	@Before
	public void setup() throws MasterDataException {
		md.reset();
		
		master = new MasterDataMaster<DataMock>("T",md, null);
		
		depMaster = new MasterDataMaster<DepMock>("D",md, null);
		depMaster.addParent(master);
		
		slave = new MasterDataSlave<DataMock>("T",md, null);
		
		depSlave = new MasterDataSlave<DepMock>("D",md, null);						
	}
	
	@Test
	public void createAndReplicate() throws MasterDataException {
		
		EID id = EID.get('T');
		DataMock input = getMock("T",id,"1");
		
		master.create(input);
		
		DataMock result = slave.get(id);
		assertThat(result, equalTo(input));		
	}
	
	@Test
	public void createUpdate() throws MasterDataException {
		
		EID id = EID.get('T');
		DataMock input = getMock("T",id,"1");
		
		master.create(input);
		
		input.setValue("2");
		master.update(input);
		
		DataMock result = slave.get(id);
		assertThat(result, equalTo(input));		
	}
	
	@Test
	public void dependent() throws MasterDataException {
		
		EID id = EID.get('T');
		DataMock inputMaster = getMock("T",id,"1");		
		master.create(inputMaster);

		DepMock inputDep = getDepMock("D",id,"1");		
		depMaster.create(inputDep);
		
		DepMock result = depSlave.get(id);
		assertThat( result, equalTo(inputDep));
	}
}
