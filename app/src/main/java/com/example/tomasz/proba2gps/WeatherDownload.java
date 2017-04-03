package com.example.tomasz.proba2gps;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Objects;

import static com.example.tomasz.proba2gps.MyService.mMain;
import static com.example.tomasz.proba2gps.MyService.mSunrise;
import static com.example.tomasz.proba2gps.MyService.mPlace;
import static com.example.tomasz.proba2gps.MyService.mPressure;
import static com.example.tomasz.proba2gps.MyService.mSunset;
import static com.example.tomasz.proba2gps.MyService.mTemp;
import static com.example.tomasz.proba2gps.MyService.mUnixT;


public class WeatherDownload extends AsyncTask<String,Void,String>
{

    public static int temp;
    double pressure;
    String place;
    double temperature;
    int sunrise;
    int sunset;
    int main;
    long unixTime;
    int unixT;
    String sky;



    @Override
    protected String doInBackground(String... urls) {

        // Łączenie z serwerem pogodowym i odbieranie danych
        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();

            while (data != -1){
                char current = (char) data;
                result += current;
                data = reader.read();
            }
            return result;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);



        try {
            JSONObject jsonObject= new JSONObject(result);

            //main
            JSONObject weatherDATA = new JSONObject(jsonObject.getString("main"));
            pressure = Double.parseDouble(weatherDATA.getString("pressure"));
            place = jsonObject.getString("name");
            temperature = Double.parseDouble(weatherDATA.getString("temp"));
            temp = (int) (temperature - 273.15);

            //sys
            JSONObject sunData = new JSONObject(jsonObject.getString("sys"));
            sunrise = (int) Double.parseDouble(sunData.getString("sunrise"));
            sunset = (int) Double.parseDouble(sunData.getString("sunset"));

            //weather
            JSONArray weatData = new JSONArray(jsonObject.getString("weather"));
            JSONObject obj = new JSONObject(weatData.getString(0));
            main = Integer.parseInt(obj.getString("id"));

            unixTime = System.currentTimeMillis() / 1000L;
            unixT = (int) unixTime;

            int main2 = main/100;
                switch(main2){
                    case 2:  sky = "Burza";
                        break;
                    case 3:  sky = "Mżawka";
                        break;
                    case 5:  sky = "Deszcz";
                        break;
                    case 6:  sky = "Śnieg";
                        break;
                    case 7:  sky = "Mgła";
                        break;
                    case 8:  sky = "Zachmurzenie";
                        break;
                    case 9:  sky = "Warunki ekstremalne";
                        break;
                }

                if (main==800){
                    sky = "Czyste niebo";
                }

            mTemp = temp;
            mPlace = place;
            mPressure = pressure;
            mSunrise = sunrise;
            mSunset = sunset;
            mUnixT = unixT;
            mMain = main;

            // Wyświetlanie informacji w głównej aktywności
            MainActivity.oplace.setText("Miasto: " + String.valueOf(place));
            MainActivity.otemp.setText("Temperatura: " + String.valueOf(temp) + " C");
            MainActivity.opress.setText("Ciśnienie: " + String.valueOf(pressure) + " hPA");
            MainActivity.osky.setText("Niebo: " + sky);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
