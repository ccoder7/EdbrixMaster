package com.edbrix.contentbrix.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 Created by rajk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationsData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String Id;
    private String OrganizationName;
    private String OrganizationLogoUrl;

    public String getOrganizationName() {
        return OrganizationName;
    }

    @JsonProperty("OrganizationName")
    public void setOrganizationName(String organizationName) {
        OrganizationName = organizationName;
    }

    public String getId() {
        return Id;
    }

    @JsonProperty("Id")
    public void setId(String Id) {
        this.Id = Id;
    }

    public String getOrganizationLogoUrl() {
        return OrganizationLogoUrl;
    }

    @JsonProperty("OrganizationLogoUrl")
    public void setOrganizationLogoUrl(String organizationLogoUrl) {
        OrganizationLogoUrl = organizationLogoUrl;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());

        builder.append("\n{");

        builder.append("\nId:");
        builder.append(this.Id);

        builder.append("\nOrganizationName :");
        builder.append(this.OrganizationName);

        builder.append("\n}");

        return builder.toString();
    }
}
