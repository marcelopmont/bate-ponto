package com.apps.ghost.bateponto.models;

import java.util.Date;

/**
 * Created by Marcelo on 02/08/2017.
 */

public class ClockIn {

    private int id;
    private Date startTime;
    private double duration;

    public ClockIn() {}

    public ClockIn (Date startTime, Double duration) {
        this.startTime = startTime;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
