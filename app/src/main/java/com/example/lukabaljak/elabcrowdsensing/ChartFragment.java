package com.example.lukabaljak.elabcrowdsensing;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lukabaljak.elabcrowdsensing.thread.AudioRecordThread;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartFragment extends Fragment implements AudioRecordThread.OnAudioRecordingInterface {
    private OnFragmentInteractionListener mListener;

    private LineChart soundChart = null;
    private List<Entry> chartDataEntries = null;

    private Button startRecordingButton = null;
    private Button stopRecordingButton = null;
    private Button settingsButton = null;

    private TextView timeView = null;

    private AudioRecordThread audioRecordThread = null;
    int totalSeconds = 0;
    private Timer timer = null;
    private int numOfSample = 0;

    private HashMap<String, Double> signalMap = new HashMap<>();

    GregorianCalendar startOfMeasurement;
    String naziv, opis;
    String globalNaziv, globalOpis;

    LocationThread locationThread;

    public ChartFragment() {
        // Required empty public constructor
    }

    public static ChartFragment newInstance() {
        ChartFragment fragment = new ChartFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationThread = new LocationThread(getActivity(), getActivity().getApplicationContext());


    }

    @Override
    public void onResume() {
        super.onResume();
        setUpChart();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (audioRecordThread != null && audioRecordThread.isAlive()) {
            audioRecordThread.interrupt();
        }
        locationThread.destroyLocationService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        soundChart = (LineChart) view.findViewById(R.id.sound_chart);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        chartDataEntries = new ArrayList<>();

        startRecordingButton = (Button) view.findViewById(R.id.start_recording_button);
        stopRecordingButton = (Button) view.findViewById(R.id.stop_recording_button);
        settingsButton = view.findViewById(R.id.settingsButton);

        timeView = (TextView) view.findViewById(R.id.time_view);
        timeView.setText(String.valueOf(0));

        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
        return view;
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

    public void openDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
        dialog.setMessage("Unesite naziv i opis (opciono)");

        LinearLayout layout = new LinearLayout(this.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);


        //naziv
        final EditText nazivEditText = new EditText(this.getContext());
        nazivEditText.setHint("Naziv");
        layout.addView(nazivEditText);

        //opis
        final EditText opisEditText = new EditText(this.getContext());
        opisEditText.setHint("Opis");
        layout.addView(opisEditText);

        if (naziv != null) {
            if (!naziv.isEmpty()) {
                nazivEditText.setText(naziv);
                opisEditText.setError("Niste uneli opis!");
            }
            if (!opis.isEmpty()) {
                opisEditText.setText(opis);
                nazivEditText.setError("Niste uneli naziv!");
            }
        }

        if (globalNaziv != null && globalOpis != null) {
            if (naziv == null && opis != null) {
                nazivEditText.setText("");
                nazivEditText.setError("Niste uneli naziv!");
                opisEditText.setText(globalOpis);
            }
            if (naziv != null && opis == null) {
                nazivEditText.setText(globalNaziv);
                opisEditText.setText("");
                opisEditText.setError("Niste uneli opis!");
            }
            if (opis == null && naziv == null) {
                nazivEditText.setText(globalNaziv);
                opisEditText.setText(globalOpis);
            }
        }

        dialog.setView(layout);

        dialog.setPositiveButton("PRIHVATI",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        naziv = nazivEditText.getText().toString();
                        opis = opisEditText.getText().toString();
                        if (nazivEditText.getText().toString().isEmpty()) {
                            //Toast.makeText(getApplicationContext(), "Niste uneli naziv", Toast.LENGTH_SHORT).show();
                            openDialog();
                        } else if (opisEditText.getText().toString().isEmpty()) {
                            //Toast.makeText(getApplicationContext(), "Niste uneli opis", Toast.LENGTH_SHORT).show();
                            openDialog();
                        } else {
                            globalNaziv = naziv;
                            globalOpis = opis;

                            naziv = null;
                            opis = null;
                        }
                    }
                });

        dialog.setNegativeButton("ZATVORI",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        naziv = null;
                        opis = null;


                    }
                });
        dialog.show();
    }


    private void setUpChart() {
        soundChart.getXAxis().setAxisMinimum(0);
        soundChart.getAxis(YAxis.AxisDependency.LEFT).setAxisMinimum(0);
        soundChart.getAxis(YAxis.AxisDependency.LEFT).setAxisMaximum(100);
        soundChart.getAxis(YAxis.AxisDependency.RIGHT).setEnabled(false);
        soundChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        soundChart.getXAxis().setDrawGridLines(false);
        soundChart.getXAxis().setAxisMinimum(0);
        soundChart.getXAxis().setAxisMaximum(100);
        chartDataEntries.add(new Entry(0, 0));
        LineDataSet lineDataSet = new LineDataSet(chartDataEntries, "");
        LineData lineData = new LineData(lineDataSet);
        soundChart.setData(lineData);
        soundChart.invalidate();
    }

    public void startRecording() {
        if (locationThread.getLocation() == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Prvo uključite lokaciju", Toast.LENGTH_SHORT).show();
            return;
        }

        startOfMeasurement = new GregorianCalendar();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    SoundActivity.MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

        } else {
            audioRecordThread = new AudioRecordThread(this);
            audioRecordThread.start();
            totalSeconds = 0;
            timer = new Timer();
            numOfSample = 0;


            signalMap = new HashMap<>();

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeView.setText(String.valueOf(totalSeconds++));
                        }
                    });
                }
            }, 0, 1000L);

            startRecordingButton.setVisibility(View.GONE);
            stopRecordingButton.setVisibility(View.VISIBLE);
        }
    }

    public void stopRecording() {
        if (audioRecordThread != null) {
            audioRecordThread.stopRecording();
            startRecordingButton.setVisibility(View.VISIBLE);
            stopRecordingButton.setVisibility(View.GONE);
        }

        if (timer != null) {
            timer.cancel();
        }

        for (Map.Entry<String, Double> entry : signalMap.entrySet()) {
            entry.setValue(entry.getValue() / numOfSample);
        }

        GregorianCalendar endOfRecording = new GregorianCalendar();

        mListener.onDataCollected(signalMap, globalNaziv, globalOpis, locationThread.getLocation().getLongitude(), locationThread.getLocation().getLatitude());
    }

    @Override
    public void readAudioDataArray(final double[] data, final int rate) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("RATE", rate + "");
                chartDataEntries.clear();
                chartDataEntries.add(new Entry(0, 0));
                for (int i = 0; i < data.length; i++) {
                    int x = rate * i;

                    int downy = (int) ((data[i] * 10));
                    chartDataEntries.add(new Entry(x, downy));
                }

                LineDataSet lineDataSet = new LineDataSet(chartDataEntries, "");
                soundChart.getXAxis().setAxisMaximum(data.length * rate);
                soundChart.setData(new LineData(lineDataSet));
                soundChart.invalidate();
            }
        });

        calculateAverageSignal(data, rate);
        numOfSample++;
    }

    public void calculateAverageSignal(double[] data, int rate) {
        for (int i = 0; i < data.length / 2; i++) {
            double x = rate * i;

            double re = data[2 * i];
            double im = data[2 * i + 1];

            double res = Math.sqrt(re * re + im * im);

            if (!signalMap.containsKey(x + "")) {
                signalMap.put(x + "", res);
            } else {
                signalMap.put(x + "", signalMap.get(x + "") + res);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
       // stopRecording();
        mListener = null;

    }

    public interface OnFragmentInteractionListener {
        void onDataCollected(HashMap<String, Double> signalMap, String naziv, String opis, double longitude, double latitude);
    }

    private class PutData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String urlString = strings[0];
            String newValue = strings[1];

            Log.d("urlString", urlString);
            Log.d("newValue", newValue);

            HTTPBroker.PUT(urlString, newValue);

            return null;
        }
    }

}
