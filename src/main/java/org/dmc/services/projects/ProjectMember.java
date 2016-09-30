package org.dmc.services.projects;

import java.util.Date;

import org.dmc.services.DMCError;
import org.dmc.services.DMCServiceException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectMember {
    private int profileId;
    private int projectId;
    private boolean accept;
    private int fromProfileId;
    private String from;
    private Date date;
    private String id;

    public ProjectMember() {
    }

    @JsonProperty("id")
    public String getId() {
        id = getProjectId() + "-" + getProfileId() + "-" + getFromProfileId();
        return id;
    }

    @JsonProperty("id")
    public void setId(String value) {
        final String[] parts = value.split("-");
        if (parts.length != 3) {
            throw new DMCServiceException(DMCError.IncorrectType, "Project member request id is invalid");
        }
        projectId = Integer.parseInt(parts[0]);
        profileId = Integer.parseInt(parts[1]);
        fromProfileId = Integer.parseInt(parts[2]);
        id = value;
    }

    @JsonProperty("profileId")
    public String getProfileId() {
        return Integer.toString(profileId);
    }

    @JsonProperty("profileId")
    public void setProfileId(String value) {
        profileId = Integer.parseInt(value);
    }

    @JsonProperty("projectId")
    public String getProjectId() {
        return Integer.toString(projectId);
    }

    @JsonProperty("projectId")
    public void setProjectId(String value) {
        projectId = Integer.parseInt(value);
    }

    @JsonProperty("accept")
    public boolean getAccept() {
        return accept;
    }

    @JsonProperty("accept")
    public void setAccept(boolean value) {
        accept = value;
    }

    @JsonProperty("fromProfileId")
    public String getFromProfileId() {
        return Integer.toString(fromProfileId);
    }

    @JsonProperty("fromProfileId")
    public void setFromProfileId(String value) {
        fromProfileId = Integer.parseInt(value);
    }

    @JsonProperty("from")
    public String getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(String value) {
        from = value;
    }

    @JsonProperty("date")
    public long getDate() {
        return date.getTime();
    }

    @JsonProperty("date")
    public void setDate(long value) {
        date = new Date(value);
    }

}
