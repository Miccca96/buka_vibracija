package com.example.lukabaljak.elabcrowdsensing;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        mDrawerLayout =  findViewById(R.id.vibration_drawer_layout);

        navigationView = findViewById(R.id.vibration_nvView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_record_fragment:
                        Log.d("nav", "record");
                        handleOnNavigationItemSelected(RECORD_FRAGMENT);
                        return true;
                    case R.id.nav_map_fragment:
                        Log.d("nav", "record");
                        handleOnNavigationItemSelected(MAP_FRAGMENT);
                        return true;
                    case R.id.nav_settings_fragment:
                        Log.d("nav", "record");
                        handleOnNavigationItemSelected(SETTINGS_FRAGMENT);
                        return true;
                }
                return false;
            }
        });

        handleOnNavigationItemSelected(RECORD_FRAGMENT);

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
            case SETTINGS_FRAGMENT:
                fragmentView = null;
                break;
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
