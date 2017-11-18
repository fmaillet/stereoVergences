/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import ch.aplu.xboxcontroller.XboxController;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 *
 * @author Fred
 */
public class OrthoStereogram {
    
    public static final String VERSION = "0.6.5beta" ;
    public static boolean BR_glasses = true ;
    static public UserInfo user ;
    static public MySQLClass mySQLConnection ;
    
    public static int screenResolution ;
    public static NewController controller ;

    /**
     * @param args the command line arguments
     */
    
    
    
    public static void main(String[] args) {
        
        //Basic params
        screenResolution = Toolkit.getDefaultToolkit().getScreenResolution() ;
        
        //For connection
        user = new UserInfo () ;
        mySQLConnection = new MySQLClass () ;
        
        // no flickering on resize
        System.setProperty("sun.awt.noerasebackground", "true");
        
        /*//Check registry
        System.out.println("CPU Name : " + RegQuery.getCPUName());
        System.out.println("Personal directory : "
       + RegQuery.getCurrentUserPersonalFolderPath());*/
        
        
        
        
        //Main Frame
        controller = new NewController () ;
        //controller.setSize(450, 650);
        controller.setLocationRelativeTo(null);
        controller.setVisible(true);
        controller.initController();
    }
    
    static boolean is64bit() {
        return System.getProperty("sun.arch.data.model").equals("64");
    }
    
    public static void sortir () {
        //System.out.println ("sortie") ;
        controller.setEnabled(false);
        if (OrthoStereogram.user.nom != null) OrthoStereogram.mySQLConnection.rendreJeton();
        System.exit(0);
        
    }
    
}

class RegQuery {

  private static final String REGQUERY_UTIL = "reg query ";
  private static final String REGSTR_TOKEN = "REG_SZ";
  private static final String REGDWORD_TOKEN = "REG_DWORD";

  private static final String PERSONAL_FOLDER_CMD = REGQUERY_UTIL +
    "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\"
     + "Explorer\\Shell Folders\" /v Personal";
  private static final String CPU_SPEED_CMD = REGQUERY_UTIL +
    "\"HKLM\\HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0\""
     + " /v ~MHz";
  private static final String CPU_NAME_CMD = REGQUERY_UTIL +
   "\"HKLM\\HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0\""
     + " /v ProcessorNameString";
  private static final String DISPLAY_NAME_CMD = REGQUERY_UTIL +
   "\"HKLM\\SYSTEM\\CurrentControlSet\\Enum\\DISPLAY\""
     + " /s /v HardwareID";

  public static String getCurrentUserPersonalFolderPath() {
    try {
      Process process = Runtime.getRuntime().exec(PERSONAL_FOLDER_CMD);
      StreamReader reader = new StreamReader(process.getInputStream());

      reader.start();
      process.waitFor();
      reader.join();

      String result = reader.getResult();
      int p = result.indexOf(REGSTR_TOKEN);

      if (p == -1)
         return null;

      return result.substring(p + REGSTR_TOKEN.length()).trim();
    }
    catch (Exception e) {
      return null;
    }
  }

  public static String getCPUSpeed() {
    try {
      Process process = Runtime.getRuntime().exec(CPU_SPEED_CMD);
      StreamReader reader = new StreamReader(process.getInputStream());

      reader.start();
      process.waitFor();
      reader.join();

      String result = reader.getResult();
      int p = result.indexOf(REGDWORD_TOKEN);

      if (p == -1)
         return null;

      // CPU speed in Mhz (minus 1) in HEX notation, convert it to DEC
      String temp = result.substring(p + REGDWORD_TOKEN.length()).trim();
      return Integer.toString
          ((Integer.parseInt(temp.substring("0x".length()), 16) + 1));
    }
    catch (Exception e) {
      return null;
    }
  }

  public static String getCPUName() {
    try {
      Process process = Runtime.getRuntime().exec(DISPLAY_NAME_CMD);
      StreamReader reader = new StreamReader(process.getInputStream());

      reader.start();
      process.waitFor();
      reader.join();

      String result = reader.getResult();
      int p = result.indexOf("HardwareID");

      if (p == -1)
         return null;

      return result.substring(p + REGSTR_TOKEN.length()).trim();
    }
    catch (Exception e) {
      return null;
    }
  }

  static class StreamReader extends Thread {
    private InputStream is;
    private StringWriter sw;

    StreamReader(InputStream is) {
      this.is = is;
      sw = new StringWriter();
    }

    public void run() {
      try {
        int c;
        while ((c = is.read()) != -1)
          sw.write(c);
        }
        catch (IOException e) { ; }
      }

    String getResult() {
      return sw.toString();
    }
  }

  public static void main(String s[]) {
    System.out.println("Personal directory : "
       + getCurrentUserPersonalFolderPath());
    System.out.println("CPU Name : " + getCPUName());
    System.out.println("CPU Speed : " + getCPUSpeed() + " Mhz");
  }
}