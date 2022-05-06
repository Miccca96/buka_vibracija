package com.example.lukabaljak.elabcrowdsensing;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Marker userLocationMarker;
    private LocationCallback mLocationCallback = null;
    private LocationRequest mLocationRequest = null;
    private OnFragmentInteractionListener mListener;

    private static int vrstaSnimanja;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(int iVrstaSnimanja) {
        MapFragment fragment = new MapFragment();
        vrstaSnimanja = iVrstaSnimanja;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mListener.onLocationChange(sydney);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        if (vrstaSnimanja == Constants.SOUND) {
            GetSound getSound = new GetSound();
            getSound.execute();
        } else {
            GetVibration getVibration = new GetVibration();
            getVibration.execute();
        }


    }

    public void accessUserLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    SoundActivity.MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                if (userLocationMarker != null) {
                                    userLocationMarker.remove();
                                }

                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                CameraUpdate updateCamera = CameraUpdateFactory.newLatLng(latLng);
                                userLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng));

                                mListener.onLocationChange(latLng);
                                mMap.moveCamera(updateCamera);
                            }
                        }
                    });
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    SoundActivity.MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onStart() {
        super.onStart();
//        startLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        //stopLocationUpdates();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onLocationChange(LatLng latLng);
    }

    private class GetSound extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... strings) {

            HTTPBroker.GET("http://www.skyvideo.rs/rec/controller.php?action=allMap&basic=1");
            Log.d("RESPONSE", HTTPBroker.stream);
            try {
                JSONArray jsonArray = new JSONArray(HTTPBroker.stream);
                return jsonArray;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            try {
                for (int i = 0; i < 50; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    double lat = jsonObject.getDouble("reclat");
                    double lon = jsonObject.getDouble("reclon");
                    if (lat == 0 || lon == 0) {
                        continue;
                    }
                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(jsonObject.getString("recid")));
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


    private class GetVibration extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... strings) {
            HTTPBroker.GET("http://www.skyvideo.rs/rec/controller.php?action=allVib");
            Log.d("RESPONSE", HTTPBroker.stream);
            try {
                JSONArray jsonArray = new JSONArray(HTTPBroker.stream);
                return jsonArray;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if (!jsonObject.getString("mac").equals(Common.getWifiMacAddress())) {
                        continue;
                    }

                    JSONArray merenja = jsonObject.getJSONArray("merenja");
                    for (int j = 0; j < merenja.length(); j++) {

                        double lat = merenja.getJSONObject(j).getDouble("vlat");
                        double lon = merenja.getJSONObject(j).getDouble("vlon");
                        Log.d("LokacijaKorisnika", "lat:" + lat + " lon:" + lon);
                        if (lat == 0 || lon == 0) {
                            continue;
                        }

//
                        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(merenja.getJSONObject(j).getString("vknaziv")));
                    }

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("VelicinaNizaVIbracija", jsonArray.length() + "");
        }
    }


}
