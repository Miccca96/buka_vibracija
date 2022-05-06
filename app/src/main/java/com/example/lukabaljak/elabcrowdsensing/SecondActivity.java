package com.example.lukabaljak.elabcrowdsensing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    public void openSoundActivity(View view) {
        Intent intent = new Intent(this, SoundActivity.class);
        startActivity(intent);
    }

    public void openVibrationActivity(View view) {
        Intent intent = new Intent(this, VibrationActivity.class);
        startActivity(intent);

    }
}
