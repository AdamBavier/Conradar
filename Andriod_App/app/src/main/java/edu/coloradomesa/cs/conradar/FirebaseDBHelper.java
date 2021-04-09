package edu.coloradomesa.cs.conradar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDBHelper {
    private String rootSTR;
    private FirebaseDatabase firebaseDB;
    public FirebaseDBHelper(){
        firebaseDB = FirebaseDatabase.getInstance();
        rootSTR = "Jack/" + getUniqueID();
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
