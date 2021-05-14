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
        //Alarms cannot be set in the background like this, so this function is useless
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
        DateFormat date = new SimpleDateFormat("yyyy:MM:dd:HH:mm a");
        date.setTimeZone(TimeZone.getTimeZone("America/Denver"));

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Denver"));
        DatabaseReference curTimeRef = db.getReference(dbHelper.getRootSTR() + "lastLocTime");
        curTimeRef.setValue(date.format(cal.getTime()));

        cal.add(Calendar.HOUR, 24);
        Date currentlocaltime = cal.getTime();

        String timetoalert = date.format(currentlocaltime);
        DatabaseReference timeToAlertRef = db.getReference(dbHelper.getRootSTR() + "Time to Alert");
        timeToAlertRef.setValue(timetoalert);

        Log.d("ye", "Location Updated");
    }


}
