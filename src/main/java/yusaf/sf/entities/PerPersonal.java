package yusaf.sf.entities;

import java.time.ZonedDateTime;

import utils.ValueResolver;
import yusaf.sf.writeback.ODataEntity;

public class PerPersonal extends ODataEntity {
	public PerPersonal() {
		super.setType("PerPersonal");
	}

	public PerPersonal(String personIdExternal, ZonedDateTime startDate) {
		super.setType("PerPersonal");
		this.addKey("personIdExternal", personIdExternal);
		this.addKey("startDate", ValueResolver.toSFBodyDate(startDate));
	}

	@Override
	public void setType(String type) {
	}

	public String getStartDateString() {
		return this.getKeys().get("startDate");
	}

	public ZonedDateTime getStartDate() {
		return ValueResolver.convertToZonedDateTime(this.getStartDateString());
	}

	public void setStartDate(ZonedDateTime startDate) {
		this.addKey("startDate", ValueResolver.toSFBodyDate(startDate));
	}

	public String getPersonIdExternal() {
		return getKeys().get("personIdExternal");
	}

	public void setPersonIdExternal(String personIdExternal) {
		this.addKey("personIdExternal", personIdExternal);
	}

	public String getNativePreferredLang() {
		return this.getFields().get("nativePreferredLang");
	}

	public void setNativePreferredLang(String nativePreferredLang) {
		this.addField("nativePreferredLang", nativePreferredLang);
	}

	public String getNationality() {
		return this.getFields().get("nationality");
	}

	public void setNationality(String nationality) {
		this.addField("nationality", nationality);
	}

	public String getFirstName() {
		return this.getFields().get("firstName");
	}

	public void setFirstName(String firstName) {
		this.addField("firstName", firstName);
	}

	public String getLastName() {
		return this.getFields().get("lastName");
	}

	public void setLastName(String lastName) {
		this.addField("lastName", lastName);
	}
}
