package com.example.fanniethomasjardin;

import com.google.gson.annotations.SerializedName;

public class Temperature {

    @SerializedName("temperature")
    double temperature;
    @SerializedName("date")
    String date;

    public Temperature(double temperature, String date) {
        this.temperature = temperature;
        this.date = date;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
