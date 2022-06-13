package utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * This is to convert values from sfsf api to custom strings
 * 
 * @author yali
 *
 */
public class ValueResolver {
	public static String convert(Object value) {
		if (value == null || value.equals("null")) {
			return "null";
		}
		if (value instanceof String) {
			String con = (String) value;
			if (con.toLowerCase().contains("/date("))
				return convertToDate(con);
			return con;
		}
		if (value instanceof LinkedHashMap<?, ?>) {
			return mapToString((LinkedHashMap<?, ?>) value);
		}
		if (value instanceof Number || value instanceof Boolean) {
			return String.valueOf(value);
		}
		return "Unable to convert";
	}

	private static String mapToString(LinkedHashMap<?, ?> map) {
		return map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", "));
	}

	private static String convertToDate(String value) {
		value = value.contains("+") ? value.split("[+]")[0] : value;
		value = value.replaceAll("[^0-9-]", "");
		Long l = Long.valueOf(value);
		Instant s = Instant.ofEpochMilli(l);
		return ZonedDateTime.ofInstant(s, ZoneId.of("UTC")).format(SFSF.sfsfDateFormat);
	}
}
