package com.example.lukabaljak.elabcrowdsensing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

public class VibrationActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener {

    private DrawerLayout mDrawerLayout = null;
    private NavigationView navigationView = null;

    private static final String RECORD_FRAGMENT = "record";
    private static final String MAP_FRAGMENT = "map";
    private static final String SETTINGS_FRAGMENT = "settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration);

        //dodato
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        mDrawerLayout =  findViewById(R.id.vibration_drawer_layout);
        //dodato
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.vibration_nvView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        Log.d("nav", "record");
                        Intent intent = new Intent(VibrationActivity.this, SecondActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_record_fragment:
                        Log.d("nav", "record");
                        handleOnNavigationItemSelected(RECORD_FRAGMENT);
                        //Intent intent1 = new Intent(VibrationActivity.this, VibrationMap.class);
                        //Intent intent1 = new Intent(VibrationActivity.this, FFT.class);
                        //startActivity(intent1);
                        return true;
                    case R.id.nav_map_fragment:
                        Log.d("nav", "record");
                        Intent intent1 = new Intent(VibrationActivity.this, com.example.lukabaljak.elabcrowdsensing.Map.class);
                        startActivity(intent1);
                        //handleOnNavigationItemSelected(MAP_FRAGMENT);
                        return false; //da ne bi kada se korisnik vrati ostalo oznaceno da je na mapi                
                  /*  case R.id.nav_settings_fragment:

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
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void handleOnNavigationItemSelected(String fragment){
        int fragmentLastIndex = getSupportFragmentManager().getBackStackEntryCount() - 1;
        Fragment fragmentView;

        mDrawerLayout.closeDrawer(Gravity.START);

        switch (fragment) {
            case MAP_FRAGMENT:
                fragmentView = MapFragment.newInstance(Constants.VIBRATION);
                break;
            case RECORD_FRAGMENT:
                fragmentView = VibrationMapFragment.newInstance();
                break;
          /*  case SETTINGS_FRAGMENT:
                fragmentView = null;
                break; */
            default:
                return;
        }

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
                .replace(R.id.vibration_content_frame, fragmentView)
                .addToBackStack(fragment)
                .commit();
    }

    @Override
    public void onLocationChange(LatLng latLng) {

    }
}
