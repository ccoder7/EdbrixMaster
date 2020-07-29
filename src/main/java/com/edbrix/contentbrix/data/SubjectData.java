package com.edbrix.contentbrix.data;

import java.io.Serializable;

/**
 * Created by rajk on 11/12/17.
 */

public class SubjectData implements Serializable {
    private String subject;
    private String id;

    public static SubjectData addSubjectData(String id, String subject) {
        return new SubjectData(id, subject);
    }

    public SubjectData(String id, String subject) {
        this.subject = subject;
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
