package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private strings
    private final static String TAG = "system";

    //UI
    private Button searchButton;
    private EditText targetEditText;
    private TextView textViewCity;
    private ImageView weatherIcon;
    private TextView textViewWeather;

    //SystemService


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = findViewById(R.id.button_search);
        targetEditText = findViewById(R.id.edit_text_target_city);
        weatherIcon = findViewById(R.id.image_view_target_icon);
        textViewCity = findViewById(R.id.text_view_target_city);
        textViewWeather = findViewById(R.id.text_view_target_weather);

        searchButton.setOnClickListener(this);

        String content = "https://openweathermap.org/data/2.5/weather?q=Seoul&appid=439d4b804bc8187953eb36d2a8c26a02";
        callWeatherData(content);
    }


    static class Weather extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... address) {
            try {
                URL url = new URL(address[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //Connect to URL
                connection.connect();

                //Retrieving Data
                InputStream streamIn = connection.getInputStream();
                InputStreamReader streamInReader = new InputStreamReader(streamIn);

                int data = streamInReader.read();
                StringBuilder weatherContent = new StringBuilder();

                while (data != -1) {
                    char ch = (char) data;
                    weatherContent.append(ch);
                    data = streamInReader.read();
                }

                return weatherContent.toString();
            } catch (MalformedURLException e) { //Exception for mal-formed url
                e.printStackTrace();
            } catch (IOException e) { //Exception for Open Connection to http url
                e.printStackTrace();
            }

            return null;
        }
    }

    //Implemented Methods
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_search) {
            //hide keyboard
            hideKeyboard();
            //Get target city data
            if (targetEditText.getText().toString().trim().length() > 0) {
                String targetData = "https://openweathermap.org/data/2.5/weather?q=" + targetEditText.getText().toString() + "&appid=439d4b804bc8187953eb36d2a8c26a02";
                callWeatherData(targetData);
            }
        }
    }

    //User-Defined Methods
    private void callWeatherData(String content) {
        Weather weather = new Weather();
        try {

            //Default : Seoul
            content = weather.execute(content).get();

            //JSON
            JSONObject jsonObject = new JSONObject(content);
            String weatherData = jsonObject.getString("weather");

            Log.d(TAG, weatherData);

            JSONArray retrieveArrayDataWeather = new JSONArray(weatherData);

            String cityData = jsonObject.getString("name");
            String descriptionData = "";
            String iconData = "";

            for (int i = 0; i < retrieveArrayDataWeather.length(); i++) {
                JSONObject dataFromArray = retrieveArrayDataWeather.getJSONObject(i);
                descriptionData = dataFromArray.getString("main");
                iconData = dataFromArray.getString("icon");
            }

            setDetails(cityData, descriptionData, iconData);

            Log.d(TAG, "**********MAIN***********\n" + iconData);
            Log.d(TAG, "*******Description*******\n" + descriptionData);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(TAG, e.getMessage());
        }
    }

    private void setDetails(String cityData, String descriptionData, String iconData) {
        textViewCity.setText(cityData);
        textViewWeather.setText(descriptionData);
        String targetIcon = "http://openweathermap.org/img/wn/" + iconData + "@2x.png";
        Uri uri = Uri.parse(targetIcon);
        Log.d(TAG, targetIcon);

        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(weatherIcon);
    }


    private void hideKeyboard(){

        View view = this.getCurrentFocus();

        if(view != null){ //Only if the keyboard is opened.
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchButton.getWindowToken(),0);
        }
    }
}