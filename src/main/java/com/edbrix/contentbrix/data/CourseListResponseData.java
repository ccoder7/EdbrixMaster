package com.edbrix.contentbrix.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseListResponseData implements Serializable
{

    @SerializedName("Success")
    @Expose
    private Integer success;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("Message")
    @Expose
    private String message;
    @SerializedName("CoursesList")
    @Expose
    private ArrayList<CoursesData> coursesList = null;
    private final static long serialVersionUID = 3451037350910840857L;

    @JsonProperty("Success")
    public Integer getSuccess() {   return success; }@JsonProperty("Success")
    public void setSuccess(Integer success) {   this.success = success; }

    @JsonProperty("Code")
    public String getCode() {   return code;    }@JsonProperty("Code")
    public void setCode(String code) {  this.code = code;   }

    @JsonProperty("Message")
    public String getMessage() {    return message; }@JsonProperty("Message")
    public void setMessage(String message) {    this.message = message; }

    @JsonProperty("CoursesList")
    public ArrayList<CoursesData> getCoursesList() { return coursesList; }@JsonProperty("CoursesList")
    public void setCoursesList(ArrayList<CoursesData> coursesList) { this.coursesList = coursesList; }

}