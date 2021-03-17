package edu.coloradomesa.cs.conradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Toast.makeText(context, "Geofence Triggered", Toast.LENGTH_SHORT).show();
        NotificationHelper notificationHelper = new NotificationHelper(context);

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
                Toast.makeText(context, "GEOFENCE ENTER", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Welcome back from your adventure!", "Happy to know you are safe, disabling beacon.", MainActivity.class);

                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "GEOFENCE EXIT", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Going for an adventure?", "Let us know when you should be back!", MainActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "GEOFENCE DWELL", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}