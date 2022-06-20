package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
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

	public static List<Map<String, String>> jsonToMap(String fromValue, Function<Map<String, String>, Void> filter) {
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
}