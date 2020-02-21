package edu.ucsb.cs.cs184.gfoster.gfostercdalyfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AttendeeActivity extends AppCompatActivity {

    Button manualCheckin;
    FirebaseDatabase db;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);

        db = FirebaseDatabase.getInstance();
        myRef = db.getReference("attendees");

        manualCheckin = findViewById(R.id.manualCheckin);
        manualCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIn();
            }
        });
        //more setup stuff here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void checkIn() {
        Toast.makeText(getApplicationContext(), "You're all checked in!", Toast.LENGTH_LONG).show();
    }
}
