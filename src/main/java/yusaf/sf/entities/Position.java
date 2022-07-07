package yusaf.sf.entities;

import java.time.ZonedDateTime;

import utils.ValueResolver;
import yusaf.sf.writeback.ODataEntity;

public class Position extends ODataEntity {
	public Position() {
		super.setType("Position");
	}

	public Position(String code, ZonedDateTime effectiveStartDate) {
		super.setType("Position");
		this.addKey("code", code);
		this.addKey("effectiveStartDate", ValueResolver.toSFBodyDate(effectiveStartDate));
	}

	@Override
	public void setType(String type) {
	}

	public String getCode() {
		return this.getKeys().get("code");
	}

	public void setCode(String code) {
		this.addKey("code", code);
	}

	public String getEffectiveStartDateString() {
		return this.getKeys().get("effectiveStartDate");
	}

	public ZonedDateTime getEffectiveStartDate() {
		return ValueResolver.convertToZonedDateTime(this.getEffectiveStartDateString());
	}

	public void setEffectiveStartDate(ZonedDateTime effectiveStartDate) {
		this.addKey("effectiveStartDate", ValueResolver.toSFBodyDate(effectiveStartDate));
	}

	public String getEffectiveEndDateString() {
		return this.getFields().get("effectiveEndDate");
	}

	public ZonedDateTime getEffectiveEndDate() {
		return ValueResolver.convertToZonedDateTime(this.getEffectiveEndDateString());
	}

	public void setEffectiveEndDate(ZonedDateTime effectiveEndDate) {
		this.addField("effectiveEndDate", ValueResolver.toSFBodyDate(effectiveEndDate));
	}

	public String getEffectiveStatus() {
		return this.getFields().get("effectiveStatus");
	}

	public void setEffectiveStatus(String effectiveStatus) {
		this.addField("effectiveStatus", effectiveStatus);
	}

	public String getPositionTitle() {
		return this.getFields().get("positionTitle");
	}

	public void setPositionTitle(String positionTitle) {
		this.addField("positionTitle", positionTitle);
	}
}