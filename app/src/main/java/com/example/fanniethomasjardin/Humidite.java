package com.example.fanniethomasjardin;

import com.google.gson.annotations.SerializedName;

public class Humidite {

    @SerializedName("humide")
    float humidite;
    @SerializedName("date")
    String date;

    public Humidite(float humidite, String date) {
        this.humidite = humidite;
        this.date = date;
    }

    public float getHumidite() {
        return humidite;
    }

    public void setHumidite(float humidite) {
        this.humidite = humidite;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
