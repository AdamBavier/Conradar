package edu.coloradomesa.cs.conradar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
/*
This file is used to set root of database for the user, and uniqueID will need to be updated
in order to implement multiple users.
 */
public class FirebaseDBHelper {

    private String rootSTR;
    private FirebaseDatabase firebaseDB;
    public FirebaseDBHelper(){
        firebaseDB = FirebaseDatabase.getInstance();
        rootSTR = "Mike/" + getUniqueID();
    }
    public FirebaseDatabase getDB(){
        return firebaseDB;
    }
    public String getRootSTR(){
        return rootSTR;
    }
    String getUniqueID(){
        return "UniqueID" + "/";
    }
}
