package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
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
    private TextView textViewTemperature;
    private TextView textViewTrivials;

    //SystemService


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = findViewById(R.id.button_search);
        targetEditText = findViewById(R.id.edit_text_target_city);
        weatherIcon = findViewById(R.id.image_view_target_icon);
        textViewCity = findViewById(R.id.text_view_target_city);
        textViewTemperature = findViewById(R.id.text_view_target_temp);
        textViewTrivials = findViewById(R.id.text_view_others);

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
            } else {
                Toast.makeText(this, "You haven't enter anything!", Toast.LENGTH_SHORT).show();
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


            JSONArray retrieveArrayDataWeather = new JSONArray(weatherData);

            String cityData = jsonObject.getString("name");
            String iconData = "";

            for (int i = 0; i < retrieveArrayDataWeather.length(); i++) {
                JSONObject dataFromArray = retrieveArrayDataWeather.getJSONObject(i);
                iconData = dataFromArray.getString("icon");
            }

            JSONObject mainInfoObj = jsonObject.getJSONObject("main");
            String tempData = mainInfoObj.getString("temp");

            setDetails(cityData, tempData, iconData);
            setTrivial(mainInfoObj);

        } catch (Exception e) {
            Toast.makeText(this, "There is no such city called " + targetEditText.getText().toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, e.getMessage());
        }
    }

    private void setDetails(String cityData, String temperatureData, String iconData) {
        textViewCity.setText(cityData);
        setTemperature(temperatureData);
        String targetIcon = "http://openweathermap.org/img/wn/" + iconData + "@2x.png";
        Uri uri = Uri.parse(targetIcon);
        Log.d(TAG, targetIcon);

        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(weatherIcon);
    }

    private void setTrivial(JSONObject jsonObject) throws JSONException {
        String highestTemp = jsonObject.getString("temp_max").concat("℃");
        String lowestTemp = jsonObject.getString("temp_min").concat("℃");
        String humidity = jsonObject.getString("humidity").concat("%");

        String trivial = highestTemp + " ~ " + lowestTemp + " / " + humidity;
        textViewTrivials.setText(trivial);
    }

    private void setTemperature(String temperatureData){
        temperatureData += '\u2103';
        textViewTemperature.setText(temperatureData.trim());
    }

    private void hideKeyboard(){

        View view = this.getCurrentFocus();

        if(view != null){ //Only if the keyboard is opened.
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchButton.getWindowToken(),0);
        }
    }
}