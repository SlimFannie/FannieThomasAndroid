package com.example.fanniethomasjardin;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String broker = "tcp://172.16.72.231:1883";
    private static final String client_id = "";
    private MqttHandler mqttHandler;
    private SharedPreferences sharedPreferences;
    Switch souitche;
    TextView tvTemperature;
    TextView tvHeure;

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

        //sharedPrefs
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        //mqtt
        mqttHandler = new MqttHandler();
        mqttHandler.connect(broker, client_id);
        subscribeToTopics(Arrays.asList("topicTemperature", "topicHeure"));

        //get des éléments de la view
        souitche = findViewById(R.id.swLangue);
        tvHeure = findViewById(R.id.tvHeure);
        tvTemperature = findViewById(R.id.tvTemperature);

        //changer la langue
        String sharedLanguage = sharedPreferences.getString("langue", null);
        if (sharedLanguage != null) {
            changeLanguage(sharedLanguage);
        }

        souitche.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    changeLanguage("fr");
                } else {
                    changeLanguage("en");
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        mqttHandler.disconnect();
        super.onDestroy();
    }

    private void publishMessage(String topic, String message) {
        Toast.makeText(this, "Publishing message", Toast.LENGTH_SHORT).show();
        mqttHandler.publish(topic, message);
    }

    private void subscribeToTopics(List<String> topics) {
        for (String topic : topics) {
            subscribeToTopic(topic);
        }
    }

    private void subscribeToTopic(String topic) {
        mqttHandler.subscribe(topic, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                String temperatureMessage = new String(message.getPayload(), "UTF-8");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String temp = temperatureMessage + " °C";
                        tvTemperature.setText(temp);
                    }
                });
            }
        });
    }

    private void changeLanguage(String languageCode) {

        sharedPreferences.edit().putString("langue", languageCode).apply();

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        recreate();
    }

}