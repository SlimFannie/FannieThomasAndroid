package com.example.fanniethomasjardin;

import com.google.gson.annotations.SerializedName;

public class Temperature {

    @SerializedName("temperature")
    float temperature;
    @SerializedName("date")
    String date;

    public Temperature(float temperature, String date) {
        this.temperature = temperature;
        this.date = date;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
