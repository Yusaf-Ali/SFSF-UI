package yusaf.sf.entities;

import java.time.ZonedDateTime;

import utils.ValueResolver;
import yusaf.sf.writeback.ODataEntity;

public class EmpEmployment extends ODataEntity {
	public EmpEmployment() {
		super.setType("EmpEmployment");
	}

	public EmpEmployment(String userId, String personIdExternal) {
		super.setType("EmpEmployment");
		this.addKey("userId", userId);
		this.addKey("personIdExternal", personIdExternal);
	}

	@Override
	public void setType(String type) {
	}

	public String getUserId() {
		return this.getKeys().get("userId");
	}

	public void setUserId(String userId) {
		this.addKey("userId", userId);
	}

	public String getPersonIdExternal() {
		return this.getKeys().get("personIdExternal");
	}

	public void setPersonIdExternal(String personIdExternal) {
		this.addKey("personIdExternal", personIdExternal);
	}

	public String getStartDateString() {
		return this.getFields().get("startDate");
	}

	public ZonedDateTime getStartDate() {
		return ValueResolver.convertToZonedDateTime(this.getStartDateString());
	}

	public void setStartDate(ZonedDateTime startDate) {
		this.addField("startDate", ValueResolver.toSFBodyDate(startDate));
	}

	public String getFirstDateWorkedString() {
		return this.getFields().get("firstDateWorked");
	}

	public ZonedDateTime getFirstDateWorked() {
		return ValueResolver.convertToZonedDateTime(this.getFirstDateWorkedString());
	}

	public void setFirstDateWorked(ZonedDateTime firstDateWorked) {
		this.addField("firstDateWorked", ValueResolver.toSFBodyDate(firstDateWorked));
	}

	public String getEndDateString() {
		return this.getFields().get("endDate");
	}

	public ZonedDateTime getEndDate() {
		return ValueResolver.convertToZonedDateTime(this.getFirstDateWorkedString());
	}

	@Deprecated
	public void setEndDate(ZonedDateTime endDate) {
		this.addField("endDate", ValueResolver.toSFBodyDate(endDate));
	}
}