package com.example.lukabaljak.elabcrowdsensing;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

public class LocationThread implements LocationListener {

    //private LocationManager mLocationManager = null;
    LocationCallback mLocationCallback;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest locationRequest;
    Location location;

    public Location getLocation() {
        return location;
    }

    public LocationThread(final Activity activity, final Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    2);

            mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(activity);
        } else
            mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(activity);
        createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                location = locationResult.getLastLocation();
                Log.d("Location", "duzina: " + location.getLongitude() + ", sirina: " + location.getLatitude());

                boolean isMock = false;
                if (android.os.Build.VERSION.SDK_INT >= 18) {
                    isMock = location.isFromMockProvider();
                }
                Log.d("JERYMOCK", String.valueOf(isMock));
                if(isMock){
                    Toast.makeText(context, "Prvo isključite aplikaciju za lažnu GPS lokaciju.", Toast.LENGTH_SHORT).show();
                    activity.finish();
                }

            }
        };

        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(100);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void destroyLocationService(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    public int getNumUpdates() {
        return locationRequest.getNumUpdates();
    }


    @Override
    public void onLocationChanged(Location location) {
       // Log.d("Location", location.getLatitude() + " " + location.getLongitude());

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
}
