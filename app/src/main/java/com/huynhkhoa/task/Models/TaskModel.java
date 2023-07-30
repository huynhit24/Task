package com.huynhkhoa.task.Models;

import java.util.Date;

public class TaskModel {

    private String id;

    private String title;

    private String description;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    private String createdAt;

    private String duedateAt;

    private String priority;

    private Boolean finished;

    public String getCreatedAt() { return createdAt; }

    public String getDuedateAt() { return duedateAt; }

    public String getPriority() { return priority; }

    public Boolean getFinished() { return finished; }

    public TaskModel(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public TaskModel(String id, String title, String description, String duedateAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duedateAt = duedateAt;
    }

    public TaskModel(String id, String title, String description, String createdAt, String duedateAt, String priority, Boolean finished) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.duedateAt = duedateAt;
        this.priority = priority;
        this.finished = finished;
    }

}
