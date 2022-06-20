package yusaf.sf.writeback;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import utils.ValueResolver;

public class ODataEntity {
	private String type;
	private HashMap<String, String> keys = new HashMap<>();
	private HashMap<String, String> fields = new HashMap<>();
	private HashMap<String, ODataEntity> navFields = new HashMap<>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public HashMap<String, String> getFields() {
		return fields;
	}

	public void setFields(HashMap<String, String> fields) {
		this.fields = fields;
	}

	public void addField(String fieldName, String value) {
		this.fields.put(fieldName, value);
	}

	public HashMap<String, String> getKeys() {
		return keys;
	}

	public void setKeys(HashMap<String, String> keys) {
		this.keys = keys;
	}

	public void addKey(String keyName, String value) {
		this.keys.put(keyName, value);
	}

	public HashMap<String, ODataEntity> getNavFields() {
		return navFields;
	}

	public void setNavFields(HashMap<String, ODataEntity> navFields) {
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
}
