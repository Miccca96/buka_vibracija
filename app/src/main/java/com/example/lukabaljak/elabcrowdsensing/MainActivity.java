package com.example.lukabaljak.elabcrowdsensing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.service.chooser.ChooserTarget;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    EditText _imePrezimeText;
    EditText _brojIndeksaText;

    Button _signupButton;
    TextView _loginLink;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("id", MODE_PRIVATE);
        String id = sharedPreferences.getString("id", null);
        if (id != null) {
            Intent intent = new Intent(this, SecondActivity.class);
            String macAddress = Common.getWifiMacAddress();

            Log.d("MACAdress", macAddress);
            startActivity(intent);
            finish();
        }

        _imePrezimeText = findViewById(R.id.input_imePrezime);
        _brojIndeksaText = findViewById(R.id.input_brojIndeksa);

        _signupButton = findViewById(R.id.btn_signup);
    }

    public void signup(View view) {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        String name = _imePrezimeText.getText().toString();
        String email = _brojIndeksaText.getText().toString();


        // TODO: Implement your own signup logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();

                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        String macAddress = Common.getWifiMacAddress();
        String imePrezime = String.valueOf(_imePrezimeText.getText());
        String brojIndeksa = String.valueOf(_brojIndeksaText.getText());
        Log.d("MACAdress", macAddress);
        //String urlString = Common.getPOSTAddress();
        String json = Common.getSingInJSON(macAddress, imePrezime, brojIndeksa);
        Log.d("json", json);

        //radice posle
        String urlString = "https://crowdsensing.elab.fon.bg.ac.rs/controller.php?action=registracija";

        PostData postData = new PostData();
        postData.execute(urlString, json);

        editor.putString("id", macAddress);
        editor.apply();

        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String imePrezime = String.valueOf(_imePrezimeText.getText());
        String brojIndeksa = String.valueOf(_brojIndeksaText.getText());


        String imePrezimeError = validateImePrezime(imePrezime);
        if (imePrezimeError == null) {
            _imePrezimeText.setError(null);
        } else {
            _imePrezimeText.setError(imePrezimeError);
            valid = false;
        }

        if (validateBrojIndeksa(brojIndeksa)) {
            _brojIndeksaText.setError(null);
        } else {
            _brojIndeksaText.setError("Broj indeksa uneti kao br/godina.");
            valid = false;
        }

        return valid;
    }

    private String validateImePrezime(String imePrezime) {
        if (imePrezime.isEmpty()) {
            return "Niste uneli ime i prezime.";
        }
        if (imePrezime.split(" ").length < 2 || imePrezime.split(" ").length > 2) {
            return "Niste ispravno uneli ime i prezime.";
        }
        if (!Character.isUpperCase(imePrezime.split(" ")[0].charAt(0))) {
            return "Ime mora početi velikim slovom.";
        }
        if (!Character.isUpperCase(imePrezime.split(" ")[1].charAt(0))) {
            return "Prezime mora početi velikim slovom.";
        }
        return null;
    }

    private boolean validateBrojIndeksa(String brojIndeksa) {
        if (brojIndeksa.matches("[1-9]{1}[0-9]{0,3}\\/20(0[0-9]|1[0-9]|2[0-2])")) {
            return true;
        }
        return false;
    }

    private class PostData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String urlString = strings[0];
            String json = strings[1];

            //posle
            HTTPBroker.POSTWithCertificate(getApplicationContext(), urlString,json);
            if(HTTPBroker.stream!=null){
               Log.d("RESPONSE", HTTPBroker.stream);
           }



            return null;
        }
    }

}
