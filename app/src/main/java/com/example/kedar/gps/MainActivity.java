package com.example.kedar.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements LocationListener {
    TextView latitude;
    TextView longitude;
    TextView address;
    TextView distance;
    Location locationCurrent;
    Location locationPrevious;
    static final int COARSE_LOCATION = 1;
    static final int FINE_LOCATION = 2;
    LocationManager locManager;
    boolean firstTime = true;
    double dist;
    double totalDist;
    DecimalFormat df = new DecimalFormat("#.####");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitude = (TextView) (findViewById(R.id.latitude));
        longitude = (TextView) (findViewById(R.id.longitude));
        address = (TextView) (findViewById(R.id.address));
        distance = (TextView) (findViewById(R.id.distance));

        locManager = (LocationManager) (getSystemService(Context.LOCATION_SERVICE));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},COARSE_LOCATION);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION);
            return;
        }
        locationCurrent = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,5,this);
    }

    @Override
    public void onLocationChanged(Location location) {
        AsyncThread gpsThread = new AsyncThread();
        gpsThread.execute("https://maps.googleapis.com/maps/api/geocode/json?latlng="+location.getLatitude()+","+location.getLongitude()+"&key=YOUR_API_KEY_HERE");
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        latitude.setText("Latitude: " + df.format(lat));
        longitude.setText("Longitude: " + df.format(lng));
        if(firstTime){
            locationCurrent = location;
            firstTime = false;
        }
        else{
            if(locationCurrent != location) {
                locationPrevious = locationCurrent;
                locationCurrent = location;
            }
        }
        if(locationCurrent != null && locationPrevious != null){
            dist = locationCurrent.distanceTo(locationPrevious);
            totalDist += dist;
            double totalDistMiles = totalDist/1609;
            distance.setText("Total Distance: "+df.format(totalDistMiles)+" miles");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public class AsyncThread extends AsyncTask<String, Void, Void> {
        JSONObject addressOBJ;
        @Override
        protected Void doInBackground(String... string) {
            try {
                URL gpsURL = new URL(string[0]);
                URLConnection connection = gpsURL.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String JSONString;
                String temp = "";
                while ((JSONString=reader.readLine())!=null)
                    temp += JSONString;
                addressOBJ = new JSONObject(temp);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                address.setText(addressOBJ.getJSONArray("results").getJSONObject(0).getString("formatted_address"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
