package yusaf.sf.entities;

import yusaf.sf.writeback.ODataEntity;

public class User extends ODataEntity {
	public User() {
		super.setType("User");
	}

	public User(String userId) {
		super.setType("User");
		this.getKeys().put("userId", userId);
	}

	@Override
	public void setType(String type) {
	}

	public String getUserId() {
		return this.getKeys().get("userId");
	}

	public void setUserId(String userId) {
		this.getKeys().put("userId", userId);
	}

	public String getStatus() {
		return this.getFields().get("status");
	}

	public void setStatus(String status) {
		this.getFields().put("status", status);
	}

	public String getUsername() {
		return this.getFields().get("username");
	}

	public void setUsername(String username) {
		this.getFields().put("username", username);
	}

	public String getFirstName() {
		return this.getFields().get("firstName");
	}

	public void setFirstName(String firstName) {
		this.getFields().put("firstName", firstName);
	}

	public String getLastName() {
		return this.getFields().get("lastName");
	}

	public void setLastName(String lastName) {
		this.getFields().put("lastName", lastName);
	}
}