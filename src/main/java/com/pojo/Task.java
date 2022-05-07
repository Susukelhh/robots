package com.pojo;

import lombok.Data;

@Data
public class Task {
    private int taskID;
    private double x;
    private double y;
    private String area;
    private String distance;
    private int status;

    public Task() {
    }

    public Task(int taskID, double x, double y, String area, String distance, int status) {
        this.taskID = taskID;
        this.x = x;
        this.y = y;
        this.area = area;
        this.distance = distance;
        this.status = status;
    }
}
