/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.coloradomesa.conradaremailer;

import com.google.firebase.database.*;
import java.io.IOException;

import java.util.Properties;    
import javax.mail.*;    
import javax.mail.internet.*;    
public class Main { 
    
/**
 *
 * @author jacksleeman
 */

    public static void main(String args[]){
        // 
        
        Thread t = new Thread(new ShowDbChanges());
        t.run();
        try{Thread.sleep(100000);}
        catch(InterruptedException e){
            e.printStackTrace();
        }
       
    }
    
}
