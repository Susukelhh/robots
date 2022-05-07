package com.pojo;

import lombok.Data;

import java.util.List;
@Data
public class Robot {
    private int robotID;
    private double electricity;
    private int status;
    private int electricityStatus;
    private List<Task> task;

    public Robot() {
    }

    public Robot(int robotID, double electricity, int status, int electricityStatus, List<Task> task) {
        this.robotID = robotID;
        this.electricity = electricity;
        this.status = status;
        this.electricityStatus = electricityStatus;
        this.task = task;
    }


}
