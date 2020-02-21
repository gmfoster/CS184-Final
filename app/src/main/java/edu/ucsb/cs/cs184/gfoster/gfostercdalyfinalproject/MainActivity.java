package edu.ucsb.cs.cs184.gfoster.gfostercdalyfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class MainActivity extends AppCompatActivity {

    Button attendeeLogin;
    Button adminLogin;
    EditText email;
    EditText username;
    String text_email;
    String text_username;
    String device_address;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final static int REQUEST_ENABLE_BT = 1;

    FirebaseDatabase db;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        attendeeLogin  = findViewById(R.id.loginAttendee);
        adminLogin = findViewById(R.id.loginAdmin);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);

        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                }
            });
            builder.show();
        }
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.i("DEBUG", "Device doesnt support bluetooth :(");
        }
        if (!bluetoothAdapter.isEnabled()) {
            Log.i("DEBUG", "Bluetooth adapter is not enabled, requesting...");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        device_address = bluetoothAdapter.getAddress();
        Log.i("DEBUG", "Device Address: " + device_address);


        adminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adminClick(view);
            }
        });

        attendeeLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attendeeRegister(view);
            }
        });
        db = FirebaseDatabase.getInstance();
        myRef = db.getReference();

    }

    public void attendeeRegister(View view) {
        text_email = email.getText().toString();
        text_username = username.getText().toString();
        Log.i("DEBUG", "Attendee Register Clicked");
        Log.i("DEBUG", text_email + " : " + text_username);

        Attendee attendee = new Attendee(text_email, text_username, device_address);
        myRef.child("attendees").child(device_address).setValue(attendee);
        Log.i("DEBUG", "Attendee should be in database");

        Intent attendeeIntent = new Intent(this, AttendeeActivity.class);
        startActivity(attendeeIntent);
    }

    public void adminClick(View view) {
        text_email = email.getText().toString();
        text_username = username.getText().toString();
        Log.i("DEBUG", "Admin Login Clicked");
        Log.i("DEBUG", text_email + " : " + text_username);

        Attendee admin = new Attendee(text_email, text_username, device_address);
        myRef.child("admins").child(device_address).setValue(admin);

        Intent adminIntent = new Intent(this, GeoFenceActivity.class);
        startActivity(adminIntent);
    }
}
