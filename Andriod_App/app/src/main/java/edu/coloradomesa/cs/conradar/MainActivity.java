package edu.coloradomesa.cs.conradar;

import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.coloradomesa.cs.conradar.ui.main.MapFragment;
import edu.coloradomesa.cs.conradar.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private int timeOut = 24;

    ArrayList<Contact> contacts = new ArrayList<>();
    List<String> namesList = new ArrayList<>();

    ViewPager viewPager;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private GeofencingClient geofencingClient;
    private GeoFenceHelper geoFenceHelper;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";

    FirebaseDBHelper dbHelper = new FirebaseDBHelper();
    FirebaseDatabase db = dbHelper.getDB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);
        namesList.add("Select Contact");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        checkPermissions();
        geofencingClient = LocationServices.getGeofencingClient(this);
        geoFenceHelper = new GeoFenceHelper(this);
    }

    public void onMessageRadioGroupClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        EditText customMessage = (EditText) findViewById(R.id.custom_message_text);
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.default_message_radio_select:
                if (checked)
                    customMessage.setVisibility(View.GONE);
                break;
            case R.id.custom_message_radio_select:
                if (checked)
                    customMessage.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onAddEditRadioGroupClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        ScrollView addContacts = (ScrollView) findViewById(R.id.add_contact_scroll_view);
        ScrollView editContacts = (ScrollView) findViewById(R.id.edit_contacts_scrollview);
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.add_contact_radio:
                if (checked)
                    System.out.println("HERE");
                addContacts.setVisibility(View.VISIBLE);
                editContacts.setVisibility(View.GONE);
                break;
            case R.id.edit_contact_radio:
                if (checked)
                    System.out.println("BEER");
                addContacts.setVisibility(View.GONE);
                editContacts.setVisibility(View.VISIBLE);
                Spinner s = (Spinner) findViewById(R.id.contact_list_spinner);
                s.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, namesList));
                break;
        }
    }

    public void addContact(View view) {
        EditText firstName, lastName, tempEmail, tempMessage = null;
        firstName = (EditText) findViewById(R.id.emergency_contact_firstname);
        String fName = firstName.getText().toString();
        lastName = (EditText) findViewById(R.id.emergency_contact_lastname);
        String lName = lastName.getText().toString();
        tempEmail = (EditText) findViewById(R.id.emergency_contact_email_address);
        String email = tempEmail.getText().toString();
        Contact c = new Contact(fName, lName, email);
        String name = fName + " " + lName;
        namesList.add(name);
        contacts.add(c);
        firstName.setText("");
        lastName.setText("");
        tempEmail.setText("");
        //if(R.id.custom_message_radio_select)
    }

    public void increaseTime(View view) {
        TextView adventureLength = (TextView) findViewById(R.id.adventure_length);
        TextView dayVsHour = (TextView) findViewById(R.id.day_hour_tag);
        if (timeOut < 24) timeOut++;
        else timeOut += 24;
        if (timeOut >= 24) {
            String strTime = String.valueOf(timeOut / 24);
            if (timeOut / 24 == 1) dayVsHour.setText("Day");
            else dayVsHour.setText("Days");
            adventureLength.setText(strTime);
        } else {
            adventureLength.setText(Integer.toString(timeOut));
            if (timeOut == 1) dayVsHour.setText("Hour");
            else dayVsHour.setText("Hours");
        }
    }

    public void decreaseTime(View view) {
        TextView adventureLength = (TextView) findViewById(R.id.adventure_length);
        TextView dayVsHour = (TextView) findViewById(R.id.day_hour_tag);
        if (timeOut == 0) ;
        else if (timeOut <= 24) timeOut--;
        else timeOut -= 24;
        if (timeOut >= 24) {
            String strTime = String.valueOf(timeOut / 24);
            if (timeOut / 24 == 1) dayVsHour.setText("Day");
            else dayVsHour.setText("Days");
            adventureLength.setText(strTime);
        } else {
            adventureLength.setText(Integer.toString(timeOut));
            if (timeOut == 1) dayVsHour.setText("Hour");
            else dayVsHour.setText("Hours");
        }
    }

    public void updateInformation(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String parent = "Parent";
        String child = "Parent/child";
        DatabaseReference myRef = database.getReference(parent);
        myRef.setValue("parent Was Here");
        DatabaseReference children = database.getReference(child);
        children.setValue("Child is here");
    }


    public void checkPermissions(){
        if (ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,  new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            // permission granted
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    List<Geofence> geofenceList;

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    public void addGeoFence(LatLng latLng, float radius) {
        Geofence geofence = geoFenceHelper.getGeoFence(GEOFENCE_ID, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        PendingIntent pendingIntent = geoFenceHelper.getPendingIntent();
        GeofencingRequest geofencingRequest = geoFenceHelper.getGeofencingRequest(geofence);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("yes", "geofencesuccess");


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errormessage =  geoFenceHelper.getErrorString(e);
                        Log.d("yes", errormessage);
                        return;
                    }
                });
        DatabaseReference geofenceLat = db.getReference(dbHelper.getRootSTR() + "GeofenceLat");
        DatabaseReference geofenceLng = db.getReference(dbHelper.getRootSTR() + "GeofenceLng");

        DatabaseReference geofenceRad = db.getReference(dbHelper.getRootSTR() + "GeofenceRad");

        geofenceLat.setValue(latLng.latitude);
        geofenceLng.setValue(latLng.longitude);
        geofenceRad.setValue(radius);
    }

    }
