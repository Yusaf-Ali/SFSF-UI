package yusaf.sf.writeback;

import java.io.UnsupportedEncodingException;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import utils.Configuration;
import utils.DefaultConfiguration;
import utils.SFSF;
import utils.ValueResolver;

public class ODataWritebackTest {
	public static void testValueResolver(String args[]) {
		System.out.println(Instant.now().toEpochMilli());
		Object s = ValueResolver.convertDateToFormattedString(String.valueOf(Instant.now().toEpochMilli()));
		System.out.println(s);
	}

	@Test
	public void test() throws JsonProcessingException, InterruptedException, UnsupportedEncodingException {
		// Create, Sleep, Get, Sleep and delete.
		upsertEntity();
		System.out.println("Sleeping");
		Thread.sleep(1000 * 5);
		System.out.println("Slept");
		readEntity(); // check if the entity exists by simple fetch
		deleteEntity(); // returns null, must check if the entity exists by reading
		System.out.println("Sleeping after deletion");
		Thread.sleep(1000 * 5);
		readEntity();
		// TODO place here check if the results are like
		/*
		 * { "d" : { "results" : [
		 * 
		 * ] } }
		 */
	}

	public void readEntity() throws UnsupportedEncodingException {
		SFSF sfsf = new SFSF(new DefaultConfiguration());
		String e = sfsf.getEntity("Position", "code eq 'temppos1701'");
		System.out.println(e);
	}

	public void deleteEntity() {
		Configuration config = new DefaultConfiguration();
		SFSF sfsf = new SFSF(config);

		ODataEntity e = new ODataEntity();
		e.setType("Position");
		e.addKey("code", "temppos1701");
		e.addKey("effectiveStartDate", ValueResolver.convert("/Date(1655424000000)/"));

		System.out.println(e);

		ODataWriter writer = new ODataWriter(sfsf).createDeleteRequest(e);
		String response = writer.sendRequest();
		System.out.println(writer);
		System.out.println(response);
	}

	public void upsertEntity() throws JsonProcessingException {
		Configuration config = new DefaultConfiguration();
		SFSF sfsf = new SFSF(config);

		ODataEntity e = new ODataEntity();
		e.setType("Position");
		e.addField("positionTitle", "Temp Position DEL 01");
		e.addField("effectiveStatus", "A");

		e.addKey("code", "temppos1701");
		e.addKey("effectiveStartDate", ValueResolver.toSFDate(1655424000000L));
		System.out.println(e);

		ODataWriter writer = new ODataWriter(sfsf).createUpsertRequest(e);
		System.out.println(writer);

		String response = writer.sendRequest();
		System.out.println("response ");
		System.out.println(response);
	}
}
