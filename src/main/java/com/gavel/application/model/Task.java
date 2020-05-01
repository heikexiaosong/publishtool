package com.gavel.application.model;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Task {

    private final StringProperty taskid;
    private final StringProperty url;
    private final ObjectProperty<LocalDate> createtime;

    /**
     * Default constructor.
     */
    public Task() {
        this(null, null);
    }

    /**
     * Constructor with some initial data.
     *
     * @param taskid
     * @param url
     */
    public Task(String taskid, String url) {
        this.taskid = new SimpleStringProperty(taskid);
        this.url = new SimpleStringProperty(url);
        this.createtime = new SimpleObjectProperty<LocalDate>(LocalDate.of(1999, 2, 21));
    }

    public String getTaskid() {
        return taskid.get();
    }

    public StringProperty taskidProperty() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid.set(taskid);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public LocalDate getCreatetime() {
        return createtime.get();
    }

    public ObjectProperty<LocalDate> createtimeProperty() {
        return createtime;
    }

    public void setCreatetime(LocalDate createtime) {
        this.createtime.set(createtime);
    }

}
