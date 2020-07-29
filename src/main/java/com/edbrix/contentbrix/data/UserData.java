package com.edbrix.contentbrix.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by rajk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String FirstName;

    private String Id;

    private String LastName;

    private String OrganizationId;

    private String StudentCode;

    public String getFirstName() {
        return FirstName;
    }

    @JsonProperty("FirstName")
    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }

    public String getId() {
        return Id;
    }

    @JsonProperty("Id")
    public void setId(String Id) {
        this.Id = Id;
    }

    public String getLastName() {
        return LastName;
    }

    @JsonProperty("LastName")
    public void setLastName(String LastName) {
        this.LastName = LastName;
    }

    public String getOrganizationId() {
        return OrganizationId;
    }

    @JsonProperty("OrganizationId")
    public void setOrganizationId(String OrganizationId) {
        this.OrganizationId = OrganizationId;
    }

    public String getStudentCode() {
        return StudentCode;
    }

    @JsonProperty("StudentCode")
    public void setStudentCode(String StudentCode) {
        this.StudentCode = StudentCode;
    }

    @Override
    public String toString() {
        return "ClassPojo [FirstName = " + FirstName + ", Id = " + Id + ", LastName = " + LastName + ", OrganizationId = " + OrganizationId + ", StudentCode = " + StudentCode + "]";
    }
}
