package orthostereogram;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fred
 */
public class UserInfo {
    public UserInfo () {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	//get current date time with Date()
	Date date = new Date();
	
        
        //Pas de connection au départ
        nom = null ;
        
        
        
        //On récupère la macAdress pour la connection
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            macaddress = sb.toString();
        } catch (Exception e) {}
    }
    
    String nom, prenom, titre, message, activite, adeli, code, mail ;
    String adr1, adr2, cp, ville, tel1, tel2 ;
    
    
    
    static public String macaddress = null ;
    
    
        
    public String getSoftVersion () {
        return "v.2.6.0 du 14/10/2017" ;
    }
   
    
    
    
    
    
            
    
    
    
}

