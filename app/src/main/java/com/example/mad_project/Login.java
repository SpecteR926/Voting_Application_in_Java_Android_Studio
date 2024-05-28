package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private DatabaseReference db;
    private ValueEventListener loginListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        EditText e1 = findViewById(R.id.etcnic);
        EditText e2 = findViewById(R.id.etpass);
        Button btnLogin = findViewById(R.id.btnLogin);

        db = FirebaseDatabase.getInstance().getReference().child("users");

        btnLogin.setOnClickListener(v -> {
            String a = e1.getText().toString();
            String b = e2.getText().toString();

            loginListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                    boolean userFound = false;
                    for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                        String m1 = snapshot.child("cnic").getValue().toString();
                        String m2 = snapshot.child("password").getValue().toString();
                        if (!a.isEmpty() && !b.isEmpty()) {
                            if (a.equals(m1) && b.equals(m2)) {
                                Toast.makeText(getApplicationContext(), "logged in", Toast.LENGTH_SHORT).show();
                                db.removeEventListener(loginListener);
                                Intent intt = new Intent(getApplicationContext(), VotingActivity.class);
                                intt.putExtra("1", m1);
                                startActivity(intt);
                                userFound = true;
                                break;
                            }
                        }
                    }
                    if (!userFound) {
                        Toast.makeText(getApplicationContext(), "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };

            db.addValueEventListener(loginListener);
        });
    }
}
