package yusaf.main.ui.pojos;

import java.util.Map;

public class DynamicRow {
	Map<String, String> fieldValues;

	public DynamicRow(Map<String, String> fieldValues) {
		this.fieldValues = fieldValues;
	}

	public Map<String, String> getFieldValues() {
		return fieldValues;
	}
}