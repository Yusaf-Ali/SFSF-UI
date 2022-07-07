package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
	public static String getRandomStringJson() {
		StringBuilder b = new StringBuilder();
		b.append("[");
		b.append("{\"F1\":\"R1V1\",\"F2\":\"R1V2\"}");
		b.append(",");
		b.append("{\"F1\":\"R2V1\",\"F2\":\"R2V2\"}");
		b.append("]");
		return b.toString();
	}

	public static List<Map<String, String>> jsonToMap(String fromValue) {
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, String>> converted = null;
		try {
			converted = mapper.readValue(fromValue, new TypeReference<List<Map<String, String>>>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return converted;
	}

	public static <T> T jsonToMap(String fromValue, TypeReference<T> typeReference) {
		ObjectMapper mapper = new ObjectMapper();
		T converted = null;
		try {
			converted = mapper.readValue(fromValue, typeReference);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return converted;
	}

	public static Document getDocument(String stringSource)
			throws SAXException, IOException, ParserConfigurationException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(stringSource)));
		return doc;
	}

	public static Document getDocument(InputStream source)
			throws SAXException, IOException, ParserConfigurationException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
		return doc;
	}

	public static boolean isInteger(String content) {
		try {
			Integer.valueOf(content);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isDouble(String content) {
		try {
			Double.valueOf(content);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static String mapToJson(Map<String, Object> body) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(body);
	}

	/**
	 * Converts to LocalDateTime first and then converts to ZonedDateTime using atZone with UTC.<br>
	 * Only parses {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} format.
	 * 
	 * @param string value to parse.
	 */
	public static ZonedDateTime createZDT(String string) {
		return createZDT(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	/**
	 * Converts to LocalDateTime first and then converts to ZonedDateTime using atZone with UTC
	 * 
	 * @param string value to parse.
	 * @param format to parse against.
	 */
	public static ZonedDateTime createZDT(String string, DateTimeFormatter format) {
		return LocalDateTime.parse(string, format).atZone(ZoneId.of("UTC"));
	}
}