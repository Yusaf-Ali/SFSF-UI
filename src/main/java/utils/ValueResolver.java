package utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

	public static String toSFDate(Instant instant) {
		return toSFDate(instant.toEpochMilli());
	}

	public static String toSFDate(long milli) {
		return "/Date(" + milli + ")/";
	}

	/**
	 * Converts String date to yyyy-MM-ddTHH:mm:ss
	 * 
	 * @param value
	 * @return
	 */
	public static String convertDateToFormattedString(String value) {
		if (value.contains("T")) {
			ZonedDateTime dateTime = Instant.parse(value).atZone(ZoneId.of("UTC"));
			return dateTime.format(SFSF.sfsfDateFormat);
		}
		return convertToDate(value);
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

	public static String resolveKeyValue(String value) {
		Matcher m = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d)").matcher(value);
		if (m.matches()) {
			return "datetime'" + value + "'";
		}
		return "'" + value + "'";
	}
}