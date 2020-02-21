package edu.ucsb.cs.cs184.gfoster.gfostercdalyfinalproject;

import android.app.IntentService;
import android.content.Intent;



public class GeofenceTrasitionService extends IntentService {

    private static final String construct = GeofenceTrasitionService.class.getSimpleName();
    public GeofenceTrasitionService() {
        super(construct);
        return;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //placeholder for db/how to handle the geofence.
    }







}
