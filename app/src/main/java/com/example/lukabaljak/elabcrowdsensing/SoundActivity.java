package com.example.lukabaljak.elabcrowdsensing;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


public class SoundActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener, ChartFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener {

    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 2;

    private static final String RECORD_FRAGMENT = "record";
    private static final String MAP_FRAGMENT = "map";
    private static final String SETTINGS_FRAGMENT = "settings";

    private Fragment fragmentContainer = null;
    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerLayoutToggle = null;
    private NavigationView navigationView = null;

    private LatLng currentLocation = null;
    ///HashMap je struktura koja omogućava skladištenje (ključ, vrednost) stavki.
    /// Heš funkcija uparuje svaki ključ sa indeksom niza gde će se čuvati vrednost.
    private HashMap<String, Double> signalMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        //odkomentarisano
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //dodato
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        ///Poziva se kada je izabrana stavka u meniju za navigaciju.
        /// salje joj se kao parametar selektovana stavka, a vraca true da oznaci koja je stavka selektovana
        navigationView = (NavigationView) findViewById(R.id.nvView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        Log.d("nav", "record");
                        Intent intent = new Intent(SoundActivity.this, SecondActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_record_fragment:
                        Log.d("nav", "record");
                        handleOnNavigationItemSelected(RECORD_FRAGMENT);
                        return true;
                    case R.id.nav_map_fragment:
                        Log.d("nav", "record");

                        Intent intent1 = new Intent(SoundActivity.this, com.example.lukabaljak.elabcrowdsensing.Map.class);
                        startActivity(intent1);
                        //handleOnNavigationItemSelected(MAP_FRAGMENT);
                        return false; //da ne bi kada se korisnik vrati ostalo oznaceno da je na mapi
                   /* case R.id.nav_settings_fragment:

                        Log.d("nav", "record");
                        handleOnNavigationItemSelected(SETTINGS_FRAGMENT);
                        return true; */
                }
                return false;
            }
        });

        handleOnNavigationItemSelected(RECORD_FRAGMENT);
    }

    //dodato
    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            ///drawer je otvoren
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void handleOnNavigationItemSelected(String fragment) {
        int fragmentLastIndex = getSupportFragmentManager().getBackStackEntryCount() - 1;
        Fragment fragmentView;

        mDrawerLayout.closeDrawer(Gravity.START);

        switch (fragment) {
            /*case MAP_FRAGMENT:
                fragmentView = MapFragment.newInstance(Constants.SOUND);
                //fragmentView = MapFragment.newInstance();
                break;*/
            case RECORD_FRAGMENT:
                fragmentView = ChartFragment.newInstance();
                break;
           /* case SETTINGS_FRAGMENT:
                fragmentView = new SettingsFragment();
                break; */
            default:
                return;
        }
//za upravljanje fragmentom
        if (fragmentLastIndex > -1) {
            String fragmentName = getSupportFragmentManager()
                    .getBackStackEntryAt(fragmentLastIndex)
                    .getName();

            if (fragmentName.equals(fragment)) {
                return;
            }
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragmentView)
                .addToBackStack(fragment)
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }



    ///?
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.nav_record_fragment:
                Log.d("nav", "first");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //https://localcoder.org/how-to-check-if-permission-is-granted-by-user-at-runtime-on-android
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Fragment mapFragment = MapFragment.newInstance(Constants.SOUND);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, mapFragment)
                            .addToBackStack(MAP_FRAGMENT)
                            .commit();
                }
            }
        }
    }

    @Override
    public void onLocationChange(LatLng latLng) {
        currentLocation = latLng;
        Log.d("lokacija", currentLocation.latitude + "");
    }

    @Override
    public void onDataCollected(HashMap<String, Double> signalMap, String naziv, String opis, double longitude, double latitude) {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        this.signalMap = signalMap;
        //UploadStatsTask uploadStatsTask = new UploadStatsTask();
        //uploadStatsTask.execute(sharedPreferences.getString("url", ""), naziv, opis);
        if (naziv == null) {
            Log.d("NULLJE", "NULLJE");

            naziv = "naziv";
            opis = "opis";

        }

        Log.d("naziv", naziv);
        Log.d("opis", opis);

        JSONObject request = new JSONObject();


        try {
            request.put("MACAdress", Common.getWifiMacAddress());
            request.put("lon", longitude);
            request.put("lat", latitude);
            request.put("naziv", naziv);
            request.put("opis", opis);

            HashMap<Integer, Double> mapForSorting = new HashMap<>();
            for (Map.Entry<String, Double> entry : signalMap.entrySet()) {
                double d = Double.parseDouble(entry.getKey());

                mapForSorting.put((int) d, entry.getValue());
            }

            //sortiranje
            TreeMap<Integer, Double> sorted = new TreeMap<>();
            sorted.putAll(mapForSorting);
            //ovde se nalazi glavni algoritam za slanje podataka na API
            JSONArray jsonArray = new JSONArray();
            //app meri buku i salje do platforme json niz sa drugim meta podacima kao sto su lokacija i naziv i opis
            for (Map.Entry<Integer, Double> entry : sorted.entrySet()) {
                Integer key = entry.getKey();
                Double value = entry.getValue();
                // Log.d("kljuc", key + "");
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(String.valueOf(key), value);
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            request.put("analiza", jsonArray);
            request.put("data", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //ovo je API za slanje podataka
        String urlString = "https://crowdsensing.elab.fon.bg.ac.rs/a1.php?vib=0";
        String newValue = request.toString();

        PostData postData = new PostData();
        postData.execute(urlString, newValue);
    }


    int brojPokusaja = 0;

    //kada se pozove fragment setting izbacuje gresku potom je dodato  implements SettingsFragment.OnFragmentInteractionListener
    //to je zahtevalo da se override-uje metoda onFragmentInteraction i ostavljena je prazna, a fragment sad radi
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class PostData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            final String urlString = strings[0];
            final String newValue = strings[1];

            Log.d("urlString", urlString);
            Log.d("newValue", newValue);

            HTTPBroker.POSTWithCertificate(getApplicationContext(), urlString, newValue);

            if (HTTPBroker.stream != null) {
                Log.d("RESPONSE", HTTPBroker.stream);
                HTTPBroker.stream = null;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Podaci su uspešno poslati.", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Log.d("RESPONSE", "nymyyyyy");
                final Timer sendDataTimer = new Timer();
                sendDataTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        HTTPBroker.POSTWithCertificate(getApplicationContext(), urlString, newValue);
                        //ako nema internet konekciju da bude neuspesno
                        if (HTTPBroker.stream != null) {
                            Log.d("RESPONSE", HTTPBroker.stream);
                            HTTPBroker.stream = null;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Podaci su uspešno poslati.", Toast.LENGTH_SHORT).show();
                                }
                            });

                            cancel();
                        }
                        if ((brojPokusaja++) > 5) {
                            Log.d("Tried", "but faileeed");
                            brojPokusaja = 0;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Podaci nisu uspešno poslati, obezbedite internet konekciju.", Toast.LENGTH_SHORT).show();
                                }
                            });

                            cancel();
                        }
                    }
                }, 3000L, 3000L);
            }


            return null;
        }
    }


    private class UploadStatsTask extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... urls) {
            try {
                //Log.d("request url", urls[0]);


                URL url = new URL("https://crowdsensing.elab.fon.bg.ac.rs/a.php?vib=0");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                try {
                    DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

                    JSONObject request = new JSONObject();


                    try {
                        request.put("MACAdress", Common.getWifiMacAddress());
                        request.put("lon", 0);
                        request.put("lat", 0);

                        request.put("analiza", new JSONObject(signalMap));
                        request.put("data", "");
                        request.put("naziv", urls[1]);
                        request.put("opis", urls[2]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    Log.d("request", request.toString());
                    out.write(request.toString().getBytes());
                    out.flush();

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        return 1L;
                    } else {
                        return 0L;
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return 0L;
        }

        protected void onPostExecute(Long result) {
            if (result == 1L) {
                //Toast.makeText(SoundCaptureActivity.this, "Success", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(SoundCaptureActivity.this, "Fail", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
