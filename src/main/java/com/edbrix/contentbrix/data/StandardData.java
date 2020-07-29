package com.edbrix.contentbrix.data;

import java.io.Serializable;

/**
 * Created by rajk on 11/12/17.
 */

public class StandardData implements Serializable {
    private String id;
    private String standard;

    public static StandardData addStandardData(String id, String standard) {
        return new StandardData(id, standard);
    }

    public StandardData(String id, String standard) {
        this.id = id;
        this.standard = standard;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }
}
