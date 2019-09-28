package orthostereogram;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;




/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fred
 */
public class MySQLClass {
    private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DATABASE_URL = "jdbc:mysql://fredericmaillet.fr/fmaillet_professionnels?autoReconnect=true";
    private static final String USERNAME = "fmaillet_fredo";
    private static final String PASSWORD = "mastercog";
    private static final String MAX_POOL = "250";
    
    // init connection object
    public static Connection connection;
    // init properties object
    private Properties properties;
    
    // create properties
    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
            properties.setProperty("MaxPooledStatements", MAX_POOL);
        }
        return properties;
    }
    
    // connect database
    public Connection connect() {
        if (connection != null) {
            try {
                if (! connection.isValid(100)) {
                    disconnect () ;
                }
                
            }    catch (Exception e) { disconnect () ; }
        }
        if (connection == null) {
            try {
                Class.forName(DATABASE_DRIVER);
                DriverManager.setLoginTimeout(5);
                connection = DriverManager.getConnection(DATABASE_URL, getProperties());
                if (connection != null) connection.setAutoCommit(true);
            } catch (ClassNotFoundException | SQLException e) {
                //e.printStackTrace();
            }
            
        }
        
        return connection;
    }
    // disconnect database
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            
        }
    }
    
    private Statement transmission ;
    private ResultSet leResultat ;
    
    public static boolean getJeton (ResultSet r) { //Pour orthoEVA c'est le premier bit
        // on prend le jeton 
        try {
            byte jeton = r.getByte("JETON") ;
            jeton = (byte) (jeton | 0x01) ;
            r.updateByte("JETON", jeton);
            
        } catch (SQLException e) { return false ; }
        return true ;
    }

    //On rend le jeton...
    public void rendreJeton() {
        //connect () ;
        if (connect () != null) {
            try {
                transmission = connection.createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE) ;
                leResultat = transmission.executeQuery ("select ADELI, JETON from Pro where ADELI = " + OrthoStereogram.user.adeli) ;
                if (leResultat.next()) {
                    // On rend le jeton
                    byte jeton = leResultat.getByte("JETON") ;
                    jeton = (byte) (jeton & 0xFE) ;
                    leResultat.updateByte("JETON", jeton);
                    
                    //On met à jour
                    leResultat.updateRow () ;
                }
            } catch (Exception e) {}
            
        }
    }
    
    //On sauvegarde la calibration...
    public void saveCalibration() {
        //connect () ;
        if (connect () != null) {
            try {
                transmission = connection.createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE) ;
                leResultat = transmission.executeQuery ("select ADELI, CALIB_STEREO from Pro where ADELI = " + OrthoStereogram.user.adeli) ;
                if (leResultat.next()) {
                    // On rend le jeton
                    double jeton = leResultat.getDouble("CALIB_STEREO") ;
                    jeton = (double) OrthoStereogram.screenResolution ;
                    leResultat.updateDouble("CALIB_STEREO", jeton);
                    
                    //On met à jour
                    leResultat.updateRow () ;
                }
            } catch (Exception e) {}
            
        }
    }
    
    //On récupère la calibration...
    public boolean getCalibration() {
        int jeton = 0 ;
        if (connect () != null) {
            try {
                transmission = connection.createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE) ;
                leResultat = transmission.executeQuery ("select ADELI, CALIB_STEREO from Pro where ADELI = " + OrthoStereogram.user.adeli) ;
                if (leResultat.next()) {
                    // On rend le jeton
                    jeton = leResultat.getInt("CALIB_STEREO") ;
                    if (jeton > 0) {
                        OrthoStereogram.screenResolution = jeton ;
                    }
                    
                    //On met à jour
                    //leResultat.updateRow () ;
                }
            } catch (Exception e) {}
        }
        return (jeton > 0) ;
    }
    
    //On récupère les lunettes utilisées...
    public boolean getAnaglyphes() {
        int jeton = 0 ;
        if (connect () != null) {
            try {
                transmission = connection.createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE) ;
                leResultat = transmission.executeQuery ("select ADELI, ANAGLYPH from Pro where ADELI = " + OrthoStereogram.user.adeli) ;
                if (leResultat.next()) {
                    // On rend le jeton
                    jeton = leResultat.getInt("ANAGLYPH") ;
                    if (jeton == 0) OrthoStereogram.BR_glasses = false;
                    else OrthoStereogram.BR_glasses = true;
                        
                    
                    //On met à jour
                    //leResultat.updateRow () ;
                }
            } catch (Exception e) {}
        }
        return (true) ;
    }
    
    //On sauvegarde le type de lunettes...
    public void saveAnaglyphes() {
        //connect () ;
        if (connect () != null) {
            try {
                transmission = connection.createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE) ;
                leResultat = transmission.executeQuery ("select ADELI, ANAGLYPH from Pro where ADELI = " + OrthoStereogram.user.adeli) ;
                if (leResultat.next()) {
                    // On rend le jeton
                    double jeton = leResultat.getDouble("ANAGLYPH") ;
                    jeton = (double) (OrthoStereogram.BR_glasses ? 0 : 1) ;
                    leResultat.updateDouble("ANAGLYPH", jeton);
                    
                    //On met à jour
                    leResultat.updateRow () ;
                }
            } catch (Exception e) {}
            
        }
    }
    
}//Fin de la classe MySQLClass

