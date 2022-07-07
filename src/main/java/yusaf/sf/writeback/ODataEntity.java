package yusaf.sf.writeback;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import utils.ValueResolver;

/**
 * Extending this Type?, make sure to set type and override its {@link #setType(String)} method to nothing.<br>
 * Also use super.setType instead of this.setType in constructor.<br>
 * Every constructor must contain keys as its parameter, to identify what fields are keys.<br>
 * <b>Some entities are not deletable. Create with caution!<b>
 * 
 * @author yali
 *
 */
public class ODataEntity {
	private String type;
	private Map<String, String> keys = new HashMap<>();
	private Map<String, String> fields = new HashMap<>();
	private Map<String, ODataEntity> navFields = new HashMap<>();

	/**
	 * Creates a $filter String with " eq " and join with " and "
	 * 
	 * @return
	 */
	public String createFilter() {
		return keys.entrySet().stream().map(entry -> {
			return entry.getKey() + " eq " + ValueResolver.resolveKeyValue(entry.getValue());
		}).collect(Collectors.joining(" and "));
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getField(String key) {
		return fields.get(key);
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public void addField(String fieldName, String value) {
		this.fields.put(fieldName, value);
	}

	public String getKey(String key) {
		return keys.get(key);
	}

	public Map<String, String> getKeys() {
		return keys;
	}

	public void setKeys(Map<String, String> keys) {
		this.keys = keys;
	}

	public void addKey(String keyName, String value) {
		this.keys.put(keyName, value);
	}

	public Map<String, ODataEntity> getNavFields() {
		return navFields;
	}

	public void setNavFields(Map<String, ODataEntity> navFields) {
		this.navFields = navFields;
	}

	public String getMetadataString(String baseUrl) {
		StringBuilder builder = new StringBuilder();
		builder.append("{");

		builder.append("\"");
		builder.append("uri");
		builder.append("\"");
		builder.append(":");
		builder.append("\"");
		builder.append(baseUrl).append("/").append(type);
		builder.append("(");
		String keyString = keys.entrySet().stream().map(entry -> {
			if (entry.getKey().toLowerCase().contains("date")) {
				// startDate, effectiveStartDate or similar
				return "" + entry.getKey() + "=datetime'" + ValueResolver.convertDateToFormattedString(entry.getValue()) + "'";
			}
			return "" + entry.getKey() + "='" + entry.getValue() + "'";
		}).collect(Collectors.joining(","));
		builder.append(keyString);
		builder.append(")");
		builder.append("\"");
		builder.append(",");

		builder.append("\"");
		builder.append("type");
		builder.append("\"");
		builder.append(":");
		builder.append("\"");
		builder.append("SFOData.").append(type);
		builder.append("\"");

		builder.append("}");
		return builder.toString();
	}

	public Map<String, String> getMetadata(String baseUrl) {
		String keyString = keys.entrySet().stream().map(entry -> {
			if (entry.getKey().toLowerCase().contains("date")) {
				// startDate, effectiveStartDate or similar
				return "" + entry.getKey() + "=datetime'" + ValueResolver.convertDateToFormattedString(entry.getValue()) + "'";
			}
			return "" + entry.getKey() + "='" + entry.getValue() + "'";
		}).collect(Collectors.joining(","));

		Map<String, String> meta = new HashMap<>();
		String uri = baseUrl + "/" + type + "(" + keyString + ")";
		meta.put("uri", uri);
		meta.put("type", "SFOData." + type);
		return meta;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Type: ");
		sb.append(type);
		sb.append("\n");

		if (keys.size() > 0) {
			sb.append("Keys: ");
			sb.append(keys.entrySet().stream().map(entry -> {
				return entry.getKey() + ": " + entry.getValue();
			}).collect(Collectors.joining(", ")));
			sb.append("\n");
		}

		if (fields.size() > 0) {
			sb.append("Fields: ");
			sb.append(fields.entrySet().stream().map(entry -> {
				return entry.getKey() + ": " + entry.getValue();
			}).collect(Collectors.joining(", ")));
		}
		return sb.toString();
	}

	public void copyTo(ODataEntity entity) {
		this.fields.forEach((k, v) -> {
			entity.fields.put(k, v);
		});
		this.keys.forEach((k, v) -> {
			entity.keys.put(k, v);
		});
		this.navFields.forEach((k, v) -> {
			entity.navFields.put(k, v);
		});
		entity.type = this.type;
	}
}