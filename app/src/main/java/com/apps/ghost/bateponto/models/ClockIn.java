package com.apps.ghost.bateponto.models;

import java.util.Date;

/**
 * Created by Marcelo on 02/08/2017.
 */

public class ClockIn {

    private int id;
    private Date startTime;
    private long duration;

    public ClockIn() {}

    public ClockIn (Date startTime, long duration) {
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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
