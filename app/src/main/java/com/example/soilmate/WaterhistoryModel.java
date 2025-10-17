package com.example.soilmate;

import java.io.Serializable;

public class WaterhistoryModel implements Serializable {  // Implement Serializable
    private String status;
    private String time;
    private String date;

    public WaterhistoryModel(String status, String time, String date) {
        this.status = status;
        this.time = time;
        this.date = date;
    }

    public String getStatus() { return status; }
    public String getTime() { return time; }
    public String getDate() { return date; }
}
