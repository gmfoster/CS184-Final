package edu.ucsb.cs.cs184.gfoster.gfostercdalyfinalproject;

import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;


public class AdminActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;

    //Firebase Stuff
    FirebaseDatabase db;
    DatabaseReference myRef;
    ArrayList<String> registeredAttendees = new ArrayList<String>();
    ArrayList<String> discoveredAttendees = new ArrayList<String>();
    Dictionary registeredDict = new Hashtable();
    ArrayList<String> discoveredUsers = new ArrayList<String>();
    ArrayList<String> emails = new ArrayList<>();

    ListView attendeeList;
    Button exportButton;


    //Initialize variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        db = FirebaseDatabase.getInstance();
        myRef = db.getReference("attendees");
        exportButton = findViewById(R.id.exportButton);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setExportButton();
                Toast.makeText(getApplicationContext(), "Attendees have been exported to your downloads folder", Toast.LENGTH_LONG).show();
            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot attendee : dataSnapshot.getChildren()) {
                    Attendee newAttendee = attendee.getValue(Attendee.class);
                    Log.i("DEBUG", "new attendee: " + newAttendee.username);
                    registeredAttendees.add(newAttendee.mac_address);
                    //registeredUsernames.add(newAttendee.username + "  :  " + newAttendee.email);
                    registeredDict.put(newAttendee.mac_address,newAttendee.username + "  :  " + newAttendee.email);
                    emails.add(newAttendee.email);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("DEBUG","DatabaseERROR");
            }
        });


        //Bluetooth Stuff

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        bluetoothAdapter.startDiscovery();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                //Finding devices
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {


                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // Add the name and address to an array adapter to show in a ListView
                    Log.i("DEBUG", device.getName() + "\n" + device.getAddress());
                    discoveredAttendees.add(device.getAddress());
                    if (registeredAttendees.contains(device.getAddress())) {
                        if (!discoveredUsers.contains(registeredDict.get(device.getAddress()).toString())) {
                            discoveredUsers.add(registeredDict.get(device.getAddress()).toString());
                            //discoveredAttendees.add(device.getAddress());
                            Log.i("DEBUG", "We found this device");
                            Log.i("DEBUG", discoveredUsers.toString());
                            updateAttendees();
                        }
                    }

                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    Log.i("DEBUG","Entered the Finished ");
                    bluetoothAdapter.startDiscovery();
                }
            }
        };

        IntentFilter found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, found_filter);
        IntentFilter finished_filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, finished_filter);

        Log.i("DEBUG", "registered attendees should be in arrayList");

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    List<ScanResult> results = wifiManager.getScanResults();
                } else {
                    // scan failure handling
                    Log.e("ERROR", "WIFI Error");
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);


    }

    public void updateAttendees() {
        attendeeList = findViewById(R.id.listView);
        final ArrayAdapter adapter = new ArrayAdapter(this, R.layout.simple_list_item_1, discoveredUsers);
        attendeeList.setAdapter(adapter);
        Log.i("DEBUG", emails.toString());
    }

    public void export(String fileName, String body) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        try {
            File file = new File(dir, fileName);
            FileWriter writer = new FileWriter(file);
            writer.append(body);
            writer.flush();
            writer.close();
            DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(file.getName(), file.getName(), true, "text/plain",file.getAbsolutePath(),file.length(),true);
        } catch (Exception e) { }
    }

    public void setExportButton() {
        //String meetName = "Meeting" + getDateTime();
        String meetName = "Meeting";
        export(meetName, discoveredUsers.toString());
    }

    public String getDateTime() {
        Date currentTime = Calendar.getInstance().getTime();
        return currentTime.toString();
    }

}
