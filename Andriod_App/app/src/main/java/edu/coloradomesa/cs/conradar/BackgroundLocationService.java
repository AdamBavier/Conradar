package edu.coloradomesa.cs.conradar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;
import android.os.strictmode.CredentialProtectedWhileLockedViolation;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class BackgroundLocationService extends IntentService {
    String TAG = BackgroundLocationService.class.getSimpleName();
    public BackgroundLocationService(){
        super("BackgroundLocationService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String dataString = intent.getDataString();
        Log.d(BackgroundLocationService.class.getSimpleName(), "Service Activated");
        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(BackGroundLocationWorker.class)
                        .build();
        WorkManager
                .getInstance(this)
                .enqueue(uploadWorkRequest);

        stopSelf();
    }
    public void setAlarms(Context context){

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent backgroundloc = new Intent(context,BackgroundLocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, backgroundloc, 0);
        if (pendingIntent != null && alarmManager != null){
            alarmManager.cancel(pendingIntent);
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                1000 * 60 * 60,
                pendingIntent);

        //This only ran once for whatever reason
        /*
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR, pendingIntent);
                */

        context.startService(backgroundloc);
    }
    public void sendLocFirebase(LatLng latLng){
        FirebaseDBHelper dbHelper = new FirebaseDBHelper();
        FirebaseDatabase db = dbHelper.getDB();

        DatabaseReference currentloc = db.getReference(dbHelper.getRootSTR() + "CurrentLoc");
        currentloc.setValue(latLng);

        //Set Alert Time to 24 hours from now
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        cal.add(Calendar.HOUR, 24);
        Date currentlocaltime = cal.getTime();
        DateFormat date = new SimpleDateFormat("yyyy:MM:dd:HH:mm a");
        date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
        String timetoalert = date.format(currentlocaltime);
        DatabaseReference currenttimeref = db.getReference(dbHelper.getRootSTR() + "Time to Alert");
        currenttimeref.setValue(timetoalert);

        Log.d("ye", "Location Updated");
    }

    @SuppressLint("MissingPermission")
    public void getLocation(){
        final Location location;
        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location last_known_location = task.getResult();

                 LatLng latLng = new LatLng(last_known_location.getLatitude(),
                         last_known_location.getLongitude());
                 sendLocFirebase(latLng);

            }
        });
    }
}
