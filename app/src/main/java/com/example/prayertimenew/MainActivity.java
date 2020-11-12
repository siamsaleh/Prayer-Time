package com.example.prayertimenew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.leo.simplearcloader.SimpleArcLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    //Location Finding                                                                              Location Finding
    private double latitude = 0.0, longitude = 0.0;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    //private ProgressBar progressBar;

    //Date                                                                                          Date
    int month, year;
    //Prayer Times TextView
    TextView fajrTV, sunriseTV, dhuhrTV, asrTV, sunsetTV, maghribTV, ishaTV, imsakTV, midnightTV;
    String fajr, sunrise, dhuhr, asr, sunset, maghrib, isha, imsak, midnight, date;
    PrayerTime prayerTime;

    SimpleArcLoader simpleArcLoader;
    ScrollView scrollView;


    //JSON                                                                                          JSON Variables
    //private ArrayList<PrayerTime> arrayList = null;
//    private String url = "http://api.aladhan.com/v1/calendar?latitude=51.508515&longitude" +
//            "=-0.1254872&method=2&month=4&year=2017";
    private String url = null;


    TextView textView;

    //NavigationDrawer
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //ON CREATE
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Navigation
        navigationView = findViewById(R.id.nav_View);
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.Search_menu:
                        //locationFormAlertDialog();
                        return true;
                    case R.id.share_menu:
                        shareIt();
                        return true;
                    case R.id.rate_app_menu:
                        web1("https://play.google.com/store/apps/details?id=com.prayer.prayertimeapp");
                        return true;
//                    case R.id.tasbeeh_Counter_menu:
//                        web1("https://play.google.com/store/apps/details?id=com.tasbeeh.wtajbicou" +
//                                "nterfinalapp&reviewId=gp%3AAOqpTOGK4ADz735Ab7s-8CMH8ZQ0gaaI1Rymn3muUFdCpWpDYNCyaxGJhGC2BCt3ebznOJY0Jn-cXjrROjGr12s");
//                        return true;
                    case R.id.refresh_location_menu:
                        locationSearchAuto();
                        return true;

                }
                return false;
            }
        });

        //Set TextView
        findElements();

        textView = findViewById(R.id.text);

        SharedPreferences sharedPreferences = getSharedPreferences("prayer", MODE_PRIVATE);


        //Location
        if (sharedPreferences.contains("latitude") && sharedPreferences.contains("longitude")){
            latitude = Double.parseDouble(sharedPreferences.getString("latitude", "0"));
            longitude = Double.parseDouble(sharedPreferences.getString("longitude", "0"));
        }
        else {locationSearchAuto();}

        if (latitude != 0.0 && longitude != 0.0 ){ // Zokhn age theke e save thakbe
            if (sharedPreferences.contains("prayerTime")){
                getPrayerTimeFromSP();
                setPrayerTimeVariables(prayerTime);
                if (prayerTime.date.equals(getCurrentDate())){ //"26 Jul 2020"
                    setPrayerTimeTextView();
                } else{
                    dataCollect(urlSetUp());
                }
            }
        }

//        textView.setText(""+ longitude +"    " +latitude);
        //CurrentDate;
//        CurrentDate();

        //JSON Get data
//        dataCollect(urlSetUp());

        //PrayerTimeSaving
//        if (sharedPreferences.contains("prayerTime")){
//            getPrayerTimeFromSP();
//            if (prayerTime.date.equals(getCurrentDate())){ //"26 Jul 2020"
//                setPrayerTime(prayerTime);
//            } else{
//                dataCollect(urlSetUp());
//            }
//        }else{
//            dataCollect(urlSetUp());
//        }



        //Set Prayer Time In Text View
//        setPrayerTimeTextView();

    }


    //                                                                                              location start (Latitude, Longitude)
    private void locationSearchAuto(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
        }else{
            getCurrentLatLong();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLatLong();
            }else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLatLong() {
//        progressBar.setVisibility(View.VISIBLE);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest, new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(MainActivity.this)
                        .removeLocationUpdates(this);
                if (locationResult != null && locationResult.getLocations().size() > 0){
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
//                    lanlong.setText(String.format(
//                            "Latitude = %s\nLongitude = %s",
//                            latitude,
//                            longitude
//                    ));
                    SharedPreferences sharedPreferences = getSharedPreferences("prayer", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("latitude", String.valueOf(latitude));
                    editor.putString("longitude", String.valueOf(longitude));
                    editor.commit();
                }
                dataCollect(urlSetUp());//Upgrade times
//                progressBar.setVisibility(View.GONE);
            }
        }, Looper.getMainLooper());
    }

    private void CurrentDate(int month, int year){}

    //Navigation                                                                                    Navigation
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {

            return true;
        }

        return  super.onOptionsItemSelected(item);
    }

    private String urlSetUp(){
        url =  "http://api.aladhan.com/v1/calendar?latitude="+latitude+"&longitude="+longitude
                +"&method=2&month="+getMonth()+"&year="+getYear();
        return url;
    }

    //Date                                                                                          Date
    private String getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Date date = new Date();
        return dateFormat.format(date);
        //use in get time from JSON
    }
    private int getMonth(){
        Date date = new Date();
        SimpleDateFormat formatNowMonth = new SimpleDateFormat("MM");
        return Integer.parseInt(formatNowMonth.format(date));
    }
    private int getYear(){
        Date date = new Date();
        SimpleDateFormat formatNowYear = new SimpleDateFormat("YYYY");
        return Integer.parseInt(formatNowYear.format(date));
    }
    private String dateAMPM(String _24HourTime){
        try {
            //String _24HourTime = "22:15";
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
            Date _24HourDt = _24HourSDF.parse(_24HourTime);
            return _12HourSDF.format(_24HourDt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
                                                                                                    //Date Finish


    //                                                                                              Get Times From JSON
    public void dataCollect(String url){

//        simpleArcLoader.start();

        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

//                    if (arrayList != null){
//                        arrayList = null;
//                        Log.d("Siam", "Response" + "empty");//title
//                    }
//                    arrayList = new ArrayList<PrayerTime>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        JSONObject jsonObject1 = jsonObject.getJSONObject("timings");
                        JSONObject jsonObject2 = jsonObject.getJSONObject("date");


                        if (getCurrentDate().equals(jsonObject2.getString("readable"))){
                            JSONObject jsonObject1 = jsonObject.getJSONObject("timings");

                            PrayerTime prayerTime = new PrayerTime(jsonObject1.getString("Fajr"), jsonObject1.getString("Sunrise"), jsonObject1.getString("Dhuhr"),
                                    jsonObject1.getString("Asr"), jsonObject1.getString("Sunset"), jsonObject1.getString("Maghrib"), jsonObject1.getString("Isha"),
                                    jsonObject1.getString("Imsak"), jsonObject1.getString("Midnight"), jsonObject2.getString("readable")); //, jsonObject3.getString("date"), jsonObject4.getString("date")

                            upgradePrayerTimeObject(prayerTime); //Set Location reference
                            setPrayerTimeVariables(prayerTime);
                            savePrayerTimes();
                            setPrayerTimeTextView();

                            Log.d("Siam", "Response" + prayerTime.asr);//title
                            Log.d("Date", "Response" +  getCurrentDate());//title
                            Log.d("Siam1", "Response" + jsonObject2.getString("readable"));//title
                        }



//                        arrayList.add(prayerTime);

                    }
//                    simpleArcLoader.stop();
//                    simpleArcLoader.setVisibility(View.GONE);
//                    scrollView.setVisibility(View.VISIBLE);

                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                textView.setText(error.toString());
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        requestQueue.add(jsonObjectRequest);
    }
    private void upgradePrayerTimeObject(PrayerTime prayerTime){
        this.prayerTime = prayerTime;
    }
    private void savePrayerTimes(){
        SharedPreferences sharedPreferences = getSharedPreferences("prayer", MODE_PRIVATE);

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(prayerTime);
        prefsEditor.putString("prayerTime", json);
        prefsEditor.commit();
    }
    private void getPrayerTimeFromSP(){
        SharedPreferences sharedPreferences = getSharedPreferences("prayer", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("prayerTime", "");
        prayerTime = gson.fromJson(json, PrayerTime.class);
    }
    private void setPrayerTimeVariables(PrayerTime prayerTime){
        this.fajr = prayerTime.fajr;
        this.sunrise = prayerTime.sunrise;
        this.dhuhr = prayerTime.dhuhr;
        this.asr = prayerTime.asr;
        this.sunset = prayerTime.sunset;
        this.maghrib = prayerTime.maghrib;
        this.isha = prayerTime.isha;
        this.imsak = prayerTime.imsak;
        this.midnight = prayerTime.midnight;
        this.date = prayerTime.date;
    }
    private void findElements() {
        fajrTV = findViewById(R.id.fajrTV_id);
        sunriseTV = findViewById(R.id.sunRiseTV_id);
        dhuhrTV = findViewById(R.id.dhuhrTV_id);
        asrTV = findViewById(R.id.asrTV_id);
        sunsetTV = findViewById(R.id.sunsetTV_id);
        maghribTV = findViewById(R.id.maghribTV_id);
        ishaTV = findViewById(R.id.ishaTV_id);
        //imsakTV = findViewById(R.id.imsakTV_id);
        midnightTV = findViewById(R.id.midnightTV_id);

        simpleArcLoader = findViewById(R.id.loader);
        scrollView = findViewById(R.id.prayerTime_SV);
    }
    private void setPrayerTimeTextView(){
        fajrTV.setText(dateAMPM(fajr));
        sunriseTV.setText(dateAMPM(sunrise));
        dhuhrTV.setText(dateAMPM(dhuhr));
        asrTV.setText(dateAMPM(asr));
        sunsetTV.setText(dateAMPM(sunset));
        maghribTV.setText(dateAMPM(maghrib));
        ishaTV.setText(dateAMPM(isha));
        //imsakTV.setText(dateAMPM(imsak));
        midnightTV.setText(dateAMPM(midnight));

        simpleArcLoader.stop();
        simpleArcLoader.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    //share It
    public void shareIt(){ //View view [eta onClick use korle]
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String body="https://play.google.com/store/apps/details?id=com.prayer.prayertimeapp";
        String subject="Prayer Time";
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT,body);
        startActivity(Intent.createChooser(intent,"Share With"));
    }
    //for visiting website button
    public void web1(String url){
        Intent browserIntent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }




}
