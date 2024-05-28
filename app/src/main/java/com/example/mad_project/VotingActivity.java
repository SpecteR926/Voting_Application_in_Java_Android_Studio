package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class VotingActivity extends AppCompatActivity {
    public static String a = "";
    private DatabaseReference db;
    private DatabaseReference db2;
    private ValueEventListener userListener;
    private ValueEventListener partyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voting);

        Intent obj = getIntent();
        String data = obj.getStringExtra("1");

        db = FirebaseDatabase.getInstance().getReference().child("users");
        db2 = FirebaseDatabase.getInstance().getReference().child("parties");

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    String m1 = snapshot.child("cnic").getValue().toString();
                    boolean di = (Boolean) snapshot.child("voted").getValue();
                    if (data.equals(m1)) {
                        if (!di) {
                            setupVotingOptions(snapshot);
                        } else {
                            Toast.makeText(getApplicationContext(), "Already voted", Toast.LENGTH_SHORT).show();
                            db.removeEventListener(userListener);
                            navigateToStartup();
                        }
                        break; // Stop the loop once the user is found
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        db.addValueEventListener(userListener);
    }

    private void setupVotingOptions(DataSnapshot userSnapshot) {
        TextView t1 = findViewById(R.id.selectedone);
        Button b1 = findViewById(R.id.party1Button);
        Button b2 = findViewById(R.id.party2Button);
        Button b3 = findViewById(R.id.saveRecordButton);

        b1.setOnClickListener(v -> {
            t1.setText("ITP");
            a = "ITP";
        });

        b2.setOnClickListener(v -> {
            t1.setText("SSS");
            a = "SSS";
        });

        b3.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Voted", Toast.LENGTH_SHORT).show();
            authenticateAndSaveVote(userSnapshot);
        });
    }

    private void authenticateAndSaveVote(DataSnapshot userSnapshot) {
        BiometricManager biometricManager = BiometricManager.from(getApplicationContext());
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(getApplicationContext(), "Device Doesn't have fingerprint", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(getApplicationContext(), "Not Working", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(getApplicationContext(), "No FINGER ASSIGNED", Toast.LENGTH_SHORT).show();
                return;
        }

        Executor executor = ContextCompat.getMainExecutor(getApplicationContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(VotingActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Voting Failed", Toast.LENGTH_SHORT).show();
                db.removeEventListener(userListener);
                navigateToStartup();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Fingerprint Success", Toast.LENGTH_SHORT).show();
                saveVote(userSnapshot);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        biometricPrompt.authenticate(new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric authentication")
                .setSubtitle("Authenticate to vote")
                .setNegativeButtonText("Cancel")
                .build());
    }

    private void saveVote(DataSnapshot userSnapshot) {
        DatabaseReference userRef = userSnapshot.getRef();
        userRef.child("voted").setValue(true);
        userRef.child("party").setValue(a);
        Toast.makeText(getApplicationContext(), "Vote done", Toast.LENGTH_SHORT).show();
        partyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean partyFound = false;
                for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                    String partyName = snapshot2.child("name").getValue().toString();
                    if (a.equals(partyName)) { // Compare with partyName instead of m1
                        partyFound = true;
                        DatabaseReference partyRef = snapshot2.getRef();
                        String votesStr = snapshot2.child("votes").getValue().toString();
                        int votes = Integer.parseInt(votesStr);
                        partyRef.child("votes").setValue(votes + 1);
                        Toast.makeText(getApplicationContext(), "votes added for " + partyName, Toast.LENGTH_SHORT).show();
                        db.removeEventListener(userListener);
                        db2.removeEventListener(partyListener);
                        navigateToEnd();
                        break; // Stop the loop once the party is found and vote is added
                    }
                }
                if (!partyFound) {
                    Toast.makeText(getApplicationContext(), "Party not found: " + a, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        db2.addValueEventListener(partyListener);
    }

    private void navigateToStartup() {
        Intent intent = new Intent(getApplicationContext(), Startup_Activity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToEnd() {
        Intent intent = new Intent(VotingActivity.this, EndActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.removeEventListener(userListener);
        if (partyListener != null) {
            db2.removeEventListener(partyListener);
        }
    }
}
