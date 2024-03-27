package com.example.fanniethomasjardin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface InterfaceServeur {

    @GET("getTemperature.php")
    Call<List<Temperature>> getTemperatures();

    @GET("getHumidite.php")
    Call<float[]> getHumidite();

}
