package com.gavel.grainger;

public class GraingerCategory {

    private String code;
    private String title;
    private String grade;
    private String url;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "GraingerCategory{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", grade='" + grade + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
