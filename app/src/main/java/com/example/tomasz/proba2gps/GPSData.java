package com.example.tomasz.proba2gps;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMConfiguration;

/**
 * Created by Tomasz on 22.10.2016.
 */
public class GPSData {

    //Deklaracje zmiennych opisujące pola obiektu GPSData
    String dane;
    String telID;
    String data;
    String place;

    String sky;
    Double pressure;
    double accu;
    double szerGeo;
    double dlugGeo;
    int temp;
    int sunrise;
    int sunset;
    int unixT;
    int main;


    // Funkcje pozwalające na pobieranie lub ustawianie wartości zmiennych obiektu GPSData
    public int getMain() {
        return main;
    }

    public void setMain(int main) {
        this.main = main;
    }

    public int getUnixT() {
        return unixT;
    }

    public void setUnixT(int unixT) {
        this.unixT = unixT;
    }


    public int getSunrise() {
        return sunrise;
    }

    public void setSunrise(int sunrise) {
        this.sunrise = sunrise;
    }


    public int getSunset() {
        return sunset;
    }

    public void setSunset(int sunset) {
        this.sunset = sunset;
    }


    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }


    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }


    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }


    public String getTelID() {
        return telID;
    }

    public void setTelID(String telID) {
        this.telID = telID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public double getAccu() {
        return accu;
    }

    public void setAccu(double accu) {
        this.accu = accu;
    }

    public double getSzerGeo() {
        return szerGeo;
    }

    public void setSzerGeo(double szerGeo) {
        this.szerGeo = szerGeo;
    }

    public double getDlugGeo() {
        return dlugGeo;
    }

    public void setDlugGeo(double dlugGeo) {
        this.dlugGeo = dlugGeo;
    }

    //Funkcja odpowiedzialna za tworzenie obiektu JSON
    public JSONObject toJSON()throws JSONException{
        JSONObject js= new JSONObject();

        js.put("telID",getTelID());
        js.put("data",getData());
        js.put("accu",getAccu());
        js.put("szerGeo",getSzerGeo());
        js.put("dlugGeo",getDlugGeo());
        js.put("temp",getTemp());
        js.put("place",getPlace());
        js.put("pressure",getPressure());
        js.put("sunrise",getSunrise());
        js.put("sunset",getSunset());
        js.put("unixT",getUnixT());
        js.put("main",getMain());

        return js;
    }
    //Tworzenie obiektu GPSData na podstawie obiektu JSON
    public static GPSData fromJSON(JSONObject object) throws JSONException {
        GPSData d= new GPSData();

        d.setTelID(object.getString("telID"));
        d.setData(object.getString("data"));
        d.setAccu(object.getDouble("accu"));
        d.setSzerGeo(object.getDouble("szerGeo"));
        d.setDlugGeo(object.getDouble("dlugGeo"));
        d.setTemp(object.getInt("temp"));
        d.setPlace(object.getString("place"));
        d.setPressure(object.getDouble("pressure"));
        d.setSunrise(object.getInt("sunrise"));
        d.setSunset(object.getInt("sunset"));
        d.setUnixT(object.getInt("unixT"));
        d.setMain(object.getInt("main"));

        return d;
    }


}