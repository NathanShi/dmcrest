package org.dmc.services.verification;


import com.fasterxml.jackson.annotation.JsonProperty;

public class VerificationPatch {
	
	private int id; 
	private String url; 
	private String table;
	private String folder; 
	private String resourceType; 
	private String userEPPN; 
	private boolean verified;
	private String scanLog; 
	
    @JsonProperty("id")
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
    @JsonProperty("url")
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
    @JsonProperty("table")
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	
    @JsonProperty("folder")
    public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
    @JsonProperty("resourceType")
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
    @JsonProperty("userEPPN")
	public String getUserEPPN() {
		return userEPPN;
	}
	public void setUserEPPN(String userEPPN) {
		this.userEPPN = userEPPN;
	}
	@JsonProperty("verified")
	public boolean isVerified() {
		return verified;
	}
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	
	@JsonProperty("scanLog")
	public String getScanLog() {
		return scanLog;
	}
	public void setScanLog(String scanLog) {
		this.scanLog = scanLog;
	} 
	
	
	
	
	
	
}
