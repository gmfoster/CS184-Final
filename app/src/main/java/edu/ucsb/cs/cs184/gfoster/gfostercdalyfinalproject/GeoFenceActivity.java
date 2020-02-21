package edu.ucsb.cs.cs184.gfoster.gfostercdalyfinalproject;
// referenced https://www.raywenderlich.com/7372-geofencing-api-tutorial-for-android
//referenced https://code.tutsplus.com/tutorials/how-to-work-with-geofences-on-android--cms-26639


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Color;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;



import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;





public class GeoFenceActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {

    private static final String TAG = "GEOFENCE";
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private SupportMapFragment mapFragment;
    private Circle fenceCircle;
    private LocationRequest locationRequest;
    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;
    private static SeekBar radiusBar;
    Button startButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        radiusBar = (SeekBar)findViewById(R.id.radiusBar);
        radiusBar.setVisibility(View.INVISIBLE);
        startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMeeting();
            }
        });
        createGoogleApi();

        initGMaps();
    }
    private void createGoogleApi() {
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    private void initGMaps(){
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
//        LatLng ucsb = new LatLng(34.412939, -119.847863);
//        map.moveCamera(CameraUpdateFactory.newLatLng(ucsb));
        map.setMinZoomPreference(15.3f);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {

        markerForGeofence(latLng);
        radiusBar.setVisibility(View.VISIBLE);
        myBarr();
        removeGeofenceDraw();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        return;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        return;
    }

    private void getLastKnownLocation() {
        if ( true ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
    }

    private void writeLastLocation() {
        markerLocation(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
    }
    private Marker geoFenceMarker;
    private Marker locationMarker;
    private void markerLocation(LatLng latLng) {

        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(title);
        if ( map!=null ) {
            if ( locationMarker != null ){
                locationMarker.remove();
                Log.i("Debug", "yooo");
            }
            if (geoFenceMarker != null)
                geoFenceMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            geoFenceMarker = locationMarker;
            float zoom = 19;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
        markerForGeofence(latLng);
        radiusBar.setVisibility(View.VISIBLE);
        myBarr();

    }


    private void markerForGeofence(LatLng latLng) {
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(title);
        if ( map!=null ) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();
            if(locationMarker != null)
                locationMarker.remove();

            geoFenceMarker = map.addMarker(markerOptions);

        }
    }


    private static final long GEO_DURATION = 6000;
    private static final String GEOFENCE_REQ_ID = "myGeofence";
    private static float GEOFENCE_RADIUS = 10.0f; // in meters

    private Geofence createGeofence( LatLng latLng, float radius ) { //not working
        //change radius to match drawn radius
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( GEO_DURATION )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }


    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()){
            drawGeofence();
        }
    }



    private void drawGeofence() {

        if (fenceCircle != null)
            fenceCircle.remove();
        CircleOptions circleOptions = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(200, 0,0,255))
                .fillColor( Color.argb(100, 0,0,255) )
                .radius( GEOFENCE_RADIUS );
        fenceCircle = map.addCircle( circleOptions );
    }

    private void removeGeofenceDraw() {
        if ( geoFenceMarker != null && fenceCircle != null)
            fenceCircle.remove();
    }

    public void myBarr( ){
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                GEOFENCE_RADIUS = radiusBar.getProgress() * 2;
                drawGeofence();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void startMeeting() {
        Intent adminIntent = new Intent(this, AdminActivity.class);
        startActivity(adminIntent);
    }



}
