package edu.coloradomesa.cs.conradar;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.coloradomesa.cs.conradar.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private int timeOut = 24;

    ArrayList<Contact> contacts = new ArrayList<>();
    List<String> namesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
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
    }

    public void onMessageRadioGroupClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        EditText customMessage = (EditText)findViewById(R.id.custom_message_text);
        // Check which radio button was clicked
        switch(view.getId()) {
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
        switch(view.getId()) {
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void increaseTime(View view) {
        TextView adventureLength = (TextView)findViewById(R.id.adventure_length);
        TextView dayVsHour = (TextView)findViewById(R.id.day_hour_tag);
        if(timeOut < 24) timeOut++;
        else timeOut += 24;
        if(timeOut >= 24) {
            String strTime = String.valueOf(timeOut / 24);
            if(timeOut / 24 == 1) dayVsHour.setText("Day");
            else dayVsHour.setText("Days");
            adventureLength.setText(strTime);
        }
        else {
            adventureLength.setText(Integer.toString(timeOut));
            if(timeOut == 1) dayVsHour.setText("Hour");
            else dayVsHour.setText("Hours");
        }
    }

    public void decreaseTime(View view) {
        TextView adventureLength = (TextView)findViewById(R.id.adventure_length);
        TextView dayVsHour = (TextView)findViewById(R.id.day_hour_tag);
        if(timeOut == 0);
        else if(timeOut <= 24) timeOut--;
        else timeOut -= 24;
        if(timeOut >= 24) {
            String strTime = String.valueOf(timeOut / 24);
            if(timeOut / 24 == 1) dayVsHour.setText("Day");
            else dayVsHour.setText("Days");
            adventureLength.setText(strTime);
        }
        else {
            adventureLength.setText(Integer.toString(timeOut));
            if(timeOut == 1) dayVsHour.setText("Hour");
            else dayVsHour.setText("Hours");
        }
    }

    public void updateInformation(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        
        myRef.setValue("New message");
    }
}