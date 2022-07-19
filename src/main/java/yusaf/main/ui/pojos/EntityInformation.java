package yusaf.main.ui.pojos;

import java.util.ArrayList;
import java.util.List;

public class EntityInformation {
	private String name;
	private String count;
	private List<String> allFields = new ArrayList<>();
	private List<String> ignoredFields = new ArrayList<>();
	private List<String> keys = new ArrayList<>();
	private boolean numeric;

	public EntityInformation(String name, String count) {
		this.name = name;
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public String getCount() {
		return count;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public List<String> getAllFields() {
		return allFields;
	}

	public List<String> getIgnorables() {
		return ignoredFields;
	}

	public List<String> getKeys() {
		return keys;
	}

	public void setAllFields(List<String> allFields) {
		this.allFields = allFields;
	}

	public void setIgnoredFields(List<String> ignoredFields) {
		this.ignoredFields = ignoredFields;
	}

	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	public void setNumeric(boolean is) {
		this.numeric = is;
	}

	public boolean isNumeric() {
		return numeric;
	}

	@Override
	public String toString() {
		return name + " " + count;
	}
}