package com.example.fanniethomasjardin;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hrules.charter.CharterLine;
import com.hrules.charter.CharterXLabels;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    Mqtt5Client client;
    TextView tvTemperature, tvHumidite, tvPlante, tvAlerte;
    Button btMusic;
    Boolean music;
    Handler handler = new Handler();
    CharterLine lineTemp;
    CharterXLabels lineTempsX;
    String[] temp;
    String[] hum;
    CharterLine lineHum;
    CharterXLabels lineHumX;
    List<Temperature> valuesTemp;
    float[] valuesHum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //trucs généré de base par l'appli
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //get des éléments de la view
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidite = findViewById(R.id.tvHumidite);
        tvPlante = findViewById(R.id.tvPlante);
        tvAlerte = findViewById(R.id.tvAlerte);
        lineTemp = findViewById(R.id.charter_temp);
        lineTempsX = findViewById(R.id.xlabel);
        btMusic = findViewById(R.id.btSon);
        music = false;

        //mqtt
        client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost("172.16.72.231")
                .serverPort(1883)
                .simpleAuth()
                .username("fannie")
                .password("q".getBytes())
                .applySimpleAuth()
                .build();

        client.toAsync().connect()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Succès!", "La connexion a réussi!");
                        souscrire();
                    }
                });

        //charts
        remplirChartTemp();

        //bouton son
        btMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!music) {
                    publishSon("true");
                }
            }
        });

    }

    public void remplirChartTemp() {
        InterfaceServeur serveur = RetrofitInstance.getInstance().create(InterfaceServeur.class);
        Call<List<Humidite>> call = serveur.getHumidite();

        call.enqueue(new Callback<List<Humidite>>() {
            @Override
            public void onResponse(Call<List<Humidite>> call, Response<List<Humidite>> response) {
                List<Humidite> valuesHum = response.body();
                if (valuesHum != null) {

                    float[] humidites = new float[valuesHum.size()];
                    String[] dates = new String[valuesHum.size()];
                    for (int i = 0; i < valuesHum.size(); i++) {
                        Humidite humidite = valuesHum.get(i);
                        humidites[i] = humidite.getHumidite();
                        dates[i] = humidite.getDate();
                    }
                    // charter_line_XLabel
                    lineTempsX.setStickyEdges(true);
                    lineTempsX.setValues(dates);
                    lineTemp.setValues(humidites);
                    lineTemp.setAnimInterpolator(new BounceInterpolator());
                    lineTemp.setShowGridLines(true);
                    lineTemp.show();

                }
            }

            @Override
            public void onFailure(Call<List<Humidite>> call, Throwable t) {
                Log.d("Erreur", "La communication avec la bdd a échouée.");
            }
        });
    }

    public void souscrire()
    {
        String sec, mouille, red_alert, no_alert;
        sec = getString(R.string.seche);
        mouille = getString(R.string.mouille);
        red_alert = getResources().getString(R.string.red_alert);
        no_alert = getResources().getString(R.string.no_alert);
        client.toAsync().subscribeWith()
                .topicFilter("topicTemperature")
                .callback(publish -> {
                    String t = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvTemperature.setText(t.replaceAll("\\[|\\]", ""));
                        }
                    });
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {

                    //    Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
                    } else {

                      //  Toast.makeText(this, "SUCCESS", Toast.LENGTH_SHORT).show();
                    }
                });

        client.toAsync().subscribeWith()
                .topicFilter("topicAir")
                .callback(publish -> {
                    String t = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvHumidite.setText(t.replaceAll("\\[|\\]", ""));
                        }
                    });
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {

                        //    Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
                    } else {

                        //  Toast.makeText(this, "SUCCESS", Toast.LENGTH_SHORT).show();
                    }
                });

        client.toAsync().subscribeWith()
                .topicFilter("topicHumidite")
                .callback(publish -> {
                    String t = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Double i = Double.parseDouble(t.replaceAll("\\[|\\]", ""));
                            if(i < 10)
                                tvPlante.setText(sec);
                            else
                                tvPlante.setText(mouille);
                        }
                    });
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {

                        //    Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
                    } else {

                        //  Toast.makeText(this, "SUCCESS", Toast.LENGTH_SHORT).show();
                    }
                });

        client.toAsync().subscribeWith()
                .topicFilter("topicMouv")
                .callback(publish -> {
                    String t = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Log", t);
                            tvAlerte.setText(red_alert);
                        }
                    });
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                    } else {
                        tvAlerte.setText(no_alert);
                    }
                });
    }

    private void publishSon(String muse){
        client.toAsync().publishWith()
                .topic("topicSon")
                .payload(muse.getBytes())
                .send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        Log.d("Erreur", throwable.toString());
                    } else {
                        Log.d("Youpi!", muse.getBytes().toString() );
                    }
                });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

}