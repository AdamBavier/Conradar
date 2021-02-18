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

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.ArrayList;

import edu.coloradomesa.cs.conradar.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    ArrayList<Contact> contacts = new ArrayList<>();

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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        EditText customMessage = (EditText)findViewById(R.id.custom_message_text);
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.default_message_radio_select:
                if (checked)
                    customMessage.setVisibility(View.GONE);
                    System.out.println("default");
                    break;
            case R.id.custom_message_radio_select:
                if (checked)
                    System.out.println("custom");
                    customMessage.setVisibility(View.VISIBLE);
                    break;
        }
    }

    public void onClick(View view) {
        EditText firstName, lastName, tempEmail, tempMessage = null;
        firstName = (EditText) findViewById(R.id.emergency_contact_firstname);
        String fName = firstName.getText().toString();
        lastName = (EditText) findViewById(R.id.emergency_contact_lastname);
        String lName = lastName.getText().toString();
        tempEmail = (EditText) findViewById(R.id.emergency_contact_email_address);
        String email = tempEmail.getText().toString();
        Contact c = new Contact(fName, lName, email);
        contacts.add(c);
        c.show1();
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
}