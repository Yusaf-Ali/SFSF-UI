package yusaf.sf.entities;

import yusaf.sf.writeback.ODataEntity;

public class PerPerson extends ODataEntity {
	public PerPerson() {
		super.setType("PerPerson");
	}

	public PerPerson(String personIdExternal) {
		super.setType("PerPerson");
		this.addKey("personIdExternal", personIdExternal);
	}

	@Override
	public void setType(String type) {
	}

	public String getPersonIdExternal() {
		return this.getKeys().get("personIdExternal");
	}

	public void setPersonIdExternal(String personIdExternal) {
		this.addKey("personIdExternal", personIdExternal);
	}

	public String getUserId() {
		return this.getFields().get("userId");
	}

	public void setUserId(String userId) {
		this.addField("userId", userId);
	}
}