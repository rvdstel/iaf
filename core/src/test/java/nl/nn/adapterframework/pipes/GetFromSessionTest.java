package nl.nn.adapterframework.pipes;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import nl.nn.adapterframework.configuration.ConfigurationException;
import nl.nn.adapterframework.core.IPipeLineSession;
import nl.nn.adapterframework.core.PipeLineSessionBase;
import nl.nn.adapterframework.core.PipeRunException;
import nl.nn.adapterframework.core.PipeRunResult;
import nl.nn.adapterframework.core.PipeStartException;
import nl.nn.adapterframework.parameters.Parameter;
import nl.nn.adapterframework.util.Misc;
import org.junit.Before;
import org.junit.Test;

public class GetFromSessionTest extends PipeTestBase<GetFromSession> {

	private IPipeLineSession session;
	
	private final String DUMMY_DATA = "dummy data";

	@Before
	public void populateSession() {
		session = new PipeLineSessionBase();
		session.put("dummyString", DUMMY_DATA);
		session.put("dummyByteArray", DUMMY_DATA.getBytes());
		session.put("dummyStream", new ByteArrayInputStream(DUMMY_DATA.getBytes()));

		session.put("emptyMap", new HashMap<String, String>());
		Map<String, String> map = new HashMap<String, String>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		session.put("map", map);
	}

	@Override
	public GetFromSession createPipe() {
		return new GetFromSession();
	}

	@Test
	public void sessionKeyIsEmpty() throws ConfigurationException, PipeStartException, PipeRunException {
		pipe.setSessionKey("");
		pipe.configure();
		pipe.start();

		PipeRunResult prr = pipe.doPipe("dummyString", session);
		String result = (String) prr.getResult();
		assertEquals(DUMMY_DATA, result.trim());
	}

	@Test
	public void unknownSessionKey() throws ConfigurationException, PipeStartException, PipeRunException {
		pipe.setSessionKey("unknown");
		pipe.configure();
		pipe.start();

		PipeRunResult prr = pipe.doPipe("ingored", session);
		assertNull(prr.getResult());
	}

	@Test
	public void retrieveStringFromSession() throws ConfigurationException, PipeStartException, PipeRunException {
		pipe.setSessionKey("dummyString");
		pipe.configure();
		pipe.start();

		PipeRunResult prr = pipe.doPipe("ingored", session);
		String result = (String) prr.getResult();
		assertEquals(DUMMY_DATA, result);
	}

	@Test
	public void retrieveByteArrayFromSession() throws ConfigurationException, PipeStartException, PipeRunException {
		pipe.setSessionKey("dummyByteArray");
		pipe.configure();
		pipe.start();

		PipeRunResult prr = pipe.doPipe("ingored", session);
		byte[] result = (byte[]) prr.getResult();
		assertEquals(DUMMY_DATA, new String(result));
	}

	@Test
	public void retrieveInputStreamFromSession() throws ConfigurationException, PipeStartException, PipeRunException, IOException {
		pipe.setSessionKey("dummyStream");
		pipe.configure();
		pipe.start();

		PipeRunResult prr = pipe.doPipe("ingored", session);
		InputStream result = (InputStream) prr.getResult();
		assertEquals(DUMMY_DATA, Misc.streamToString(result));
	}

	@Test
	public void retrieveEmptyMapFromSession() throws ConfigurationException, PipeStartException, PipeRunException {
		pipe.setType(Parameter.TYPE_MAP);
		pipe.setSessionKey("emptyMap");
		pipe.configure();
		pipe.start();

		PipeRunResult prr = pipe.doPipe("ingored", session);
		String result = (String) prr.getResult();
		assertEquals("<items />", result.trim());
	}

	@Test
	public void retrieveMapFromSession() throws ConfigurationException, PipeStartException, PipeRunException, IOException {
		pipe.setType(Parameter.TYPE_MAP);
		pipe.setSessionKey("map");
		pipe.configure();
		pipe.start();

		PipeRunResult prr = pipe.doPipe("ingored", session);
		String result = (String) prr.getResult();

		assertNotNull(result);
		String key1 = null;
		String key2 = null;

		//It's a bit random how the map is sorted.. sometimes key2 appears before key1
		BufferedReader bufReader = new BufferedReader(new StringReader(result));
		String line=null;
		while( (line=bufReader.readLine()) != null ) {
			if(line.indexOf("key1") > 0)
				key1 = line.trim();
			if(line.indexOf("key2") > 0)
				key2 = line.trim();
		}

		assertEquals("<item name=\"key1\">value1</item>", key1);
		assertEquals("<item name=\"key2\">value2</item>", key2);
	}
}
