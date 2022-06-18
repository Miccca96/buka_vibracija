package com.example.lukabaljak.elabcrowdsensing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jtransforms.fft.DoubleFFT_1D;
import org.jtransforms.fft.FloatFFT_1D;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class FFT extends Fragment implements SensorEventListener {

    private OnFragmentInteractionListener mListener;
    private Sensor mySensor;
    private SensorManager SM;
    private Timer timer = null;

    private LineChart vibrationChart = null;
    private List<Entry> chartDataEntries = null;

    private Button startButton, stopButton;
    ToggleButton displayXToggle;

    Map<String, Boolean> displayAxis;
    private TextView timeView;
    private int totalSeconds;

    final int MAX_CAPACITY = 8192;
    final int GRAPH_CAPACITY = 128;

    float Fs = 8000;
    float T = 1/Fs;
    int L = 1600;

    float freq = 338;

    List<Merenje> merenja;
    LocationThread locationThread;

    public FFT() {
        // Required empty public constructor
    }

    @Override
    public void onStop() {
        super.onStop();
        locationThread.destroyLocationService();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        locationThread = new LocationThread(getActivity(), getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vibration_map, container, false);
        vibrationChart = view.findViewById(R.id.vibration_chart);
        timeView = view.findViewById(R.id.vibration_time_view);

        chartDataEntries = new ArrayList<>();

        displayAxis = new HashMap<>();
        displayAxis.put("x", true);
        displayAxis.put("y", true);
        displayAxis.put("z", true);

        startButton = view.findViewById(R.id.start_vibration_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("POCEO", "POCEO");
                openVibrationDialog();
            }
        });

        stopButton = view.findViewById(R.id.stop_vibration_button);
        stopButton.setEnabled(false);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ZAVRSIO", "ZAVRSIO");
                stop();
            }
        });

        displayXToggle = view.findViewById(R.id.displayXAxis);
        displayXToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayGraphForAxis("x");
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    private void displayGraphForAxis(String axis) {
        if (displayAxis.get(axis) == true) {
            displayAxis.put(axis, false);
            displayXToggle.setBackgroundColor(Color.RED);
        } else {
            displayAxis.put(axis, true);
            displayXToggle.setBackgroundColor(Color.LTGRAY);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static VibrationMapFragment newInstance() {
        VibrationMapFragment fragment = new VibrationMapFragment();
        return fragment;
    }

    float x = 1, y = 1, z = 1;
    float angleX = 0.5f, angleY = 0.5f, angleZ = 0.5f;
    float[] vibrations = new float[]{0, 0, 0};
    float[] actualVibrations = new float[]{0, 0, 0};
    float[] angles = new float[]{0, 0, 0};

    long prvoMerenje = 0;
    long poslednjeMerenje = 0;
    int brojac = 0;

    float[] arrayX = new float[MAX_CAPACITY];
    int iX = 0;
    float[] arrayY = new float[MAX_CAPACITY];
    int iY = 0;
    float[] arrayZ = new float[MAX_CAPACITY];
    int iZ = 0;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {



        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            vibrations = new float[]{sensorEvent.values[0],
                    sensorEvent.values[1],
                    sensorEvent.values[2]};
            brojac++;
            if (prvoMerenje == 0) {
                prvoMerenje = sensorEvent.timestamp;
            } else {
                poslednjeMerenje = sensorEvent.timestamp;
            }

        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            angles = new float[]{sensorEvent.values[0],
                    sensorEvent.values[1],
                    sensorEvent.values[2]};
        }

        float[] R = new float[9];
        float[] I = new float[9];
        SensorManager.getRotationMatrix(R, I, vibrations, angles);
        transformToActualVibrations(R);
    }

    private void transformToActualVibrations(float[] R) {
        actualVibrations[0] = R[0] * vibrations[0] + R[1] * vibrations[1] + R[2] * vibrations[2];
        actualVibrations[1] = R[3] * vibrations[0] + R[4] * vibrations[1] + R[5] * vibrations[2];
        actualVibrations[2] = R[6] * vibrations[0] + R[7] * vibrations[1] + R[8] * vibrations[2];

        if (iX == arrayX.length) {
            return;
        }
        arrayX[iX++] = actualVibrations[0];
        arrayY[iY++] = actualVibrations[1];
        arrayZ[iZ++] = actualVibrations[2];
    }

    private float arraySum(float[] vibrations) {
        float sum = 0;
        for (int i = 0; i < vibrations.length; i++) {
            sum += Math.abs(vibrations[i]);
        }
        return sum;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void start(View view) {

        enableButtons("start");

        SM = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mySensor = SM.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_FASTEST);

        mySensor = SM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        prvoMerenje = 0;
        poslednjeMerenje = 0;
        brojac = 0;

        merenja = new ArrayList<>();

        timer = new Timer();
        totalSeconds = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("VIBR", actualVibrations[0] + " " + actualVibrations[1] + " " + actualVibrations[2]);
                        timeView.setText(String.valueOf(totalSeconds++));
                        Merenje merenje = new Merenje(actualVibrations[0], actualVibrations[1], actualVibrations[2],
                                new GregorianCalendar(), locationThread.getLocation().getLongitude(), locationThread.getLocation().getLatitude());
                        merenja.add(merenje);
                        drawGraph();
                    }
                });
            }
        }, 0, 1000L);


    }

    private void drawGraph() {
        vibrationChart.clear();
        chartDataEntries.clear();


        float[] fftResult;

        if (iX > GRAPH_CAPACITY) {
            fftResult = new float[GRAPH_CAPACITY * 2];
            int iVibrations, iFFT;
            for (iVibrations = iX - GRAPH_CAPACITY, iFFT = 0; (iVibrations < iX) && (iFFT < GRAPH_CAPACITY); iVibrations++, iFFT++) {
                fftResult[iFFT] = (arrayX[iVibrations] + arrayY[iVibrations] + arrayZ[iVibrations]) / 3;

            }

        } else {
            fftResult = new float[iX * 2];
            for (int i = 0; i < iX; i++) {
                fftResult[i] = (arrayX[i] + arrayY[i] + arrayZ[i]) / 3;
            }
        }


        float sinValue_re_im[] = new float[L*2];
        // because FFT takes an array where its positions alternate between real and imaginary
        for( int i = 0; i < L; i++)
        {
            sinValue_re_im[2*i] = (float)Math.sin( 2*Math.PI*freq*(i * T) );
            // real part
            sinValue_re_im[2*i+1] = 0; //imaginary part
        }

        FloatFFT_1D fft = new FloatFFT_1D(L);
        fft.complexForward(sinValue_re_im);
        float[] tf = sinValue_re_im.clone();



        float[] P2 = new float[L];
        for(int i=0; i<L; i++){
            float re = tf[2*i]/L;
            float im = tf[2*i+1]/L;
            P2[i] =(float) Math.sqrt(re*re+im*im);

            //Log.d("re kod crteza",re+"");
            //Log.d("im kod crteza",im+"");

            Entry point = new Entry(i * 5, P2[i]);
            chartDataEntries.add(point);
        }


        float[] P1 = new float[L/2];
        // single-sided: the second half of P2 has the same values as the first half
        System.arraycopy(P2, 0, P1, 0, L/2);
        System.arraycopy(P1, 1, P1, 1, L/2-2);
        for(int i=1; i<P1.length-1; i++){
            P1[i] = 2*P1[i];
        }

        float[] f = new float[L/2 + 1];
        for(int i=0; i<L/2+1;i++){
            f[i] = Fs*((float) i)/L;
        }

/*
        for (int i = 0; i < fftResult.length / 2; i++) {


            float re = fftResult[2 * i];
            float im = fftResult[2 * i + 1];

            float res = (float) Math.sqrt(re * re + im * im);

            Entry point = new Entry(i * 5, res);
            chartDataEntries.add(point);
        }
*/
        LineDataSet lineDataSet = new LineDataSet(chartDataEntries, "");
        vibrationChart.getXAxis().setAxisMaximum(GRAPH_CAPACITY * 5);
        lineDataSet.setColor(Color.RED);
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setCircleRadius(1f);
        vibrationChart.setData(new LineData(lineDataSet));

        YAxis yAxis = vibrationChart.getAxisLeft();
        yAxis.setAxisMaximum(30);
        yAxis.setAxisMinimum(0);
        yAxis = vibrationChart.getAxisRight();
        yAxis.setAxisMaximum(30);
        yAxis.setAxisMinimum(0);


        vibrationChart.invalidate();
    }

    int kontekstVibracije;

    public void openVibrationDialog() {
        if (locationThread.getLocation() == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Prvo uključite lokaciju", Toast.LENGTH_SHORT).show();
            return;
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
        dialog.setMessage("Odaberite lokaciju merenja");

        LinearLayout layout = new LinearLayout(this.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);


        final RadioGroup radioGroup = new RadioGroup(this.getContext());

        final KategorijeMerenja kategorijeMerenja = KategorijeMerenja.getInstance();
        for (int i = 0; i < kategorijeMerenja.count(); i++) {
            RadioButton radioButton = new RadioButton(this.getContext());
            radioButton.setText(kategorijeMerenja.getNazivKategorije(i + 1));
            radioGroup.addView(radioButton);
        }

        layout.addView(radioGroup);

        dialog.setView(layout);

        dialog.setPositiveButton("PRIHVATI",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if (radioGroup.getCheckedRadioButtonId() == -1) {
                            Toast.makeText(getContext(), "Morate prvo odabrati lokaciju", Toast.LENGTH_SHORT).show();
                            openVibrationDialog();
                        } else {

                            RadioButton rb = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());

                            kontekstVibracije = kategorijeMerenja.getIDKategorije(String.valueOf(rb.getText()));
                            Log.d("radioID", String.valueOf(kontekstVibracije));
                            start(null);
                        }
                    }
                });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (radioGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getContext(), "Morate prvo odabrati lokaciju", Toast.LENGTH_SHORT).show();
                    openVibrationDialog();
                }
            }
        });

        dialog.show();
    }


    public void stop() {
        enableButtons("stop");

        SM.unregisterListener(this);
        timer.cancel();

        float[] fftResultX;
        fftResultX = new float[iX * 2];

        for (int i = 0; i < iX; i++) {
            fftResultX[i] = arrayX[i];
        }

        FloatFFT_1D fft = new FloatFFT_1D(fftResultX.length);
        fft.realForward(fftResultX);

        Log.d("FFT RESULT",fftResultX.length+" ");

        TreeMap<Integer, Double> treeMap = new TreeMap<>();





        float sinValue_re_im[] = new float[L*2];
        // because FFT takes an array where its positions alternate between real and imaginary
        for( int i = 0; i < L; i++)
        {
            sinValue_re_im[2*i] = (float)Math.sin( 2*Math.PI*freq*(i * T) );
            // real part
            sinValue_re_im[2*i+1] = 0; //imaginary part
        }

        fft = new FloatFFT_1D(L);
        fft.complexForward(sinValue_re_im);
        float[] tf = sinValue_re_im.clone();



        float[] P2 = new float[L];
        for(int i=0; i<L; i++){
            float re = tf[2*i]/L;
            float im = tf[2*i+1]/L;
            P2[i] =(float) Math.sqrt(re*re+im*im);

           // Log.d("re kod stopa",re+"");
           // Log.d("im kod stopa",im+"");


            Entry point = new Entry(i * 5, P2[i]);
            chartDataEntries.add(point);
        }


        float[] P1 = new float[L/2];
        // single-sided: the second half of P2 has the same values as the first half
        System.arraycopy(P2, 0, P1, 0, L/2);
        System.arraycopy(P1, 1, P1, 1, L/2-2);
        for(int i=1; i<P1.length-1; i++){
            P1[i] = 2*P1[i];
        }

        float[] f = new float[L/2 + 1];
        for(int i=0; i<L/2+1;i++){
            f[i] = Fs*((float) i)/L;
        }

        /*for (int i = 0; i < fftResultX.length / 2; i++) {
            float re = fftResultX[2 * i];
            float im = fftResultX[2 * i + 1];

            Log.d("re kod stopa",re+" ");
            Log.d("im kod stopa",im+" ");


            float res = (float) Math.sqrt(re * re + im * im);
            treeMap.put(i * 5, Double.valueOf(res));
        }*/

        Log.d("velicina drveta", treeMap.size() + "");

        JSONArray jsonArrayX = new JSONArray();
        for (Map.Entry<Integer, Double> entry : treeMap.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(String.valueOf(entry.getKey()), entry.getValue());

                jsonArrayX.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("fftX", jsonArrayX.toString());

        jsonArrayX = getJSON_fftX();
        JSONArray jsonArrayY = getJSON_fftY();
        JSONArray jsonArrayZ = getJSON_fftZ();


        vibrations = new float[]{0, 0, 0};
        actualVibrations = new float[]{0, 0, 0};
        angles = new float[]{0, 0, 0};

        arrayX = new float[MAX_CAPACITY];
        iX = 0;
        arrayY = new float[MAX_CAPACITY];
        iY = 0;
        arrayZ = new float[MAX_CAPACITY];
        iZ = 0;

        Log.d("VELICINA NIZA", merenja.size() + "");
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("id", Context.MODE_PRIVATE);
        String macAddress = sharedPreferences.getString("id", null);

        RezultatMerenja rezultatMerenja = new RezultatMerenja(new Id(macAddress), merenja);
        rezultatMerenja.setKontekst("at_home");
        String listaMerenjaUJSONu = Common.dajBoljuListuMerenjaUJSONU(rezultatMerenja, kontekstVibracije);
        String MACAddress = Common.getWifiMacAddress();
        String glavniJSON = Common.dajCeoJSON(macAddress, "23.1.2019. 2:41:38", listaMerenjaUJSONu, jsonArrayX, jsonArrayY, jsonArrayZ);
        Log.d("listaMerenjaUJSONu", listaMerenjaUJSONu);
        Log.d("glavniUJSONu", glavniJSON);

        PostData postData = new PostData();
        postData.execute("https://crowdsensing.elab.fon.bg.ac.rs/a1.php?vib=1", glavniJSON);

    }

    private void enableButtons(String state) {
        if (state.equals("start")) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);

        }
    }

    private void printMatrixToLog(float[] matrix, String type) {

        for (int i = 0; i < matrix.length; i += 3) {
            Log.d(type, matrix[i] + " " + matrix[i + 1] + " " + matrix[i + 2]);
        }
    }


    JSONArray getJSON_fftX() {
        float[] fftResultX;
        fftResultX = new float[iX * 2];

        for (int i = 0; i < iX; i++) {
            fftResultX[i] = arrayX[i];
        }

        FloatFFT_1D fft = new FloatFFT_1D(fftResultX.length);
        fft.realForward(fftResultX);

        TreeMap<Integer, Double> treeMap = new TreeMap<>();
        for (int i = 0; i < fftResultX.length / 2; i++) {
            float re = fftResultX[2 * i];
            float im = fftResultX[2 * i + 1];

            float res = (float) Math.sqrt(re * re + im * im);
            treeMap.put(i * 5, Double.valueOf(res));
        }

        Log.d("velicina drveta", treeMap.size() + "");

        JSONArray jsonArrayX = new JSONArray();
        for (Map.Entry<Integer, Double> entry : treeMap.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(String.valueOf(entry.getKey()), entry.getValue());

                jsonArrayX.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("fftX", jsonArrayX.toString());

        return jsonArrayX;
    }

    JSONArray getJSON_fftY() {
        float[] fftResultY;
        fftResultY = new float[iY * 2];

        for (int i = 0; i < iY; i++) {
            fftResultY[i] = arrayY[i];
        }

        FloatFFT_1D fft = new FloatFFT_1D(fftResultY.length);
        fft.realForward(fftResultY);

        TreeMap<Integer, Double> treeMap = new TreeMap<>();
        for (int i = 0; i < fftResultY.length / 2; i++) {
            float re = fftResultY[2 * i];
            float im = fftResultY[2 * i + 1];

            float res = (float) Math.sqrt(re * re + im * im);
            treeMap.put(i * 5, Double.valueOf(res));
        }

        JSONArray jsonArrayY = new JSONArray();
        for (Map.Entry<Integer, Double> entry : treeMap.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(String.valueOf(entry.getKey()), entry.getValue());

                jsonArrayY.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("fftY", jsonArrayY.toString());

        return jsonArrayY;
    }

    JSONArray getJSON_fftZ() {
        float[] fftResultZ;
        fftResultZ = new float[iZ * 2];

        for (int i = 0; i < iZ; i++) {
            fftResultZ[i] = arrayZ[i];
        }

        FloatFFT_1D fft = new FloatFFT_1D(fftResultZ.length);
        fft.realForward(fftResultZ);

        TreeMap<Integer, Double> treeMap = new TreeMap<>();
        for (int i = 0; i < fftResultZ.length / 2; i++) {
            float re = fftResultZ[2 * i];
            float im = fftResultZ[2 * i + 1];

            float res = (float) Math.sqrt(re * re + im * im);
            treeMap.put(i * 5, Double.valueOf(res));
        }


        JSONArray jsonArrayZ = new JSONArray();
        for (Map.Entry<Integer, Double> entry : treeMap.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(String.valueOf(entry.getKey()), entry.getValue());

                jsonArrayZ.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("fftZ", jsonArrayZ.toString());

        return jsonArrayZ;
    }

    public void ispisiTrajanje(View view) {
        long trajanje = (poslednjeMerenje - prvoMerenje) / 1000000000;

        Log.d("TRAJANJE MERENJA", String.valueOf(trajanje));
        Log.d("BROJAC", String.valueOf(brojac));
        Log.d("FREQ", String.valueOf(((long) brojac) / trajanje));
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
        void onFragmentInteraction(Uri uri);
    }


    int brojPokusaja = 0;
    boolean uspeo = false;

    private class PostData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            final String urlString = strings[0];
            final String newValue = strings[1];

            Log.d("urlString", urlString);
            Log.d("newValue", newValue);

            HTTPBroker.POSTWithCertificate(getActivity().getApplicationContext(), urlString, newValue);

            if (HTTPBroker.stream != null) {
                Log.d("RESPONSE", HTTPBroker.stream);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(), "Podaci su uspešno poslati.", Toast.LENGTH_SHORT).show();
                    }
                });
                uspeo = true;
                HTTPBroker.stream = null;
            } else {
                Log.d("RESPONSE", "nymyyyyy");
                final Timer sendDataTimer = new Timer();

                sendDataTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        HTTPBroker.POSTWithCertificate(getActivity().getApplicationContext(), urlString, newValue);

                        if (HTTPBroker.stream != null) {
                            Log.d("RESPONSE", HTTPBroker.stream);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity().getApplicationContext(), "Podaci su uspešno poslati.", Toast.LENGTH_SHORT).show();
                                }
                            });

                            uspeo = true;
                            HTTPBroker.stream = null;
                            cancel();
                        }
                        if ((brojPokusaja++) > 5) {
                            Log.d("Tried", "but faileeed");
                            brojPokusaja = 0;
                            uspeo = false;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity().getApplicationContext(), "Podaci nisu uspešno poslati, obezbedite internet konekciju.", Toast.LENGTH_SHORT).show();
                                }
                            });

                            cancel();
                        }
                    }
                }, 3000L, 3000L);

            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
        }
    }

}

