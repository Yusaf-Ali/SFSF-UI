package yusaf.sf.entities;

import java.time.ZonedDateTime;

import utils.ValueResolver;
import yusaf.sf.writeback.ODataEntity;

public class EmpJob extends ODataEntity {
	public EmpJob() {
		super.setType("EmpJob");
	}

	public EmpJob(String seqNumber, String userId, ZonedDateTime startDate) {
		super.setType("EmpJob");
		this.addKey("seqNumber", seqNumber);
		this.addKey("userId", userId);
		this.addKey("startDate", ValueResolver.toSFBodyDate(startDate));
		this.setBusinessUnit(null);
		this.setCompany(null);
		this.setEventReason(null);
		this.setJobCode(null);
		this.setJobTitle(null);
		this.setLocation(null);
		this.setPayScaleArea(null);
		this.setPayScaleType(null);
		this.setPosition(null);
		this.setTimeZone(null);
	}

	@Override
	public void setType(String type) {
		// Overrode to avoid setting type again.
	}

	public String getStartDateString() {
		return getKeys().get("startDate");
	}

	public ZonedDateTime getStartDate() {
		return ValueResolver.convertToZonedDateTime(getStartDateString());
	}

	public void setStartDate(String startDate) {
		this.getKeys().put("startDate", startDate);
	}

	public void setStartDate(ZonedDateTime startDate) {
		this.getKeys().put("startDate", ValueResolver.toSFBodyDate(startDate));
	}

	public void setEndDate(ZonedDateTime endDate) {
		this.setEndDate(ValueResolver.toSFBodyDate(endDate));
	}

	public void setEndDate(String endDate) {
		this.getFields().put("endDate", endDate);
	}

	public String getEndDateString() {
		return this.getFields().get("endDate");
	}

	public ZonedDateTime getEndDate() {
		return ValueResolver.convertToZonedDateTime(getEndDateString());
	}

	public String getBusinessUnit() {
		return getFields().get("businessUnit");
	}

	public void setBusinessUnit(String businessUnit) {
		this.getFields().put("businessUnit", businessUnit);
	}

	public String getCompany() {
		return getFields().get("company");
	}

	public void setCompany(String company) {
		this.getFields().put("company", company);
	}

	public String getEventReason() {
		return getFields().get("eventReason");
	}

	/**
	 * HIRNEW for new entity.<br>
	 * DATACHG for general data changes.
	 * 
	 * @param eventReason
	 */
	public void setEventReason(String eventReason) {
		this.getFields().put("eventReason", eventReason);
	}

	public String getJobCode() {
		return getFields().get("jobCode");
	}

	public void setJobCode(String jobCode) {
		this.getFields().put("jobCode", jobCode);
	}

	public String getJobTitle() {
		return getFields().get("jobTitle");
	}

	public void setJobTitle(String jobTitle) {
		this.getFields().put("jobTitle", jobTitle);
	}

	public String getLocation() {
		return getFields().get("location");
	}

	public void setLocation(String location) {
		this.getFields().put("location", location);
	}

	public String getPayScaleArea() {
		return getFields().get("payScaleArea");
	}

	public void setPayScaleArea(String payScaleArea) {
		this.getFields().put("payScaleArea", payScaleArea);
	}

	public String getPayScaleType() {
		return getFields().get("payScaleType");
	}

	public void setPayScaleType(String payScaleType) {
		this.getFields().put("payScaleType", payScaleType);
	}

	public String getPosition() {
		return getFields().get("position");
	}

	public void setPosition(String position) {
		this.getFields().put("position", position);
	}

	public String getTimeZone() {
		return getFields().get("timezone");
	}

	public void setTimeZone(String timeZone) {
		this.getFields().put("timezone", timeZone);
	}

	public String getCustomString2() {
		return getFields().get("customString2");
	}

	public void setCustomString2(String customString2) {
		this.getFields().put("customString2", customString2);
	}

	public String getCustomString3() {
		return getFields().get("customString3");
	}

	public void setCustomString3(String customString3) {
		this.getFields().put("customString3", customString3);
	}

	public String getCustomString7() {
		return getFields().get("customString7");
	}

	public void setCustomString7(String customString7) {
		this.getFields().put("customString7", customString7);
	}

	public String getCustomString8() {
		return getFields().get("customString8");
	}

	public void setCustomString8(String customString8) {
		this.getFields().put("customString8", customString8);
	}

	public String getCustomString13() {
		return getFields().get("customString13");
	}

	public void setCustomString13(String customString13) {
		this.getFields().put("customString13", customString13);
	}
}