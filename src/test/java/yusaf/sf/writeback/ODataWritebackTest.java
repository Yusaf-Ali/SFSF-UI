package yusaf.sf.writeback;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import utils.Configuration;
import utils.DefaultConfiguration;
import utils.SFSF;
import utils.ValueResolver;

public class ODataWritebackTest {
	@Test
	public void testValueResolver() {
		System.out.println(Instant.now().toEpochMilli());
		Object s = ValueResolver.convertDateToFormattedString(String.valueOf(Instant.now().toEpochMilli()));
		System.out.println(s);
		ZonedDateTime zdt = LocalDateTime.parse("2000-01-01T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("UTC"));
		System.out.println(zdt);
	}

	public void testOnSFSF() throws JsonProcessingException, InterruptedException, UnsupportedEncodingException {
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
		// OLDTODO place here check if the results are like
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
		e.addKey("effectiveStartDate", ValueResolver.toSFBodyDate(1655424000000L));
		System.out.println(e);

		ODataWriter writer = new ODataWriter(sfsf).createUpsertRequest(e);
		System.out.println(writer);

		String response = writer.sendRequest();
		System.out.println("response ");
		System.out.println(response);
	}
}
