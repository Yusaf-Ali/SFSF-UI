package utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
}