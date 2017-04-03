package com.example.tomasz.proba2gps;

/**
 * Created by Tomasz on 05.12.2016.
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

public class MyService extends Service {

    public GoogleApiClient client;

    GPSData gpsData;
    JSONObject js;
    public String android_id;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
    String currentDateandTime;

    DbGPS db = new DbGPS(this);

    public LocationManager LM;
    public Location loc;
    static String mPlace;
    static double mPressure;
    static int mSunrise;
    static int mSunset;
    static int mUnixT;
    static int mTemp;
    static int mMain;


    private Toast toast;


    //Klasa zwracająca obiekt usługi
    private final IBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        MyService getMyService() {
            return MyService.this;
        }
    }
    // funkcja wyświetlająca komunikat
  private void showToast(String text) {
      toast.setText(text);
      toast.show();
  }

    private void writeToLogs(String message) {
        Log.d("HelloServices", message);
    }

    // Stworzenie nasłuchiwacza na zmiany położenia
    public LocationListener LL = new LocationListener() {
        private double lat=0;
        private double lon=0;

        @Override
        public void onLocationChanged(Location location) {
            WeatherDownload zadanie = new WeatherDownload();
            System.out.println("Zmiana położenia");
            if (loc != null) {
                lon = loc.getLongitude();
                lat = loc.getLatitude();
            }
            //Wysyłanie żądania do serwisu pogodowego
            zadanie.execute("http://api.openweathermap.org/data/2.5/weather?lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon) + "&APPID=4b09231e32b493ab6865b0e7e050569f");

            // tworzenie obiektu JSON
            gpsToJson(location);
            // zapis stworzonego obiektu do bazy danych SQLite
            db.addData(js.toString());

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        writeToLogs("Called onCreate() method.");

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //Inicjalizacja obiektu klasy zapewniającego dostęp do usług lokalizacyjnych
        LM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //Ustawienie parametrów usługi lokalizacyjnej
        LM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5*60000, 0, LL);
        }

    //Funkcja tworząca obiekt JSON
    public JSONObject gpsToJson(Location l) {

        currentDateandTime = sdf.format(new Date());
        gpsData = new GPSData();
        gpsData.setTelID(android_id);
        gpsData.setData(currentDateandTime);
        gpsData.setDlugGeo(l.getLongitude());
        gpsData.setSzerGeo(l.getLatitude());
        gpsData.setAccu(l.getAccuracy());
        gpsData.setTemp(mTemp);
        gpsData.setPlace(mPlace);
        gpsData.setPressure(mPressure);
        gpsData.setSunrise(mSunrise);
        gpsData.setSunset(mSunset);
        gpsData.setUnixT(mUnixT);
        gpsData.setMain(mMain);
        try {
            System.out.println("Zapisuje do bazy");
            js = new JSONObject();
            js = gpsData.toJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        writeToLogs("Called onStartCommand() methond");
        showToast("Your service has been started");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        writeToLogs("Called onDestroy() method");
        showToast("Your service has been stopped");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }
}