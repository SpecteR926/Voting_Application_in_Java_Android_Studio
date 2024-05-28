package com.example.mad_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class EndActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        Toast.makeText(getApplicationContext(), "Thank you, your vote matters", Toast.LENGTH_SHORT).show();
        Intent i1 = new Intent(getApplicationContext(),Startup_Activity.class);
        startActivity(i1);
    }
}