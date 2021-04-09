package edu.coloradomesa.cs.conradar;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;

import android.provider.ContactsContract;
import android.util.SparseIntArray;
import android.widget.AdapterView.OnItemSelectedListener;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;
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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.coloradomesa.cs.conradar.ui.main.MapFragment;
import edu.coloradomesa.cs.conradar.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private int timeOut = 24;

    ArrayList<String> namesList = new ArrayList<String>();

    ArrayList<Contact> contacts = new ArrayList<>();
    Contact currContact = null;
    String currName = "";
    Boolean[] weeklyOutingDays = {false, false, false, false, false, false, false};
    List<Date> tripDates = new ArrayList<>();
    private String defaultMessage = "Dear *Contact*,\n \nThis is an automated message informing you that *your name* began a trip on *start date* and has not returned. The recent location information for *your name* can be found here.";
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    ViewPager viewPager;
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
    }

    public void loadData(View view) {
        String fNameContact, lNameContact, emailContact, messageContact;
        final String[] fName = new String[1];
        final String[] lName = new String[1];
        final String[] email = new String[1];
        final String[] message = new String[1];
        final int[] i = new int[1];
        final boolean[] finished = {false};

        mDatabase.child("Mike/UniqueID/Contacts/").child("Count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else{
                    i[0] = Integer.parseInt(task.getResult().getValue().toString());
                    System.out.println("Here " + task.getResult().getValue());
                }
            }
        });

        System.out.println("There are " + i[0] + " contacts");

        while(!finished[0] && i[0] > 0) {
            fNameContact = "Contact" + i[0] + "/FirstName";
            lNameContact = "Contact" + i[0] + "/LastName";
            emailContact = "Contact" + i[0] + "/Email";
            messageContact = "Contact" + i[0] + "/Message";
            mDatabase.child("Mike/UniqueID/Contacts/").child(fNameContact).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else if(String.valueOf(task.getResult().getValue()).equals("null")) {
                        finished[0] = true;
                    } else{
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    }
                }
            });
            mDatabase.child("Mike/UniqueID/Contacts/").child(lNameContact).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else if(String.valueOf(task.getResult().getValue()).equals("null")) {
                        finished[0] = true;
                    } else{
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    }
                }
            });
            mDatabase.child("Mike/UniqueID/Contacts/").child(emailContact).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else if(String.valueOf(task.getResult().getValue()).equals("null")) {
                        finished[0] = true;
                    } else{
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    }
                }
            });
            mDatabase.child("Mike/UniqueID/Contacts/").child(messageContact).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else if(String.valueOf(task.getResult().getValue()).equals("null")) {
                        finished[0] = true;
                    } else{
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    }
                }
            });
            Contact c = new Contact(fName[0], lName[0], email[0], message[0]);
            contacts.add(c);
            i[0]--;
        }
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
                    customMessage.setHint(defaultMessage);
                    break;
        }
    }

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
                            EditText customMessage = (EditText)findViewById(R.id.edit_contact_custom_message_box);
                            if(found) {
                                editFirstName.setText(contacts.get(j).getFirstName());
                                editLastName.setText(contacts.get(j).getLastName());
                                editEmail.setText(contacts.get(j).getEmail());
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

    public void buttonEditContact(View view) {
        EditText firstName, lastName, tempEmail;
        firstName = (EditText) findViewById(R.id.edit_contact_first_name);
        String fName = firstName.getText().toString();
        lastName = (EditText) findViewById(R.id.edit_contact_last_name);
        String lName = lastName.getText().toString();
        tempEmail = (EditText) findViewById(R.id.edit_contact_email_address);
        String email = tempEmail.getText().toString();
        RadioButton btn = (RadioButton) findViewById(R.id.edit_contact_default_message);
        EditText customMessage = (EditText)findViewById(R.id.edit_contact_custom_message_box);
        String message = customMessage.getText().toString();
        // Check which radio button was clicked
        if(fName.equals("") || lName.equals("") || email.equals("")) {
            Snackbar.make(view, "Please provide all information!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            if (!email.equals("")) {
                if (btn.isChecked()) message = defaultMessage;
                currContact.setFirstName(fName);
                currContact.setLastName(lName);
                currContact.setEmail(email);
                currContact.setMessage(message);
                String name = fName + " " + lName;
                int index = namesList.indexOf(currName);
                namesList.set(index, name);
                firstName.setText("");
                lastName.setText("");
                tempEmail.setText("");
                customMessage.setText("");
                btn.setChecked(true);
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
                DatabaseReference fireBaseFname = database.getReference(fNameRef);
                fireBaseFname.setValue(fName);
                DatabaseReference fireBaseLname = database.getReference(lNameRef);
                fireBaseLname.setValue(lName);
                DatabaseReference fireBaseEmail = database.getReference(emailRef);
                fireBaseEmail.setValue(email);
                DatabaseReference fireBaseMessage = database.getReference(messageRef);
                fireBaseMessage.setValue(message);
            } else {
                Snackbar.make(view, "No contact selected", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    public void deleteContact(View view) {
        EditText email = (EditText) findViewById(R.id.edit_contact_email_address);
        if(!email.getText().toString().equals("")) {
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).getEmail().equals(email.getText().toString())) {
                    contacts.remove(i);
                    namesList.remove(i + 1);
                }
            }
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

    public void addContact(View view) {
        EditText firstName, lastName, tempEmail;
        firstName = (EditText) findViewById(R.id.emergency_contact_firstname);
        String fName = firstName.getText().toString();
        lastName = (EditText) findViewById(R.id.emergency_contact_lastname);
        String lName = lastName.getText().toString();
        tempEmail = (EditText) findViewById(R.id.emergency_contact_email_address);
        String email = tempEmail.getText().toString();
        RadioButton btn = (RadioButton) findViewById(R.id.default_message_radio_select);
        EditText customMessage = (EditText)findViewById(R.id.custom_message_text);
        String message = customMessage.getText().toString();
        Contact c = null;
        if(fName.equals("") || lName.equals("") || email.equals("")) {
            Snackbar.make(view, "Please provide all information!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            // Check which radio button was clicked
            if (btn.isChecked()) message = defaultMessage;
            c = new Contact(fName, lName, email, message);
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
            DatabaseReference fireBaseContactsCount = database.getReference("Mike/UniqueID/Contacts/Count");
            fireBaseContactsCount.setValue(contactNo);
            DatabaseReference fireBaseFname = database.getReference(fNameRef);
            fireBaseFname.setValue(fName);
            DatabaseReference fireBaseLname = database.getReference(lNameRef);
            fireBaseLname.setValue(lName);
            DatabaseReference fireBaseEmail = database.getReference(emailRef);
            fireBaseEmail.setValue(email);
            DatabaseReference fireBaseMessage = database.getReference(messageRef);
            fireBaseMessage.setValue(message);
        }
    }

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

    public void preferencesShowAdventureDays(View view) {
        LinearLayout adventureDays = (LinearLayout) findViewById(R.id.preferences_linearlayout_adventuredays);
        Chip sunday = (Chip) findViewById(R.id.Sunday_chip);
        Chip monday = (Chip) findViewById(R.id.Monday_chip);
        Chip tuesday = (Chip) findViewById(R.id.Tuesday_chip);
        Chip wednesday = (Chip) findViewById(R.id.Wednesday_chip);
        Chip thursday = (Chip) findViewById(R.id.Thursday_chip);
        Chip friday = (Chip) findViewById(R.id.Friday_chip);
        Chip saturday = (Chip) findViewById(R.id.Saturday_chip);

        if(showingAdventureDays) {
            showingAdventureDays = false;
            adventureDays.setVisibility(View.GONE);
        }
        else {
            showingAdventureDays = true;
            adventureDays.setVisibility(View.VISIBLE);
            sunday.setChecked(weeklyOutingDays[0]);
            monday.setChecked(weeklyOutingDays[1]);
            tuesday.setChecked(weeklyOutingDays[2]);
            wednesday.setChecked(weeklyOutingDays[3]);
            thursday.setChecked(weeklyOutingDays[4]);
            friday.setChecked(weeklyOutingDays[5]);
            saturday.setChecked(weeklyOutingDays[6]);
        }
    }

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
                datePicker.init(today, nextYear.getTime())
                        .inMode(CalendarPickerView.SelectionMode.RANGE)
                        .withSelectedDates(temp);
            }

            datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
                @Override
                public void onDateSelected(Date date) {
                    //String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(date);
                    /*Calendar calSelected = Calendar.getInstance();
                    calSelected.setTime(date);
                    String selectedDate = "" + calSelected.get(Calendar.DAY_OF_MONTH)
                            + " " + (calSelected.get(Calendar.MONTH) + 1)
                            + " " + calSelected.get(Calendar.YEAR);
                    Toast.makeText(MainActivity.this, selectedDate, Toast.LENGTH_SHORT).show();*/
                }

                @Override
                public void onDateUnselected(Date date) {
                }
            });
        }
    }

    public void updateInformation(View view) {
        EditText newDefault = (EditText) findViewById(R.id.preferences_set_default_message);
        TextView newLength = (TextView) findViewById(R.id.adventure_length);
        TextView dayVsHour = (TextView) findViewById(R.id.day_hour_tag);
        LinearLayout defaultMessageLinearLayout = (LinearLayout) findViewById(R.id.preferences_LinerarLayout_DefaultMessage);
        LinearLayout adventureLength = (LinearLayout) findViewById(R.id.preferences_linearlayout_adventurelength);
        LinearLayout adventureDays = (LinearLayout) findViewById(R.id.preferences_linearlayout_adventuredays);
        ConstraintLayout adventureCal = (ConstraintLayout) findViewById(R.id.preferences_constraintlayout_calendar);
        CalendarPickerView calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Chip sunday = (Chip) findViewById(R.id.Sunday_chip);
        Chip monday = (Chip) findViewById(R.id.Monday_chip);
        Chip tuesday = (Chip) findViewById(R.id.Tuesday_chip);
        Chip wednesday = (Chip) findViewById(R.id.Wednesday_chip);
        Chip thursday = (Chip) findViewById(R.id.Thursday_chip);
        Chip friday = (Chip) findViewById(R.id.Friday_chip);
        Chip saturday = (Chip) findViewById(R.id.Saturday_chip);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String prefRef = "Mike/UniqueID/Preferences";
        String messageRef = prefRef + "/defaultMessage";
        String timeoutLength = prefRef + "/timeoutLength";
        String[] daysOut = {prefRef + "/daysOut/Sunday", prefRef + "/daysOut/Monday", prefRef + "/daysOut/Tuesday", prefRef + "/daysOut/Wednesday", prefRef + "/daysOut/Thursday", prefRef + "/daysOut/Friday", prefRef + "/daysOut/Saturday"};
        String calTripDates = prefRef + "/tripDates";

        DatabaseReference fireBasemessageRef = database.getReference(messageRef);
        DatabaseReference fireBasetimeOut = database.getReference(timeoutLength);
        DatabaseReference[] fireBaseDaysOut = {};
        for(int i = 0; i < 6; i++) {
            fireBaseDaysOut[i] = database.getReference(daysOut[i]);
        }
        DatabaseReference fireBasetripDates = database.getReference(calTripDates);

        if(showingDefaultMessage) {
            if(!newDefault.getText().toString().equals("")) {
                String newMessage = newDefault.getText().toString();
                for (int i = 0; i < contacts.size(); i++) {
                    if (contacts.get(i).getMessage().equals(defaultMessage))
                        contacts.get(i).setMessage(newMessage);
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
            weeklyOutingDays[0] = sunday.isChecked();
            fireBaseDaysOut[0].setValue(sunday.isChecked());
            weeklyOutingDays[1] = monday.isChecked();
            fireBaseDaysOut[1].setValue(sunday.isChecked());
            weeklyOutingDays[2] = tuesday.isChecked();
            fireBaseDaysOut[2].setValue(sunday.isChecked());
            weeklyOutingDays[3] = wednesday.isChecked();
            fireBaseDaysOut[3].setValue(sunday.isChecked());
            weeklyOutingDays[4] = thursday.isChecked();
            fireBaseDaysOut[4].setValue(sunday.isChecked());
            weeklyOutingDays[5] = friday.isChecked();
            fireBaseDaysOut[5].setValue(sunday.isChecked());
            weeklyOutingDays[6] = saturday.isChecked();
            fireBaseDaysOut[6].setValue(sunday.isChecked());
            adventureDays.setVisibility(View.GONE);
            showingAdventureDays = false;
        }

        if(showingAdventureCalendar) {
            List<Date> dates = calendar.getSelectedDates();
            tripDates = dates;
            adventureCal.setVisibility(View.GONE);
            showingAdventureCalendar = false;

        }

        Snackbar.make(view, "Preferences Saved!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        /*FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        DatabaseReference myRef2 = database.getReference("name");*/
    }

    public void setGeoFence(View view) {
        Log.d("Yes", "btnclick");

          ViewPager mPager = viewPager;
        PagerAdapter adapter = mPager.getAdapter();
        int fragmentIndex = mPager.getCurrentItem();
        SectionsPagerAdapter spa = (SectionsPagerAdapter) adapter;
        MapFragment currentFragment = (MapFragment) spa.getItem(fragmentIndex);
        currentFragment.setGeoFence();
        }
    }
