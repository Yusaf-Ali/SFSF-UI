package utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is to convert values from OData API to custom strings
 * 
 * @author yali
 *
 */
public class ValueResolver {
	/**
	 * Generic converter method, handles most values (date time included).
	 * 
	 * @param value
	 * @return
	 */
	public static String convert(Object value) {
		if (value == null || value.equals("null")) {
			return "null";
		}
		if (value instanceof String) {
			String con = (String) value;
			if (con.toLowerCase().contains("/date("))
				return convertToDateAsString(con);
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

	public static String toSFBodyDate(ZonedDateTime zdt) {
		return toSFBodyDate(zdt.toInstant());
	}

	public static String toSFBodyDate(Instant instant) {
		return toSFBodyDate(instant.toEpochMilli());
	}

	public static String toSFBodyDate(long milli) {
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
		return convertToDateAsString(value);
	}

	private static String mapToString(LinkedHashMap<?, ?> map) {
		return map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", "));
	}

	/**
	 * Converts SF Date Time available in body to ZonedDateTime and then to String.<br>
	 * Calls {@link #convertToZonedDateTime(String)} internally.
	 * 
	 * @param value
	 * @return
	 */
	public static String convertToDateAsString(String value) {
		return convertToZonedDateTime(value).format(SFSF.sfsfDateFormat);
	}

	/**
	 * Converts SF Date Time available in body to ZonedDateTime object.
	 * 
	 * @param value
	 * @return
	 */
	public static ZonedDateTime convertToZonedDateTime(String value) {
		value = value.contains("+") ? value.split("[+]")[0] : value;
		value = value.replaceAll("[^0-9-]", "");
		Long l = Long.valueOf(value);
		Instant s = Instant.ofEpochMilli(l);
		return ZonedDateTime.ofInstant(s, ZoneId.of("UTC"));
	}

	/**
	 * Converts this value to SFSF Uri's Key Date Time format.
	 * 
	 * @param value
	 * @return
	 */
	public static String resolveKeyValue(ZonedDateTime value) {
		String string = value.format(SFSF.sfsfDateFormat);
		return resolveKeyValue(string);
	}

	/**
	 * Converts this value to SFSF Uri's Key Date Time format.
	 * 
	 * @param value
	 * @return
	 */
	public static String resolveKeyValue(String value) {
		Matcher m = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d)").matcher(value);
		if (m.matches()) {
			return "datetime'" + value + "'";
		} else if (value.startsWith("/Date") && value.endsWith("/")) {
			return resolveKeyValue(convertToZonedDateTime(value));
		}
		return "'" + value + "'";
	}
}