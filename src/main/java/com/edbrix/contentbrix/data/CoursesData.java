
package com.edbrix.contentbrix.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoursesData implements Serializable {

    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("Title")
    @Expose
    private String title;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("Access_Code")
    @Expose
    private String accessCode;
    @SerializedName("IsActive")
    @Expose
    private String isActive;
    @SerializedName("StartDate")
    @Expose
    private String startDate;
    @SerializedName("CourseImageUrl")
    @Expose
    private String courseImageUrl;
    private final static long serialVersionUID = -6940040926678952168L;

    @JsonProperty("Id")
    public String getId() { return id;  }@JsonProperty("Id")
    public void setId(String id) {  this.id = id;   }

    @JsonProperty("Title")
    public String getTitle() {  return title;   }@JsonProperty("Title")
    public void setTitle(String title) {    this.title = title; }

    @JsonProperty("Description")
    public String getDescription() {    return description; }@JsonProperty("Description")
    public void setDescription(String description) {    this.description = description; }

    @JsonProperty("Access_Code")
    public String getAccessCode() { return accessCode;  }@JsonProperty("Access_Code")
    public void setAccessCode(String accessCode) {  this.accessCode = accessCode;   }

    @JsonProperty("IsActive")
    public String getIsActive() {   return isActive;    }@JsonProperty("IsActive")
    public void setIsActive(String isActive) {  this.isActive = isActive;   }

    @JsonProperty("StartDate")
    public String getStartDate() {  return startDate;   }@JsonProperty("StartDate")
    public void setStartDate(String startDate) {    this.startDate = startDate; }

    @JsonProperty("CourseImageUrl")
    public String getCourseImageUrl() { return courseImageUrl;  }@JsonProperty("CourseImageUrl")
    public void setCourseImageUrl(String courseImageUrl) {  this.courseImageUrl = courseImageUrl;   }
}