package com.example.tomasz.proba2gps;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;



public class MainActivity extends AppCompatActivity implements OnClickListener {

    //Deklaracja zmiennej klasy używanej usługi
    private MyService myService;

    //Tworzenie obiektu zarządzającego połączeniem pomiędzy usługą i aktywnością
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.MyBinder binder = (MyService.MyBinder) service;
            myService = binder.getMyService();
        }
    };
    //Funkcja startująca usługę pracującą w tle
    private void startMyService() {
        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    //Funkcja stopująca usługę pracującą w tle
    private void stopMyService() {
        Intent serviceIntent = new Intent(this, MyService.class);
        unbindService(serviceConnection);
        stopService(serviceIntent);
    }

    //Deklaracja zmiennych dla obiektów interfejsu użytkownika
    Button stats;
    RadioButton gpsButton;
    RadioButton networkButton;
    TextView ShowData;
    static TextView oplace;
    static TextView otemp;
    static TextView opress;
    static TextView osky;
    String provider = null;


    //Deklaracje zmiennych pomocniczych
    public String android_id;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
    String currentDate;
    boolean isConnected;
    String result;

    Activity mActivity;
    private GoogleApiClient client;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        //Wystartowanie usługi pracującej w tle przy starcie aplikacji - głównej aktywności
        startMyService();
        //Inicjalizacja obiektów elementów interfejsu
        stats = (Button) findViewById(R.id.stats);
        gpsButton = (RadioButton) findViewById(R.id.gpsButton);
        networkButton = (RadioButton) findViewById(R.id.networkButton);
        ShowData = (TextView) findViewById(R.id.textView);
        oplace = (TextView) findViewById(R.id.textView2);
        otemp = (TextView) findViewById(R.id.textView3);
        opress = (TextView) findViewById(R.id.textView4);
        osky = (TextView) findViewById(R.id.textView5);

        stats.setOnClickListener(this);
        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }
    // Obsługa opcji dostępnych w rozwijanym menu - popup menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                startMyService();
                return true;
            case R.id.stop:
                stopMyService();
                return true;
            case R.id.refresh:
            /*    if (gpsButton.isChecked() == true) {
                    provider = myService.LM.GPS_PROVIDER;
                } else {
                    provider = myService.LM.NETWORK_PROVIDER;
                }*/
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return true;
                }
                //Pobranie ostatniej lokalizacji przed wysłaniem danych do serwera
                myService.loc = myService.LM.getLastKnownLocation(myService.LM.NETWORK_PROVIDER);
                if (myService.loc == null) {
                    log("Brak danych");
                } else {
                    myService.gpsToJson(myService.loc);
                    System.out.println(myService.gpsToJson(myService.loc).toString());
                    //dodawanie danch do bazy SQLite
                    myService.db.addData(myService.gpsToJson(myService.loc).toString());
                    //pobranie danych z bazy do listy
                    final List<DaneDb> gps = myService.db.getAllData();
                    // Utworzenie wątku odpowiedzialnego za nawiązanie połączenia i wysyłanie danych do serwera
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                //podanie adresu URL
                                URL url = new URL("http://XX.XX.XX.XX:XXXX/Proba2GPS/DoubleMeServlet");
                                //Utworzenie połączenia
                                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                                // ustawienie parametrów połączenia
                                httpConnection.setRequestMethod("POST");
                                httpConnection.setDoOutput(true);
                                httpConnection.setConnectTimeout(10000);
                                httpConnection.setDoInput(true);

                                String returnString1 = "";
                                //Otworzenie strumienia do wysłania danych
                                OutputStream out1 = new BufferedOutputStream(httpConnection.getOutputStream());
                                PrintWriter pw = new PrintWriter(out1);
                                // pętla w której wysyłane są dane do serwera (kolejne elementy z listy)
                                for (DaneDb d : gps) {
                                    result = d.getDane();
                                    pw.write(result);
                                    pw.flush();
                                    myService.db.deleteData(d);
                                }
                                pw.close();
                                BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                                while ((returnString1 = in.readLine()) != null) {
                                    System.out.println(returnString1);
                                    if(returnString1=="zapisano"){
                                    }
                                }
                                in.close();
                                httpConnection.disconnect();
                            } catch (Exception e) {
                                Log.d("Exception", e.toString());
                            }
                            finally {
                                Thread.interrupted();
                            }
                        }
                    }).start();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void log(String format, Object... args) {
        ShowData.append(String.format(format, args) + "\n");
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            // Połączenie do serwera (DO GET'A)
            case R.id.stats:

                new Thread(new Runnable() {

                    public void run() {
                        try {
                            //Usuwanie zawartości textboxa w głównej aktywności
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                            ShowData.setText("");
                                }
                            });

                            //Izolowanie samej daty z daty i godziny
                            String[] currentDateandTimeSplit = myService.currentDateandTime.split("-");
                            currentDate = currentDateandTimeSplit[0];

                            //Podanie adresu URL do serwera wraz z parametrami
                            URL url = new URL("http://XX.XX.XX.XX:XXXX/Proba2GPS/DoubleMeServlet?id=" + android_id + "&data=" + currentDate);
                            URLConnection connection = url.openConnection();
                            connection.connect();

                            //Odbieranie danych z serwera
                            String returnString = "";
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                            //Wyświetlanie danych w textboksie
                             while ((returnString = in.readLine()) != null) {

                                final String finalReturnString = returnString;
                                mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        ShowData.append(finalReturnString + "\n");
                                    }
                                });
                            }
                            in.close();

                        } catch (Exception e) {
                            Log.d("Exception", e.toString());
                        }
                        finally {
                            Thread.interrupted();
                        }

                    }
                }).start();

                break;

        }


    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }


}
