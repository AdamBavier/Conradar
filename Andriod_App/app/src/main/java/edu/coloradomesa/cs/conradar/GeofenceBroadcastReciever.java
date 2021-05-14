package edu.coloradomesa.cs.conradar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/*
This file is what runs when the geofence is triggered
 */


public class GeofenceBroadcastReciever extends BroadcastReceiver  {
    //String rootstr = "Jack/" + "UniqueID/";
    //FirebaseDatabase database;
    String TAG = GeofenceBroadcastReciever.class.getSimpleName();
    FirebaseDBHelper dbHelper = new FirebaseDBHelper();
    String rootstr = dbHelper.getRootSTR();
    FirebaseDatabase database = dbHelper.getDB();


    private FusedLocationProviderClient fusedLocationProviderClient;



    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Toast.makeText(context, "Geofence Triggered", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Geofence Recieve");
        NotificationHelper notificationHelper = new NotificationHelper(context);

        //Firebase Setup
        database = FirebaseDatabase.getInstance();
        DatabaseReference adventureMode = database.getReference(rootstr + "AdventureMode");


        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()){
            Log.d("yes", "onreceive: error recieving geofence event");
            return;
        }
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence: geofenceList){
            Log.d("yes", "onRecieve: " + geofence.getRequestId());
        }
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch(transitionType){
            case Geofence.GEOFENCE_TRANSITION_ENTER:

                adventureMode.setValue("off");

                //Toast.makeText(context, "GEOFENCE ENTER", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Welcome back from your adventure!", "Happy to know you are safe, disabling beacon.", MainActivity.class);

                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:

                //
                adventureMode.setValue("on");
                WorkRequest uploadWorkRequest =
                        new OneTimeWorkRequest.Builder(BackGroundLocationWorker.class)
                                .build();
                WorkManager
                        .getInstance(context)
                        .enqueue(uploadWorkRequest);

                //Intent backgroundloc = new Intent(context,BackgroundLocationService.class);
                //context.startService(backgroundloc);
                //sendLocation(context);

                //setAlarms(context);
                //setFireBaseAlarmTime();
                //Toast.makeText(context, "GEOFENCE EXIT", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Adventure mode is set to on!", "Your location will be sent if not updated within 24hrs!", MainActivity.class);

                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                //Toast.makeText(context, "GEOFENCE DWELL", Toast.LENGTH_SHORT).show();
                break;
        }
    }



}