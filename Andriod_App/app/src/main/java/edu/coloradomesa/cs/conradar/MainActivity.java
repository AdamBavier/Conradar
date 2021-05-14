package edu.coloradomesa.cs.conradar;

import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.timessquare.CalendarPickerView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.AdapterView.OnItemSelectedListener;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.coloradomesa.cs.conradar.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    // List of names for the spinner on the edit contacts page
    ArrayList<String> namesList = new ArrayList<String>();
    // List of all contacts
    ArrayList<Contact> contacts = new ArrayList<>();
    // For traversing and keeping track of what needs to be edited
    Contact currContact = null;
    // Days of the week that the app will/ will not be active (false is off, true is on)
    Boolean[] weeklyOutingDays = {false, false, false, false, false, false, false};
    String currName = "";
    // List of dates for up coming trips
    List<Date> tripDates = new ArrayList<>();
    // Default hours before a message is sent
    private int timeOut = 24;
    // Default message to be sent to contacts
    private String defaultMessage = "Dear *Contact*,\n \nThis is an automated message informing you that *your name* began a trip on *start date* and has not returned. The recent location information for *your name* can be found here.";

    // Firebase instance
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    ViewPager viewPager;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private GeofencingClient geofencingClient;
    private GeoFenceHelper geoFenceHelper;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";

    FirebaseDBHelper dbHelper = new FirebaseDBHelper();
    FirebaseDatabase db = dbHelper.getDB();

    // Some booleans to help with the display of the app
    private boolean showingDefaultMessage = false;
    private boolean showingAdventureLength = false;
    private boolean showingAdventureDays = false;
    private boolean showingAdventureCalendar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        namesList.add("Select Contact");
        loadData();
        checkPermissions();
        geofencingClient = LocationServices.getGeofencingClient(this);
        geoFenceHelper = new GeoFenceHelper(this);
    }

    // This function will read the data stored in Firebase and populate the local data with what is loaded
    // This function will also store the default values into Firebase if there are no current values inside
    // of the database
    public void loadData() {
        mDatabase.child("Mike/UniqueID/Contacts/Count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()) {
                    Log.e("firebase", "Error", task.getException());
                }
                else {
                    String contactStr = "Mike/UniqueID/Contacts/Contact0";
                    if (!String.valueOf(task.getResult().getValue()).equals("null")) {
                        for (int i = 0; i < Integer.parseInt(task.getResult().getValue().toString()); i++) {
                            contactStr = contactStr.substring(0, contactStr.length() - 1);
                            contactStr += Integer.toString(i + 1);
                            mDatabase.child(contactStr).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e("firebase", "Error", task.getException());
                                    } else {
                                        String information = String.valueOf(task.getResult().getValue());
                                        int emailIndex, messageIndex, fNameIndex, lNameIndex, cellIndex;
                                        String email, message, fName, lName, cell, fullName;
                                        boolean hasCell;
                                        emailIndex = information.indexOf("Email=");
                                        messageIndex = information.indexOf("Message=");
                                        fNameIndex = information.indexOf("FirstName=");
                                        lNameIndex = information.indexOf("LastName=");
                                        cellIndex = information.indexOf("Cell=");
                                        email = information.substring(emailIndex + 6, messageIndex - 2);
                                        message = information.substring(messageIndex + 8, fNameIndex - 2);
                                        fName = information.substring(fNameIndex + 10, lNameIndex - 2);
                                        lName = information.substring(lNameIndex + 9, cellIndex - 2);
                                        cell = information.substring(cellIndex + 5, cellIndex + 6);
                                        if(cell.equals("T")) hasCell = true;
                                        else hasCell = false;
                                        Contact c = new Contact(fName, lName, email, message, hasCell);
                                        fullName = fName + " " + lName;
                                        namesList.add(fullName);
                                        contacts.add(c);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
        mDatabase.child("Mike/UniqueID/Preferences/daysOut").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()) {
                    Log.e("firebase", "Error", task.getException());
                }
                else {
                    String daysOut = String.valueOf(task.getResult().getValue());
                    if(!daysOut.equals("null")) {
                        int index = daysOut.indexOf("Sunday=");
                        if (daysOut.substring(index + 7, index + 8).equals("f"))
                            weeklyOutingDays[0] = false;
                        else weeklyOutingDays[0] = true;
                        index = daysOut.indexOf("Monday=");
                        if (daysOut.substring(index + 7, index + 8).equals("f"))
                            weeklyOutingDays[1] = false;
                        else weeklyOutingDays[1] = true;
                        index = daysOut.indexOf("Tuesday=");
                        if (daysOut.substring(index + 8, index + 9).equals("f"))
                            weeklyOutingDays[2] = false;
                        else weeklyOutingDays[2] = true;
                        index = daysOut.indexOf("Wednesday=");
                        if (daysOut.substring(index + 10, index + 11).equals("f"))
                            weeklyOutingDays[3] = false;
                        else weeklyOutingDays[3] = true;
                        index = daysOut.indexOf("Thursday=");
                        if (daysOut.substring(index + 9, index + 10).equals("f"))
                            weeklyOutingDays[4] = false;
                        else weeklyOutingDays[4] = true;
                        index = daysOut.indexOf("Friday=");
                        if (daysOut.substring(index + 7, index + 8).equals("f"))
                            weeklyOutingDays[5] = false;
                        else weeklyOutingDays[5] = true;
                        index = daysOut.indexOf("Saturday=");
                        if (daysOut.substring(index + 9, index + 10).equals("f"))
                            weeklyOutingDays[6] = false;
                        else weeklyOutingDays[6] = true;
                    }
                    else {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        String path = "Mike/UniqueID/Preferences/daysOut";
                        DatabaseReference [] fireBase = {database.getReference(path + "/Sunday"), database.getReference(path + "/Monday"), database.getReference(path + "/Tuesday"),
                                database.getReference(path + "/Wednesday"), database.getReference(path + "/Thursday"), database.getReference(path + "/Friday"),
                                database.getReference(path + "/Saturday")};
                        for(int i = 0; i < 7; i ++) {
                            fireBase[i].setValue(false);
                        }
                    }
                }
            }
        });

        mDatabase.child("Mike/UniqueID/Preferences/defaultMessage").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()) {
                    Log.e("firebase", "Error", task.getException());
                }
                else {
                    if(!String.valueOf(task.getResult().getValue()).equals("null"))
                        defaultMessage = String.valueOf(task.getResult().getValue());
                    else {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference fireBase = database.getReference("Mike/UniqueID/Preferences/defaultMessage");
                        fireBase.setValue(defaultMessage);
                    }
                }
            }
        });

        mDatabase.child("Mike/UniqueID/Preferences/timeoutLength").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()) {
                    Log.e("firebase", "Error", task.getException());
                }
                else {
                    if(!String.valueOf(task.getResult().getValue()).equals("null"))
                        timeOut = Integer.parseInt(String.valueOf(task.getResult().getValue()));
                    else {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference fireBase = database.getReference("Mike/UniqueID/Preferences/timeoutLength");
                        fireBase.setValue(String.valueOf(timeOut));
                    }
                }
            }
        });

        mDatabase.child("Mike/UniqueID/Preferences/tripDates/startDate").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()) {
                    Log.e("firebase", "Error", task.getException());
                }
                else {
                    String end = String.valueOf(task.getResult().getValue());
                    try {
                        Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(end);
                        tripDates.add(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mDatabase.child("Mike/UniqueID/Preferences/tripDates/endDate").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()) {
                    Log.e("firebase", "Error", task.getException());
                }
                else {
                    String end = String.valueOf(task.getResult().getValue());
                    try {
                        Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(end);
                        tripDates.add(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // A method to toggle between viewing the custom message box and not viewing the message box
    // in the add contact screen
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
                    customMessage.setHint(defaultMessage);
                    break;
        }
    }

    // A method to toggle between viewing the custom message box and not viewing the message box
    // in the edit contact screen
    public void onEditContactMessageRadioGroupClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        EditText customMessage = (EditText)findViewById(R.id.edit_contact_custom_message_box);
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.edit_contact_default_message:
                if (checked)
                    customMessage.setVisibility(View.GONE);
                break;
            case R.id.edit_contact_custom_message:
                if (checked)
                    customMessage.setVisibility(View.VISIBLE);
                    customMessage.setHint(defaultMessage);
                break;
        }
    }

    // A method to increase the time out duration
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

    // A method to decrease the time out duration
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

    // A method to toggle between viewing the add and edit contact pages
    // This function will also populate the spinner located on the edit contact page
    public void onAddEditRadioGroupClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        ScrollView addContacts = (ScrollView) findViewById(R.id.add_contact_scroll_view);
        ScrollView editContacts = (ScrollView) findViewById(R.id.edit_contacts_scrollview);
        RadioButton addContactsRadio = (RadioButton) findViewById(R.id.default_message_radio_select);
        EditText addCustomMessage = (EditText) findViewById(R.id.custom_message_text);
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.add_contact_radio:
                if (checked)
                    addContactsRadio.setChecked(true);
                    addCustomMessage.setVisibility(View.GONE);
                    addContacts.setVisibility(View.VISIBLE);
                    editContacts.setVisibility(View.GONE);
                break;
            case R.id.edit_contact_radio:
                if (checked)
                    addContacts.setVisibility(View.GONE);
                    editContacts.setVisibility(View.VISIBLE);
                    final EditText editFirstName;
                final EditText editLastName;
                final EditText editEmail;
                final EditText editMessage;
                editFirstName = (EditText) findViewById(R.id.edit_contact_first_name);
                    editLastName = (EditText) findViewById(R.id.edit_contact_last_name);
                    editEmail = (EditText) findViewById(R.id.edit_contact_email_address);
                    editMessage = (EditText) findViewById(R.id.edit_contact_custom_message_box);
                    Spinner s = (Spinner) findViewById(R.id.contact_list_spinner);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_dropdown_item, namesList);
                    s.setAdapter(adapter);
                    s.setOnItemSelectedListener(new OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            String[] findName = adapterView.getItemAtPosition(i).toString().split(" ");
                            boolean found = false;
                            int j;
                            for(j = 0; j < contacts.size(); j++) {
                                if(contacts.get(j).getFirstName().equals(findName[0])) {
                                    currContact = contacts.get(j);
                                    currName = adapterView.getItemAtPosition(i).toString();
                                    found = true;
                                    break;
                                }
                            }
                            RadioButton btn1 = (RadioButton) findViewById(R.id.edit_contact_default_message);
                            RadioButton btn2 = (RadioButton) findViewById(R.id.edit_contact_custom_message);
                            RadioButton emailRadio = (RadioButton) findViewById(R.id.edit_email_radio);
                            RadioButton phoneRadio = (RadioButton) findViewById(R.id.edit_phoneNumber_radio);
                            Spinner carrierSpinner = (Spinner) findViewById(R.id.edit_phoneProviderSpinner);
                            EditText customMessage = (EditText)findViewById(R.id.edit_contact_custom_message_box);
                            if(found) {
                                editFirstName.setText(contacts.get(j).getFirstName());
                                editLastName.setText(contacts.get(j).getLastName());
                                editEmail.setText(contacts.get(j).getEmail());
                                if(contacts.get(j).isCellPhone()) {
                                    carrierSpinner.setVisibility(View.VISIBLE);
                                    phoneRadio.setChecked(true);
                                }
                                else {
                                    carrierSpinner.setVisibility(View.GONE);
                                    emailRadio.setChecked(true);
                                }

                                if (contacts.get(j).getMessage().equals(defaultMessage)) {
                                    btn1.setChecked(true);
                                    customMessage.setVisibility(View.GONE);
                                }
                                else {
                                    btn2.setChecked(true);
                                    editMessage.setText(contacts.get(j).getMessage());
                                    customMessage.setVisibility(View.VISIBLE);
                                }
                            }
                            else {
                                btn1.setChecked(true);
                                customMessage.setVisibility(View.GONE);
                                editFirstName.setText("");
                                editLastName.setText("");
                                editEmail.setText("");
                                editMessage.setText("");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                break;
        }
    }

    // The function connected to the button on the edit contact page, this will save changes
    // locally and update Firebase to reflect the changes
    public void buttonEditContact(View view) {
        EditText firstName, lastName, tempEmail;
        firstName = (EditText) findViewById(R.id.edit_contact_first_name);
        String fName = firstName.getText().toString();
        lastName = (EditText) findViewById(R.id.edit_contact_last_name);
        String lName = lastName.getText().toString();
        tempEmail = (EditText) findViewById(R.id.edit_contact_email_address);
        String email = tempEmail.getText().toString();
        String fbEmail = email;
        RadioButton btn = (RadioButton) findViewById(R.id.edit_contact_default_message);
        RadioButton btn2 = (RadioButton) findViewById(R.id.edit_email_radio);
        Spinner spinner = (Spinner) findViewById(R.id.edit_phoneProviderSpinner);
        EditText customMessage = (EditText)findViewById(R.id.edit_contact_custom_message_box);
        String message = customMessage.getText().toString();
        boolean hasCell = false;
        // Check which radio button was clicked
        if(fName.equals("") || lName.equals("") || email.equals("")) {
            Snackbar.make(view, "Please provide all information!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            if (!email.equals("")) {
                if (btn.isChecked()) message = defaultMessage;
                if (!btn2.isChecked()) {
                    hasCell = true;
                    String carrier = spinner.getSelectedItem().toString();
                    switch (carrier) {
                        case ("Verizon"):
                            fbEmail += "@vtext.com";
                            break;
                        case("Sprint"):
                            fbEmail += "@messaging.sprintpcs.com";
                            break;
                        case("AT&T"):
                            fbEmail += "@txt.att.net";
                            break;
                        case("T-Mobile"):
                            fbEmail += "@tmomail.net";
                            break;
                    }
                }
                currContact.setFirstName(fName);
                currContact.setLastName(lName);
                currContact.setEmail(email);
                currContact.setMessage(message);
                currContact.setCellPhone(hasCell);
                String name = fName + " " + lName;
                int index = namesList.indexOf(currName);
                namesList.set(index, name);
                firstName.setText("");
                lastName.setText("");
                tempEmail.setText("");
                customMessage.setText("");
                btn.setChecked(true);
                btn2.setChecked(true);
                customMessage.setVisibility(View.GONE);
                Spinner s = (Spinner) findViewById(R.id.contact_list_spinner);
                s.setSelection(0);
                Snackbar.make(view, "Contact Updated!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String contactNo = String.valueOf(contacts.indexOf(currContact) + 1);
                String emailRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/Email";
                String fNameRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/FirstName";
                String lNameRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/LastName";
                String messageRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/Message";
                String cellRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/Cell";
                DatabaseReference fireBaseFname = database.getReference(fNameRef);
                fireBaseFname.setValue(fName);
                DatabaseReference fireBaseLname = database.getReference(lNameRef);
                fireBaseLname.setValue(lName);
                DatabaseReference fireBaseEmail = database.getReference(emailRef);
                fireBaseEmail.setValue(fbEmail);
                DatabaseReference fireBaseMessage = database.getReference(messageRef);
                fireBaseMessage.setValue(message);
                String cell = "False";
                if(hasCell) cell = "True";
                DatabaseReference fireBaseCell = database.getReference(cellRef);
                fireBaseCell.setValue(cell);
                spinner.setVisibility(View.GONE);
            } else {
                Snackbar.make(view, "No contact selected", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    // The function connected to the delete contact button on the edit contact page
    // This will delete the local instances of the contact and update the
    // Firebase instances as well
    public void deleteContact(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        EditText email = (EditText) findViewById(R.id.edit_contact_email_address);
        String contactNo = "Mike/UniqueID/Contacts/Contact";

        int i = 0;

        if(!email.getText().toString().equals("")) {
            for (i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).getEmail().equals(email.getText().toString())) {
                    break;
                }
            }

            String demail = "";
            String dmessage = "";
            String dfName = "";
            String dlName = "";
            String cell = "";

            for(int j = i + 1; j < contacts.size(); j++) {
                demail = contactNo + j + "/Email";
                dmessage = contactNo + j + "/Message";
                dfName = contactNo + j + "/FirstName";
                dlName = contactNo + j + "/LastName";
                cell = contactNo + j + "/Cell";
                DatabaseReference fireBaseEmail = database.getReference(demail);
                fireBaseEmail.setValue(contacts.get(j).getEmail());
                DatabaseReference fireBaseMessage = database.getReference(dmessage);
                fireBaseMessage.setValue(contacts.get(j).getMessage());
                DatabaseReference fireBaseFname = database.getReference(dfName);
                fireBaseFname.setValue(contacts.get(j).getFirstName());
                DatabaseReference fireBaseLname = database.getReference(dlName);
                fireBaseLname.setValue(contacts.get(j).getLastName());
                DatabaseReference fireBaseCell = database.getReference(cell);
                if(contacts.get(j).isCellPhone()) {
                    fireBaseCell.setValue("True");
                }
                else fireBaseCell.setValue("False");
            }

            contactNo = "Mike/UniqueID/Contacts/Contact" + (contacts.size());
            DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference().child(contactNo);
            mPostReference.removeValue();

            contacts.remove(i);
            namesList.remove(i + 1);

            DatabaseReference fireBaseContactsCount = database.getReference("Mike/UniqueID/Contacts/Count");
            fireBaseContactsCount.setValue(contacts.size());

            EditText firstName = (EditText) findViewById(R.id.edit_contact_first_name);
            firstName.setText("");
            EditText lastName = (EditText) findViewById(R.id.edit_contact_last_name);
            lastName.setText("");
            email.setText("");
            EditText customMessage = (EditText) findViewById(R.id.edit_contact_custom_message_box);
            customMessage.setText("");
            RadioButton btn = (RadioButton) findViewById(R.id.edit_contact_default_message);
            btn.setChecked(true);
            customMessage.setVisibility(View.GONE);
            Spinner s = (Spinner) findViewById(R.id.contact_list_spinner);
            s.setSelection(0);
            Snackbar.make(view, "Contact Deleted!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            Snackbar.make(view, "No contact selected", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    // The function connected to the add contact button on the add contact page, his will
    // save changes locally and update Firebase to reflect the changes
    public void addContact(View view) {
        EditText firstName, lastName, tempEmail;
        firstName = (EditText) findViewById(R.id.emergency_contact_firstname);
        String fName = firstName.getText().toString();
        lastName = (EditText) findViewById(R.id.emergency_contact_lastname);
        String lName = lastName.getText().toString();
        tempEmail = (EditText) findViewById(R.id.emergency_contact_email_address);
        String email = tempEmail.getText().toString();
        String fbEmail = email;
        RadioButton btn = (RadioButton) findViewById(R.id.default_message_radio_select);
        RadioButton btn2 = (RadioButton) findViewById(R.id.email_radio_select);
        EditText customMessage = (EditText)findViewById(R.id.custom_message_text);
        String message = customMessage.getText().toString();
        Spinner spinner = (Spinner) findViewById(R.id.phoneProviderSpinner);
        boolean hasCell = false;
        Contact c = null;
        if(fName.equals("") || lName.equals("") || email.equals("")) {
            Snackbar.make(view, "Please provide all information!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            // Check which radio button was clicked
            if (btn.isChecked()) message = defaultMessage;
            if (!btn2.isChecked()) {
                hasCell = true;
                String carier = spinner.getSelectedItem().toString();
                switch (carier) {
                    case ("Verizon"):
                        fbEmail += "@vtext.com";
                        break;
                    case("Sprint"):
                        fbEmail += "@messaging.sprintpcs.com";
                        break;
                    case("AT&T"):
                        fbEmail += "@txt.att.net";
                        break;
                    case("T-Mobile"):
                        fbEmail += "@tmomail.net";
                        break;
                }
            }
            c = new Contact(fName, lName, email, message, hasCell);
            String name = fName + " " + lName;
            namesList.add(name);
            contacts.add(c);
            firstName.setText("");
            lastName.setText("");
            tempEmail.setText("");
            customMessage.setText("");
            btn.setChecked(true);
            customMessage.setVisibility(View.GONE);
            Snackbar.make(view, "Contact Added!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String contactNo = String.valueOf(contacts.size());
            String emailRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/Email";
            String fNameRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/FirstName";
            String lNameRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/LastName";
            String messageRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/Message";
            String cellRef = "Mike/UniqueID/Contacts/Contact" + contactNo + "/Cell";
            DatabaseReference fireBaseContactsCount = database.getReference("Mike/UniqueID/Contacts/Count");
            fireBaseContactsCount.setValue(contactNo);
            DatabaseReference fireBaseFname = database.getReference(fNameRef);
            fireBaseFname.setValue(fName);
            DatabaseReference fireBaseLname = database.getReference(lNameRef);
            fireBaseLname.setValue(lName);
            DatabaseReference fireBaseEmail = database.getReference(emailRef);
            fireBaseEmail.setValue(fbEmail);
            DatabaseReference fireBaseMessage = database.getReference(messageRef);
            fireBaseMessage.setValue(message);
            String usingEmail = "True";
            if(!btn2.isChecked()) usingEmail = "False";
            DatabaseReference fireBaseCell = database.getReference(cellRef);
            fireBaseCell.setValue(usingEmail);
            btn2.setChecked(true);
            spinner.setVisibility(View.GONE);
        }
    }

    // On the add contact page, this will toggle between showing the phone provider spinner
    // or not and change the Hint property for the fillable text box
    public void phoneVSemail(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.phoneProviderSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.phoneProviders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        EditText inputHint = (EditText) findViewById(R.id.emergency_contact_email_address);

        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.phone_radio_select:
                if (checked) {
                    spinner.setVisibility(View.VISIBLE);
                    inputHint.setHint("Using format: 7205551234");
                }
                break;
            case R.id.email_radio_select:
                if (checked) {
                    spinner.setVisibility(View.GONE);
                    inputHint.setHint("example@domain.com");
                }
                break;
        }
    }

    // On the edit contact page, this will toggle between showing the phone provider spinner
    // or not and change the Hint property for the fillable text box
    public void editPhoneVSemail(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.edit_phoneProviderSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.phoneProviders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        EditText inputHint = (EditText) findViewById(R.id.edit_contact_email_address);

        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.edit_phoneNumber_radio:
                if (checked) {
                    spinner.setVisibility(View.VISIBLE);
                    inputHint.setHint("Using format: 7205551234");
                }
                break;
            case R.id.edit_email_radio:
                if (checked) {
                    spinner.setVisibility(View.GONE);
                    inputHint.setHint("example@domain.com");
                }
                break;
        }
    }

    // A button on the preferences page, if the boolean value is true, show the message box
    // if not hide it. This toggles the view
    public void preferencesShowDefaultMessage(View view) {
        EditText defaultMessageBlock = (EditText) findViewById(R.id.preferences_set_default_message);
        LinearLayout defaultMessageLinearLayout = (LinearLayout) findViewById(R.id.preferences_LinerarLayout_DefaultMessage);
        if(showingDefaultMessage) {
            showingDefaultMessage = false;
            defaultMessageLinearLayout.setVisibility(View.GONE);
        }
        else {
            showingDefaultMessage = true;
            defaultMessageLinearLayout.setVisibility(View.VISIBLE);
            defaultMessageBlock.setHint(defaultMessage);
        }
    }

    // A button on the preferences page, if the boolean value is true, show the content
    // if not hide it. This toggles the view
    public void preferencesShowAdventureLength(View view) {
        LinearLayout adventureLength = (LinearLayout) findViewById(R.id.preferences_linearlayout_adventurelength);
        TextView time = (TextView) findViewById(R.id.adventure_length);
        TextView dayVsHour = (TextView) findViewById(R.id.day_hour_tag);
        if(showingAdventureLength) {
            showingAdventureLength = false;
            adventureLength.setVisibility(View.GONE);
        }
        else {
            showingAdventureLength = true;
            adventureLength.setVisibility(View.VISIBLE);
            if(timeOut >= 24) {
                String strTime = String.valueOf(timeOut / 24);
                if(timeOut / 24 == 1) dayVsHour.setText("Day");
                else dayVsHour.setText("Days");
                time.setText(strTime);
            }
            else {
                time.setText(Integer.toString(timeOut));
                if(timeOut == 1) dayVsHour.setText("Hour");
                else dayVsHour.setText("Hours");
            }
        }
    }

    // A button on the preferences page, if the boolean value is true, show the content
    // if not hide it. This toggles the view
    public void preferencesShowAdventureDays(View view) {
        LinearLayout adventureDays = (LinearLayout) findViewById(R.id.preferences_linearlayout_adventuredays);
        Chip[] days = {findViewById(R.id.Sunday_chip), findViewById(R.id.Monday_chip), findViewById(R.id.Tuesday_chip),
                findViewById(R.id.Wednesday_chip), findViewById(R.id.Thursday_chip), findViewById(R.id.Friday_chip), findViewById(R.id.Saturday_chip)};
        if(showingAdventureDays) {
            showingAdventureDays = false;
            adventureDays.setVisibility(View.GONE);
        }
        else {
            showingAdventureDays = true;
            for(int i = 0; i < 7; i++) {
                days[i].setChecked(weeklyOutingDays[i]);
            }
            adventureDays.setVisibility(View.VISIBLE);
        }
    }

    // A button on the preferences page, if the boolean value is true, show the content
    // if not hide it. This toggles the view
    public void preferencesShowCalendar(View view) {
        ConstraintLayout adventureCalendar = (ConstraintLayout) findViewById(R.id.preferences_constraintlayout_calendar);
        if(showingAdventureCalendar) {
            showingAdventureCalendar = false;
            adventureCalendar.setVisibility(View.GONE);
        }
        else {
            showingAdventureCalendar = true;
            adventureCalendar.setVisibility(View.VISIBLE);
            Date today = new Date();
            Calendar nextYear = Calendar.getInstance();
            nextYear.add(Calendar.YEAR, 1);
            CalendarPickerView datePicker = findViewById(R.id.calendar_view);
            if(tripDates.isEmpty()) {
                datePicker.init(today, nextYear.getTime())
                        .inMode(CalendarPickerView.SelectionMode.RANGE)
                        .withSelectedDate(today);
            }
            else {
                List<Date> temp = new ArrayList<>();
                temp.add(tripDates.get(0));
                temp.add(tripDates.get(tripDates.size() - 1));
                datePicker.init(tripDates.get(0), nextYear.getTime())
                        .inMode(CalendarPickerView.SelectionMode.RANGE)
                        .withSelectedDates(temp);
            }
            datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
                @Override
                public void onDateSelected(Date date) {

                }

                @Override
                public void onDateUnselected(Date date) {
                }
            });
        }
    }

    // This will take all the information that is updated on the preferences page and update the
    // values locally and on Firebase. It checks which views are available and will only
    // update the ones that are currently visable.
    public void updateInformation(View view) {
        EditText newDefault = (EditText) findViewById(R.id.preferences_set_default_message);
        TextView newLength = (TextView) findViewById(R.id.adventure_length);
        TextView dayVsHour = (TextView) findViewById(R.id.day_hour_tag);
        LinearLayout defaultMessageLinearLayout = (LinearLayout) findViewById(R.id.preferences_LinerarLayout_DefaultMessage);
        LinearLayout adventureLength = (LinearLayout) findViewById(R.id.preferences_linearlayout_adventurelength);
        LinearLayout adventureDays = (LinearLayout) findViewById(R.id.preferences_linearlayout_adventuredays);
        ConstraintLayout adventureCal = (ConstraintLayout) findViewById(R.id.preferences_constraintlayout_calendar);
        CalendarPickerView calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Chip[] daysOfTheWeek = {findViewById(R.id.Sunday_chip), findViewById(R.id.Monday_chip), findViewById(R.id.Tuesday_chip), findViewById(R.id.Wednesday_chip), findViewById(R.id.Thursday_chip), findViewById(R.id.Friday_chip), findViewById(R.id.Saturday_chip)};

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String prefRef = "Mike/UniqueID/Preferences";
        String messageRef = prefRef + "/defaultMessage";
        String timeoutLength = prefRef + "/timeoutLength";
        String[] daysOut = {prefRef + "/daysOut/Sunday", prefRef + "/daysOut/Monday", prefRef + "/daysOut/Tuesday", prefRef + "/daysOut/Wednesday", prefRef + "/daysOut/Thursday", prefRef + "/daysOut/Friday", prefRef + "/daysOut/Saturday"};
        String startDate = prefRef + "/tripDates/startDate";
        String endDate = prefRef + "/tripDates/endDate";

        DatabaseReference fireBasemessageRef = database.getReference(messageRef);
        DatabaseReference fireBasetimeOut = database.getReference(timeoutLength);
        DatabaseReference[] fireBaseDaysOut = new DatabaseReference[7];
        for(int i = 0; i < 7; i++) {
            fireBaseDaysOut[i] = database.getReference(daysOut[i]);
        }
        DatabaseReference fireBaseStartDate = database.getReference(startDate);
        DatabaseReference fireBaseEndDate = database.getReference(endDate);

        if(showingDefaultMessage) {
            if(!newDefault.getText().toString().equals("")) {
                String newMessage = newDefault.getText().toString();
                String path = "Mike/UniqueID/Contacts/Contact0";
                for (int i = 0; i < contacts.size(); i++) {
                    if (contacts.get(i).getMessage().equals(defaultMessage)) {
                        contacts.get(i).setMessage(newMessage);
                        path = path.substring(0, path.length() - 1);
                        path += String.valueOf(i + 1);
                        DatabaseReference contactMessage = database.getReference(path + "/Message");
                        contactMessage.setValue(newMessage);
                        System.out.println(path);
                    }
                }
                defaultMessage = newMessage;
                newDefault.setText("");
                newDefault.setHint(defaultMessage);
                fireBasemessageRef.setValue(defaultMessage);
            }
            defaultMessageLinearLayout.setVisibility(View.GONE);
            showingDefaultMessage = false;
        }

        if(showingAdventureLength) {
            String tempTime = dayVsHour.getText().toString();
            if(tempTime.equals("Days") || tempTime.equals("Day")) timeOut = Integer.parseInt(newLength.getText().toString()) * 24;
            else timeOut = Integer.parseInt(newLength.getText().toString());
            adventureLength.setVisibility(View.GONE);
            showingAdventureLength = false;
            fireBasetimeOut.setValue(timeOut);
        }

        if(showingAdventureDays) {
            for(int i = 0; i < 7; i ++) {
                weeklyOutingDays[i] = daysOfTheWeek[i].isChecked();
                fireBaseDaysOut[i].setValue(daysOfTheWeek[i].isChecked());
            }
            adventureDays.setVisibility(View.GONE);
            showingAdventureDays = false;
        }

        if(showingAdventureCalendar) {
            tripDates = calendar.getSelectedDates();
            adventureCal.setVisibility(View.GONE);
            showingAdventureCalendar = false;
            fireBaseStartDate.setValue(tripDates.get(0).toString());
            fireBaseEndDate.setValue(tripDates.get(tripDates.size() - 1).toString());
        }

        Snackbar.make(view, "Preferences Saved!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
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
