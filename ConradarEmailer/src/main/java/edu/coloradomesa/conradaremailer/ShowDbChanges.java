/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.coloradomesa.conradaremailer;

/**
 *
 * @author jacksleeman
 */
import com.google.firebase.database.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ShowDbChanges implements Runnable {
    public void run() {
        FireBaseService fbs = null;
        System.out.println("ShowDbChanges ran");
        try {
            fbs = new FireBaseService();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DatabaseReference hasAlert = fbs.getDb().getReference("/Mike/UniqueID/hasAlert");
        DatabaseReference ref = fbs.getDb()
                .getReference("/Mike/UniqueID/");
     
        ref.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot dataSnapshot) {
                Object document = dataSnapshot.getValue();
                String lat = dataSnapshot.child("CurrentLoc").child("latitude").getValue().toString();
                String lang = dataSnapshot.child("CurrentLoc").child("longitude").getValue().toString();
                String loc = lat + "," +  lang;
                String lastLocTimestr = dataSnapshot.child("lastLocTime").getValue().toString();
                String timeToAlertstr = dataSnapshot.child("Time to Alert").getValue().toString();
                
                System.out.println("timeToAlert: "+ timeToAlertstr);
                
                LocalTime lastLocalTime = LocalTime.parse(lastLocTimestr, DateTimeFormatter.ofPattern("yyyy:MM:dd:hh:mm a"));
                LocalDateTime currentTime = LocalDateTime.now();
                //currentTime = LocalTime.parse()
                LocalDateTime timeToAlert = LocalDateTime.parse(timeToAlertstr, DateTimeFormatter.ofPattern("yyyy:MM:dd:hh:mm a"));
                
                String hasAlertstr = dataSnapshot.child("hasAlert").getValue().toString();
                String advMode = dataSnapshot.child("AdventureMode").getValue().toString();
                
                System.out.println(currentTime);
                System.out.println(timeToAlert);
                
                if(currentTime.isAfter(timeToAlert) && advMode.equals("on") && hasAlertstr.equals("no")){
                for(DataSnapshot datas: dataSnapshot.child("Contacts").getChildren()){
                        if(!datas.getKey().equals("Count")){
                        
                        String email = datas.child("Email").getValue().toString();
                        String message = datas.child("Message").getValue().toString();
                        String mailLink = "https://www.google.com/maps/search/?api=1&query=" + loc;
                        
                        hasAlert.setValue("yes", new DatabaseReference.CompletionListener(){
                            public void onComplete(DatabaseError error, DatabaseReference ref){
                                System.out.println("set has alert to yes");
                            }
                        });
                        
                        
                        System.out.println(email);
                        if(datas.child("Cell").getValue().toString().equals("True")){
                        Mailer.send("flaresosmail@gmail.com","Conradar",email,"",message);
                        Mailer.send("flaresosmail@gmail.com","Conradar",email,"\n",mailLink + " last seen at: " +lastLocTimestr);
                        }
                        else{
                        Mailer.send("flaresosmail@gmail.com","Conradar",email,"",message + " \n " + mailLink + " last seen at: " + lastLocTimestr);
                        }
                        }
                }

           
                }   
            }


            public void onCancelled(DatabaseError error) {
                System.out.print("Error: " + error.getMessage());
            }
        });


    }
}