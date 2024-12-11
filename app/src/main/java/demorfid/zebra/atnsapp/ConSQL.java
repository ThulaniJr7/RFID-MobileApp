package demorfid.zebra.atnsapp;

import android.os.StrictMode;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConSQL {

    Connection con;

    public Connection conclass(){
         String ip="10.1.21.186", port="1433", db="RFID", username="rfidapp", password="rfid123";
         String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+db;

         // These are the connections for the DB
         // user="rfidhandheld" password="rfid@123atns"
         // user="rfidapp" password="rfid123"

        StrictMode.ThreadPolicy a = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(a);
        String ConnectURL = null;
        try
        {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            con = DriverManager.getConnection(url, username, password);

        }
        catch(Exception e)
        {
            Log.e("This is the following Error: ", e.getMessage());
        }

        return con;
    }
}
